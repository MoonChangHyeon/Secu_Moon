package com.example.vulnscanner.module.analysis;

import com.example.vulnscanner.module.compliance.ComplianceCategory;
import com.example.vulnscanner.module.compliance.ComplianceCategoryRepository;
import com.example.vulnscanner.module.compliance.ComplianceMapping;
import com.example.vulnscanner.module.compliance.ComplianceMappingRepository;
import com.example.vulnscanner.module.compliance.ComplianceStandard;
import com.example.vulnscanner.module.compliance.ComplianceStandardRepository;
import com.example.vulnscanner.module.compliance.PackInfo;
import com.example.vulnscanner.module.compliance.PackInfoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RulepackService {

    private final PackInfoRepository packInfoRepository;
    private final ComplianceStandardRepository standardRepository;
    private final ComplianceCategoryRepository categoryRepository;
    private final ComplianceMappingRepository mappingRepository;

    @Transactional
    public PackInfo uploadRulepack(MultipartFile file, boolean isCustom) throws Exception {
        return parseAndSave(file.getInputStream(), isCustom);
    }

    @Transactional
    public PackInfo parseAndSave(InputStream inputStream, boolean isCustom)
            throws ParserConfigurationException, IOException, SAXException {
        // 1. Parse XML with explicit UTF-8 encoding via InputSource
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        // Wrap InputStream in InputSource to help parser for encoding if header is
        // missing/wrong
        // Use Reader to enforce UTF-8 if we trust the file is UTF-8
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");

        Document doc = dBuilder.parse(is);
        doc.getDocumentElement().normalize();

        Element root = doc.getDocumentElement();
        String rootName = root.getNodeName();
        log.info("Parsing Rulepack XML. Root Element: {}, IsCustom: {}", rootName, isCustom);

        PackInfo packInfo = new PackInfo();
        packInfo.setCustom(isCustom);
        packInfo.setUploadDate(LocalDateTime.now());

        if ("ExternalMetadataPack".equals(rootName)) {
            // Standard Fortify Rulepack
            parseFortifyPackInfo(root, packInfo);
        } else if ("ExternalMetadata".equals(rootName)) {
            // Custom Rulepack (e.g., KISA)
            parseCustomPackInfo(root, packInfo);
        } else {
            // Default Fallback or Error
            if (isCustom) {
                parseCustomPackInfo(root, packInfo);
            } else {
                throw new IllegalArgumentException(
                        "Invalid Rulepack file: Root must be <ExternalMetadataPack> or <ExternalMetadata>");
            }
        }

        log.info("Parsed PackInfo - Name: {}, Version: {}, ID: {}", packInfo.getName(), packInfo.getVersion(),
                packInfo.getPackId());

        // Check Duplicate Version
        Optional<PackInfo> existingPack = packInfoRepository.findByVersion(packInfo.getVersion());
        if (existingPack.isPresent()) {
            throw new IllegalArgumentException("Pack version " + packInfo.getVersion() + " already exists.");
        }

        packInfo = packInfoRepository.save(packInfo);
        log.info("Saved PackInfo to DB: {} (ID: {})", packInfo.getName(), packInfo.getId());

        // Parse Standards (ExternalList)
        NodeList externalLists = doc.getElementsByTagName("ExternalList");
        log.info("Found {} ExternalList elements", externalLists.getLength());

        for (int i = 0; i < externalLists.getLength(); i++) {
            Node externalListNode = externalLists.item(i);
            if (externalListNode.getNodeType() == Node.ELEMENT_NODE) {
                Element standardElement = (Element) externalListNode;
                processExternalList(standardElement, packInfo);
            }
        }

        return packInfo;
    }

    private void parseFortifyPackInfo(Element root, PackInfo packInfo) {
        Node packInfoNode = root.getElementsByTagName("PackInfo").item(0);
        if (packInfoNode == null) {
            throw new IllegalArgumentException("Invalid Rulepack file: Missing <PackInfo>");
        }
        Element packInfoElement = (Element) packInfoNode;
        packInfo.setVersion(getTagValue("Version", packInfoElement));
        packInfo.setName(getTagValue("Name", packInfoElement));
        packInfo.setPackId(getTagValue("PackID", packInfoElement));
        packInfo.setLocale(getTagValue("Locale", packInfoElement));
    }

    private void parseCustomPackInfo(Element root, PackInfo packInfo) {
        NodeList lists = root.getElementsByTagName("ExternalList");
        if (lists.getLength() > 0) {
            Element firstList = (Element) lists.item(0);
            String name = getTagValue("Name", firstList);
            String id = getTagValue("ExternalListID", firstList);
            String internalId = getTagValue("InternalListID", firstList);
            String displayId = internalId.isEmpty() ? id : internalId;

            // Version generation
            String dateVer = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

            packInfo.setName(name);
            packInfo.setPackId(displayId);
            // If already has version tag? Custom usually doesn't.
            // Use DisplayID + Date as version
            packInfo.setVersion(dateVer); // Simplified version
            packInfo.setLocale("ko");
        } else {
            packInfo.setName("Unknown Custom Pack");
            packInfo.setVersion("Custom-" + System.currentTimeMillis());
            packInfo.setPackId("CUSTOM");
        }
    }

    private void processExternalList(Element standardElement, PackInfo packInfo) {
        String standardName = getTagValue("Name", standardElement);
        log.debug("Processing Standard: {}", standardName);

        ComplianceStandard standard = new ComplianceStandard();
        String extListId = getTagValue("ExternalListID", standardElement);
        if (extListId.isEmpty())
            extListId = getTagValue("InternalListID", standardElement);

        standard.setExternalListId(extListId);
        standard.setName(standardName);
        standard.setDescription(getTagValue("Description", standardElement));
        standard.setPackInfo(packInfo);

        standard = standardRepository.save(standard);

        // Parse and Save Categories
        Map<String, ComplianceCategory> categoryMap = new HashMap<>();
        NodeList categories = standardElement.getElementsByTagName("ExternalCategoryDefinition");
        log.debug("Found {} Categories for Standard {}", categories.getLength(), standardName);

        for (int j = 0; j < categories.getLength(); j++) {
            Element categoryElement = (Element) categories.item(j);
            String categoryName = getTagValue("Name", categoryElement);

            if (categoryName.isEmpty())
                continue;

            ComplianceCategory category = new ComplianceCategory();
            category.setName(categoryName);
            category.setDescription(getTagValue("Description", categoryElement));
            category.setStandard(standard);

            categoryMap.put(categoryName, category);
            standard.getCategories().add(category);
        }
        categoryRepository.saveAll(standard.getCategories());

        // Parse and Save Mappings
        NodeList mappings = standardElement.getElementsByTagName("Mapping");
        log.debug("Found {} Mappings for Standard {}", mappings.getLength(), standardName);

        for (int k = 0; k < mappings.getLength(); k++) {
            Element mappingElement = (Element) mappings.item(k);
            String internalCategory = getTagValue("InternalCategory", mappingElement);
            String externalCategoryName = getTagValue("ExternalCategory", mappingElement);

            ComplianceCategory category = categoryMap.get(externalCategoryName);
            if (category != null) {
                ComplianceMapping mapping = new ComplianceMapping();
                mapping.setInternalCategory(internalCategory);
                mapping.setExternalCategoryName(externalCategoryName);
                mapping.setCategory(category);
                category.getMappings().add(mapping);
            }
        }
        categoryRepository.saveAll(standard.getCategories());
    }

    @Transactional
    public void deleteRulepack(Long id) {
        packInfoRepository.deleteById(id);
    }

    public List<PackInfo> getAllRulepacks() {
        return packInfoRepository.findAll();
    }

    public List<PackInfo> getRulepacksByCustom(boolean isCustom) {
        return packInfoRepository.findAll().stream()
                .filter(p -> p.isCustom() == isCustom)
                .toList();
    }

    private String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList == null || nodeList.getLength() == 0)
            return "";

        Node item = nodeList.item(0);
        if (item == null)
            return "";

        if (item.hasChildNodes()) {
            Node child = item.getFirstChild();
            // Loop through all children to append text if split across nodes (e.g. CDATA or
            // entities)
            // But usually first child is enough for simple text.
            // For safety against fragmented text nodes:
            StringBuilder sb = new StringBuilder();
            NodeList children = item.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node c = children.item(i);
                if (c.getNodeType() == Node.TEXT_NODE || c.getNodeType() == Node.CDATA_SECTION_NODE) {
                    sb.append(c.getNodeValue());
                }
            }
            return sb.toString().trim();
        }
        return "";
    }
}