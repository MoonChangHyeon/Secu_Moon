package com.example.vulnscanner.service;

import com.example.vulnscanner.entity.*;
import com.example.vulnscanner.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
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
    public PackInfo uploadRulepack(MultipartFile file) throws Exception {
        return parseAndSave(file.getInputStream());
    }

    @Transactional
    public PackInfo parseAndSave(InputStream inputStream)
            throws ParserConfigurationException, IOException, SAXException {
        // 1. Parse XML
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputStream);
        doc.getDocumentElement().normalize();

        // 2. Validate Root Element
        if (!"ExternalMetadataPack".equals(doc.getDocumentElement().getNodeName())) {
            throw new IllegalArgumentException("Invalid Rulepack file: Root element must be <ExternalMetadataPack>");
        }

        // 3. Parse PackInfo
        Node packInfoNode = doc.getElementsByTagName("PackInfo").item(0);
        if (packInfoNode == null) {
            throw new IllegalArgumentException("Invalid Rulepack file: Missing <PackInfo>");
        }

        Element packInfoElement = (Element) packInfoNode;
        String version = getTagValue("Version", packInfoElement);
        String name = getTagValue("Name", packInfoElement);
        String packId = getTagValue("PackID", packInfoElement);
        String locale = getTagValue("Locale", packInfoElement);

        // 4. Check Duplicate Version
        Optional<PackInfo> existingPack = packInfoRepository.findByVersion(version);
        if (existingPack.isPresent()) {
            throw new IllegalArgumentException("Pack version " + version + " already exists.");
        }

        PackInfo packInfo = new PackInfo();
        packInfo.setVersion(version);
        packInfo.setName(name);
        packInfo.setPackId(packId);
        packInfo.setLocale(locale);

        packInfo = packInfoRepository.save(packInfo);
        log.info("Saved PackInfo: {} (Version: {})", name, version);

        // 5. Parse ExternalList (Standards)
        NodeList externalLists = doc.getElementsByTagName("ExternalList");
        for (int i = 0; i < externalLists.getLength(); i++) {
            Node externalListNode = externalLists.item(i);
            if (externalListNode.getNodeType() == Node.ELEMENT_NODE) {
                Element standardElement = (Element) externalListNode;

                String standardName = getTagValue("Name", standardElement);
                // Skip non-standard lists if necessary, but request implied "all standards"

                ComplianceStandard standard = new ComplianceStandard();
                standard.setExternalListId(getTagValue("ExternalListID", standardElement));
                standard.setName(standardName);
                standard.setDescription(getTagValue("Description", standardElement));
                standard.setPackInfo(packInfo);

                // Save Standard first to get ID (though CascadeType.ALL handles it, explicit
                // save is clearer for category mapping)
                // Actually with CascadeType.ALL on PackInfo->Standard, we add to list and save
                // PackInfo.
                // But PackInfo is already saved. So we can add to
                // packInfo.getStandards().add(standard) and save standard?
                // Better: Set relationships and save.

                standard = standardRepository.save(standard);

                // 6. Parse and Save Categories (ExternalCategoryDefinition)
                Map<String, ComplianceCategory> categoryMap = new HashMap<>(); // Name -> Entity
                NodeList categories = standardElement.getElementsByTagName("ExternalCategoryDefinition");

                for (int j = 0; j < categories.getLength(); j++) {
                    Element categoryElement = (Element) categories.item(j);
                    String categoryName = getTagValue("Name", categoryElement);

                    ComplianceCategory category = new ComplianceCategory();
                    category.setName(categoryName);
                    category.setDescription(getTagValue("Description", categoryElement));
                    category.setStandard(standard);

                    categoryMap.put(categoryName, category);
                    standard.getCategories().add(category);
                }
                // Save categories in bulk via cascade? standard is already saved.
                // Let's modify standard and save it again or save categories directly.
                // Since we need IDs for mapping, we should save categories now.
                categoryRepository.saveAll(standard.getCategories());

                // 7. Parse and Save Mappings
                NodeList mappings = standardElement.getElementsByTagName("Mapping");
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
                    } else {
                        log.warn("Mapping target category not found: {}", externalCategoryName);
                    }
                }
                // Save categories again to update mappings (cascade)
                categoryRepository.saveAll(standard.getCategories());
            }
        }

        return packInfo;
    }

    @Transactional
    public void deleteRulepack(Long id) {
        packInfoRepository.deleteById(id);
    }

    public List<PackInfo> getAllRulepacks() {
        return packInfoRepository.findAll();
    }

    private String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodeList.item(0);
        return node != null ? node.getNodeValue() : "";
    }
}
