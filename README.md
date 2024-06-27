import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XsdToBeanioMapping {

  private static final String DEFAULT_NAMESPACE_PREFIX = "xs";

  public static void main(String[] args) throws ParserConfigurationException, IOException, XPathExpressionException {
    if (args.length != 2) {
      System.err.println("Usage: java XsdToBeanioMapping <xsd_file> <mapping_file>");
      System.exit(1);
    }

    String xsdPath = args[0];
    String mappingPath = args[1];

    // Parse XSD schema
    Document doc = parseXsd(xsdPath);

    // Extract namespace (if any)
    String namespace = extractNamespace(doc);

    // Generate mapping content
    String mappingContent = generateBeanioMapping(doc.getDocumentElement(), namespace);

    // Write mapping to file
    writeToFile(mappingContent, mappingPath);

    System.out.println("BeanIO mapping file generated successfully!");
  }

  private static Document parseXsd(String xsdPath) throws ParserConfigurationException, IOException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.parse(new File(xsdPath));
  }

  private static String extractNamespace(Document doc) throws XPathExpressionException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    String expression = "/xsd:schema/@xmlns:" + DEFAULT_NAMESPACE_PREFIX;
    return (String) xpath.evaluate(expression, doc, XPathConstants.STRING);
  }

  private static String generateBeanioMapping(Element rootElement, String namespace) throws XPathExpressionException {
    StringBuilder mapping = new StringBuilder();
    String streamName = rootElement.getAttribute("name");
    mapping.append("<stream name=\"" + streamName + "\" format=\"xml\">\n");

    processElement(rootElement, mapping, namespace, new HashMap<>());
    mapping.append("</stream>\n");

    return mapping.toString();
  }

  private static void processElement(Element element, StringBuilder mapping, String namespace, Map<String, String> typeMap) throws XPathExpressionException {
    String elementName = element.getLocalName();
    if (namespace != null) {
      elementName = namespace + ":" + elementName;
    }

    String elementType = element.getAttribute("type");
    if (elementType != null) {
      // Handle complex type reference
      elementType = typeMap.getOrDefault(elementType, elementType);
      processComplexType(elementType, mapping, namespace, typeMap);
    } else {
      // Handle simple element
      String dataType = element.getAttribute("type", "string");  // Default to string
      mapping.append("\t<field name=\"" + elementName + "\" type=\"" + dataType + "\" />\n");
    }
  }

  private static void processComplexType(String complexTypeName, StringBuilder mapping, String namespace, Map<String, String> typeMap) throws XPathExpressionException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    String expression = "//xsd:complexType[@name='" + complexTypeName + "']";
    NodeList complexTypes = (NodeList) xpath.evaluate(expression, new File(complexTypeName + ".xsd"), XPathConstants.NODESET);

    if (complexTypes.getLength() == 0) {
      // Handle missing complex type definition (log or handle gracefully)
      System.err.println("Warning: Complex type definition not found for: " + complexTypeName);
      return;
    }

    Element complexTypeElement = (Element) complexTypes.item(0);
    typeMap.put(complexTypeName, elementName);  // Add complex type
    
