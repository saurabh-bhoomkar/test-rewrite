gsjsjsnbed
package com.example.xmlparser.service;

import org.springframework.stereotype.Service;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLEventFactory;
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

        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = factory.createXMLEventReader(xmlInputStream);

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();

            if (event.getEventType() == XMLStreamConstants.START_ELEMENT) {
                StartElement startElement = event.asStartElement();
                String elementName = startElement.getName().getLocalPart();

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
