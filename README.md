hhbb
package com.example.xmlparser.service;

import org.springframework.stereotype.Service;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.StartElement;
import java.io.InputStream;

@Service
public class XmlParsingService {

    public String parseTags(InputStream xmlInputStream) throws Exception {
        String tagAValue = null;
        String tagBValue = null;
        String tagCValue = null;

        boolean insideTagParent1 = false;
        boolean insideTagParent2 = false;

        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = factory.createXMLEventReader(xmlInputStream);

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();

            if (event.getEventType() == XMLStreamConstants.START_ELEMENT) {
                StartElement startElement = event.asStartElement();
                String elementName = startElement.getName().getLocalPart();

                if ("TagParent1".equals(elementName)) {
                    insideTagParent1 = true;
                } else if (insideTagParent1 && "TagParent2".equals(elementName)) {
                    insideTagParent2 = true;
                } else if (insideTagParent2) {
                    if ("TagA".equals(elementName)) {
                        event = eventReader.nextEvent();
                        tagAValue = event.asCharacters().getData();
                    } else if ("TagB".equals(elementName)) {
                        event = eventReader.nextEvent();
                        tagBValue = event.asCharacters().getData();
                    } else if ("TagC".equals(elementName)) {
                        event = eventReader.nextEvent();
                        tagCValue = event.asCharacters().getData();
                    }
                }
            } else if (event.getEventType() == XMLStreamConstants.END_ELEMENT) {
                String elementName = event.asEndElement().getName().getLocalPart();
                
                if ("TagParent1".equals(elementName)) {
                    insideTagParent1 = false;
                } else if ("TagParent2".equals(elementName)) {
                    insideTagParent2 = false;
                }
            }

            // Early exit if all tags are found
            if (tagAValue != null && tagBValue != null && tagCValue != null) {
                break;
            }
        }

        eventReader.close();

        if (tagAValue == null || tagBValue == null || tagCValue == null) {
            throw new IllegalArgumentException("Required tags not found in the XML payload");
        }

        return tagAValue + tagBValue + tagCValue;
    }
}
