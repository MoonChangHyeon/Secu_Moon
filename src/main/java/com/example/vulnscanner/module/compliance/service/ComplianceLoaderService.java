package com.example.vulnscanner.module.compliance.service;

import com.example.vulnscanner.module.compliance.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplianceLoaderService {

    private final PackInfoRepository packInfoRepository;
    private final ComplianceStandardRepository standardRepository;
    private final ComplianceCategoryRepository categoryRepository;

    @Transactional
    public void loadSystemData(String clientDataPath) {
        log.info("Starting System Data Load from: {}", clientDataPath);

        // 1. Load Fortify External Metadata (Optional, if we want to extract Rule
        // metadata, but for now we focus on mappings)
        // loadFortifyMetadata(new File(clientDataPath, "externalmetadata.xml"));

        // 2. Load KISA 49 Metadata
        File kisaXml = new File(clientDataPath, "kisa49_metadata.xml");
        if (kisaXml.exists()) {
            loadKisaMetadata(kisaXml);
        } else {
            log.error("kisa49_metadata.xml not found at {}", kisaXml.getAbsolutePath());
        }
    }

    private void loadKisaMetadata(File xmlFile) {
        try (FileInputStream fis = new FileInputStream(xmlFile)) {
            loadKisaMetadataFromStream(fis);
        } catch (Exception e) {
            log.error("Failed to load KISA metadata from file: {}", xmlFile.getAbsolutePath(), e);
            throw new RuntimeException("Failed to load KISA metadata from file", e);
        }
    }

    public void loadKisaMetadata(java.io.InputStream inputStream) {
        loadKisaMetadataFromStream(inputStream);
    }

    private void loadKisaMetadataFromStream(java.io.InputStream inputStream) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            // Use InputStreamReader to ensure UTF-8 encoding
            Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            InputSource is = new InputSource(reader);
            is.setEncoding("UTF-8");
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

            // Create or Update System PackInfo
            String packName = "KISA 49 (System)";
            PackInfo packInfo = packInfoRepository.findByName(packName)
                    .orElse(new PackInfo());

            if (packInfo.getId() == null) {
                packInfo.setName(packName);
                packInfo.setPackId("KISA_49_SYSTEM");
                packInfo.setVersion(
                        LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")));
                packInfo.setLocale("ko");
                packInfo.setCustom(false);
                packInfo = packInfoRepository.save(packInfo);
            }

            // Parse Standards (ExternalList)
            // KISA 49 xml usually has <ExternalList>
            NodeList externalLists = doc.getElementsByTagName("ExternalList");
            if (externalLists.getLength() > 0) {
                Element listElement = (Element) externalLists.item(0);
                processStandard(listElement, packInfo);
            }

        } catch (Exception e) {
            log.error("Failed to parse KISA metadata XML", e);
            throw new RuntimeException("Failed to parse KISA metadata XML", e);
        }
    }

    private void processStandard(Element standardElement, PackInfo packInfo) {
        String standardName = getTagValue("Name", standardElement);
        String extListId = getTagValue("ExternalListID", standardElement);

        ComplianceStandard standard = standardRepository.findByPackInfoIdAndName(packInfo.getId(), standardName)
                .orElse(new ComplianceStandard());

        standard.setPackInfo(packInfo);
        standard.setName(standardName);
        standard.setExternalListId(extListId);
        standard.setDescription(getTagValue("Description", standardElement));
        standard = standardRepository.save(standard);

        // Categories
        Map<String, ComplianceCategory> categoryMap = new HashMap<>();
        NodeList categories = standardElement.getElementsByTagName("ExternalCategoryDefinition");

        for (int i = 0; i < categories.getLength(); i++) {
            Element catElem = (Element) categories.item(i);
            String catName = getTagValue("Name", catElem);
            if (catName.isEmpty())
                continue;

            ComplianceCategory category = categoryRepository.findByStandardIdAndName(standard.getId(), catName)
                    .orElse(new ComplianceCategory());

            category.setStandard(standard);
            category.setName(catName);
            category.setDescription(getTagValue("Description", catElem));
            // Save immediately to use in mapping
            category = categoryRepository.save(category);
            categoryMap.put(catName, category);
        }

        // Mappings
        NodeList mappings = standardElement.getElementsByTagName("Mapping");
        for (int i = 0; i < mappings.getLength(); i++) {
            Element mapElem = (Element) mappings.item(i);
            String internal = getTagValue("InternalCategory", mapElem);
            String external = getTagValue("ExternalCategory", mapElem);

            ComplianceCategory category = categoryMap.get(external);
            if (category != null) {
                // Check if mapping exists
                boolean exists = category.getMappings().stream()
                        .anyMatch(m -> m.getInternalCategory().equals(internal));

                if (!exists) {
                    ComplianceMapping mapping = new ComplianceMapping();
                    mapping.setInternalCategory(internal);
                    mapping.setExternalCategoryName(external);
                    mapping.setCategory(category);
                    mapping.setCustom(false); // System mapping
                    mapping.setActive(true);
                    category.getMappings().add(mapping);
                }
            }
        }
        categoryRepository.saveAll(categoryMap.values());
    }

    private String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList != null && nodeList.getLength() > 0) {
            Node item = nodeList.item(0);
            return item.getTextContent().trim();
        }
        return "";
    }
}
