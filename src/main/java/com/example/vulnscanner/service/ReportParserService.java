package com.example.vulnscanner.service;

import com.example.vulnscanner.entity.AnalysisResult;
import com.example.vulnscanner.entity.ScanSummary;
import com.example.vulnscanner.entity.Vulnerability;
import com.example.vulnscanner.repository.AnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportParserService {

    private final AnalysisRepository analysisRepository;

    @Transactional
    public void parseAndSave(File xmlFile, AnalysisResult result) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);
        XPath xPath = XPathFactory.newInstance().newXPath();

        // 1. Parse Scan Summary
        ScanSummary summary = new ScanSummary();
        summary.setAnalysisResult(result);

        // Scan Date
        String execSummaryText = (String) xPath.evaluate(
                "//ReportSection[Title='Executive Summary']/SubSection[Title='Issues Overview']/Text", doc,
                XPathConstants.STRING);
        summary.setScanDate(extractScanDate(execSummaryText));

        // LOC & File Count
        String codeBaseText = (String) xPath.evaluate(
                "//ReportSection[Title='Project Summary']/SubSection[Title='Code Base Summary']/Text", doc,
                XPathConstants.STRING);
        summary.setTotalLoc(extractInt(codeBaseText, "Lines of Code:"));
        summary.setFileCount(extractInt(codeBaseText, "Number of Files:"));
        String codeLocation = extractString(codeBaseText, "Code location:");

        // Version & Machine Name
        String scanInfoText = (String) xPath.evaluate(
                "//ReportSection[Title='Project Summary']/SubSection[Title='Scan Information']/Text", doc,
                XPathConstants.STRING);
        summary.setScaEngineVersion(extractString(scanInfoText, "SCA Engine version:"));
        summary.setMachineName(extractString(scanInfoText, "Machine Name:"));
        summary.setScanTime(extractString(scanInfoText, "Scan time:"));

        result.setScanSummary(summary);

        // 2. Parse Vulnerabilities
        List<Vulnerability> vulnerabilities = new ArrayList<>();
        NodeList issueNodes = (NodeList) xPath.evaluate(
                "//ReportSection[Title='Results Outline']/SubSection/IssueListing/Chart/GroupingSection/Issue", doc,
                XPathConstants.NODESET);

        for (int i = 0; i < issueNodes.getLength(); i++) {
            Node issueNode = issueNodes.item(i);
            if (issueNode.getNodeType() == Node.ELEMENT_NODE) {
                Element issueElement = (Element) issueNode;
                Vulnerability vuln = new Vulnerability();
                vuln.setAnalysisResult(result);

                vuln.setIssueId(issueElement.getAttribute("iid"));
                vuln.setRuleId(issueElement.getAttribute("ruleID"));
                vuln.setCategory(getTagValue(issueElement, "Category"));
                vuln.setPriority(getTagValue(issueElement, "Friority")); // Note: XML tag is 'Friority' in some
                                                                         // versions, check if it's 'Priority'
                if (vuln.getPriority() == null || vuln.getPriority().isEmpty()) {
                    vuln.setPriority(getTagValue(issueElement, "Priority"));
                }
                vuln.setKingdom(getTagValue(issueElement, "Kingdom"));
                vuln.setAbstractMessage(getTagValue(issueElement, "Abstract"));

                // Primary Location
                Node primaryNode = (Node) xPath.evaluate("Primary", issueElement, XPathConstants.NODE);
                if (primaryNode != null && primaryNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element primaryElement = (Element) primaryNode;
                    vuln.setFileName(getTagValue(primaryElement, "FileName"));
                    String filePath = getTagValue(primaryElement, "FilePath");
                    if (codeLocation != null && filePath != null) {
                        // Combine codeLocation and filePath
                        // Check if codeLocation ends with / or filePath starts with / to avoid double
                        // slashes
                        String fullPath;
                        if (codeLocation.endsWith("/") || filePath.startsWith("/")) {
                            fullPath = codeLocation + filePath;
                        } else {
                            fullPath = codeLocation + "/" + filePath;
                        }
                        vuln.setFilePath(fullPath);
                    } else {
                        vuln.setFilePath(filePath);
                    }
                    String lineStart = getTagValue(primaryElement, "LineStart");
                    if (lineStart != null && !lineStart.isEmpty()) {
                        vuln.setLineNumber(Integer.parseInt(lineStart));
                    }
                    vuln.setCodeSnippet(getTagValue(primaryElement, "Snippet"));
                }

                vulnerabilities.add(vuln);
            }
        }

        if (result.getVulnerabilities() == null) {
            result.setVulnerabilities(new ArrayList<>());
        } else {
            result.getVulnerabilities().clear();
        }
        result.getVulnerabilities().addAll(vulnerabilities);

        analysisRepository.save(result);
        log.info("Parsed and saved {} vulnerabilities for result ID: {}", vulnerabilities.size(), result.getId());
    }

    private String getTagValue(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }

    private String extractScanDate(String text) {
        if (text == null)
            return null;
        // Example: "On 2025. 11. 29., a source code review..."
        // Regex to capture date pattern like YYYY. MM. DD. or similar
        Pattern pattern = Pattern.compile("On\\s+(.*?),");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private Integer extractInt(String text, String key) {
        if (text == null)
            return null;
        Pattern pattern = Pattern.compile(key + "\\s*(\\d+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    private String extractString(String text, String key) {
        if (text == null)
            return null;
        // Pattern: Key: Value (until newline or end)
        Pattern pattern = Pattern.compile(key + "\\s*(.*)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}
