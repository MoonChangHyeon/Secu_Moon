package com.example.vulnscanner.module.compliance.service;

import com.example.vulnscanner.module.compliance.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class XmlExportService {

    private final PackInfoRepository packInfoRepository;
    private final ComplianceStandardRepository standardRepository;

    @Transactional(readOnly = true)
    public void exportToXml(Long packId, OutputStream outputStream) throws Exception {
        PackInfo pack = packInfoRepository.findById(packId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Pack ID"));

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // Root element <ExternalMetadata> (For Custom Pack) or <ExternalMetadataPack>
        // Following KISA original format which is usually <ExternalMetadata>
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("ExternalMetadata");
        doc.appendChild(rootElement);

        // Pack Info (Implicit in KISA xml structure usually as properites or header
        // lists)
        // But let's follow the standard Fortify XML export structure we saw in
        // RulepackService
        // Custom Rulepack usually starts with ExternalList directly or wrapped.

        // Let's iterate standards
        List<ComplianceStandard> standards = standardRepository.findByPackInfoId(packId);

        for (ComplianceStandard std : standards) {
            Element listElement = doc.createElement("ExternalList");
            rootElement.appendChild(listElement);

            appendTag(doc, listElement, "Name", std.getName());
            appendTag(doc, listElement, "ExternalListID", std.getExternalListId());
            appendTag(doc, listElement, "Description", std.getDescription());

            // Categories
            for (ComplianceCategory cat : std.getCategories()) {
                Element catElement = doc.createElement("ExternalCategoryDefinition");
                listElement.appendChild(catElement);

                appendTag(doc, catElement, "Name", cat.getName());
                appendTag(doc, catElement, "Description", cat.getDescription());
            }

            // Mappings (Flat list in KISA format usually?)
            // Or inside Categories?
            // Re-checking RulepackService.processExternalList:
            // It reads "Mapping" tags from 'standardElement' (ExternalList).
            // So Mappings are siblings of ExternalCategoryDefinition, NOT children.

            for (ComplianceCategory cat : std.getCategories()) {
                for (ComplianceMapping map : cat.getMappings()) {
                    if (map.isActive()) { // Only active mappings
                        Element mappingElement = doc.createElement("Mapping");
                        listElement.appendChild(mappingElement);

                        appendTag(doc, mappingElement, "InternalCategory", map.getInternalCategory());
                        appendTag(doc, mappingElement, "ExternalCategory", cat.getName());
                    }
                }
            }
        }

        // Write to output
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(outputStream);

        transformer.transform(source, result);
    }

    private void appendTag(Document doc, Element parent, String tagName, String value) {
        Element element = doc.createElement(tagName);
        element.setTextContent(value != null ? value : "");
        parent.appendChild(element);
    }
}
