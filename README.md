package com.example.xmlvalidator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.sun.org.apache.xerces.internal.parsers.XMLGrammarPreparser;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator;
import com.sun.org.apache.xerces.internal.xni.XMLString;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class XmlValidatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(XmlValidatorApplication.class, args);
    }

    @Bean
    public RouterFunction<ServerResponse> routes(XmlValidationService xmlValidationService) {
        return route()
            .POST("/validate-xml", request ->
                request.bodyToMono(String.class)
                    .flatMap(xmlValidationService::validateXml)
                    .flatMap(result -> ok().bodyValue(result))
            )
            .build();
    }

    @Bean
    public XMLGrammarPool xmlGrammarPool() throws Exception {
        XMLGrammarPreparser preparser = new XMLGrammarPreparser();
        preparser.registerPreparser(XMLGrammarDescription.XML_SCHEMA, null);
        preparser.setProperty(GRAMMAR_POOL, new XMLGrammarPoolImpl());
        
        XMLInputSource xis = new XMLInputSource(null, getClass().getResource("/pain001.xsd").toString(), null);
        preparser.preparseGrammar(XMLGrammarDescription.XML_SCHEMA, xis);
        
        return (XMLGrammarPool) preparser.getProperty(GRAMMAR_POOL);
    }

    @Bean
    public XMLInputFactory xmlInputFactory() {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_COALESCING, false);
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
        factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        return factory;
    }

    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }
}

@Service
@Slf4j
public class XmlValidationService {

    private final XMLGrammarPool grammarPool;
    private final XMLInputFactory xmlInputFactory;
    private final ExecutorService executorService;

    public XmlValidationService(XMLGrammarPool grammarPool, XMLInputFactory xmlInputFactory, ExecutorService executorService) {
        this.grammarPool = grammarPool;
        this.xmlInputFactory = xmlInputFactory;
        this.executorService = executorService;
    }

    public Mono<String> validateXml(String xmlFilePath) {
        return Mono.fromCallable(() -> {
            try (FileChannel fileChannel = FileChannel.open(Paths.get(xmlFilePath), StandardOpenOption.READ)) {
                ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024); // 1MB buffer
                XMLSchemaValidator validator = new XMLSchemaValidator();
                validator.setGrammarPool(grammarPool);

                XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(new ByteBufferInputStream(buffer));

                while (fileChannel.read(buffer) != -1 || buffer.position() > 0) {
                    buffer.flip();
                    while (reader.hasNext()) {
                        int event = reader.next();
                        switch (event) {
                            case XMLStreamConstants.START_ELEMENT:
                                validator.startElement(reader.getName(), null, null);
                                for (int i = 0; i < reader.getAttributeCount(); i++) {
                                    validator.attribute(reader.getAttributeName(i), null, reader.getAttributeValue(i), null);
                                }
                                break;
                            case XMLStreamConstants.END_ELEMENT:
                                validator.endElement(reader.getName(), null);
                                break;
                            case XMLStreamConstants.CHARACTERS:
                                validator.characters(new XMLString(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength()));
                                break;
                        }
                    }
                    buffer.compact();
                }
                validator.endDocument(null);
                return "XML is valid";
            } catch (Exception e) {
                log.error("XML validation error", e);
                return "XML is invalid: " + e.getMessage();
            }
        }).subscribeOn(Schedulers.fromExecutor(executorService));
    }

    private static class ByteBufferInputStream extends InputStream {
        private final ByteBuffer buf;
        public ByteBufferInputStream(ByteBuffer buf) {
            this.buf = buf;
        }
        public int read() {
            if (!buf.hasRemaining()) {
                return -1;
            }
            return buf.get() & 0xFF;
        }
        public int read(byte[] bytes, int off, int len) {
            if (!buf.hasRemaining()) {
                return -1;
            }
            len = Math.min(len, buf.remaining());
            buf.get(bytes, off, len);
            return len;
        }
    }
}
