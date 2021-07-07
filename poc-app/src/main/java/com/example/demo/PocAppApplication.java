package com.example.demo;

import lombok.extern.log4j.Log4j2;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@Log4j2
@SpringBootApplication
public class PocAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(PocAppApplication.class, args);
    }

    @Component
    public class ComponentCustom extends RouteBuilder {

        @Override
        public void configure() {
            from("direct:a1").id("directA1").process(exchange -> {
                String aa = exchange.getIn().getBody(String.class);
                log.info("received = " + aa);
                exchange.getIn().setBody(aa + "-->NewData");
                log.info(" = added new data = ");
            }).log("${body}");

            from("direct:a2")
                    .onException(RuntimeException.class).logNewException("arrrrr").end()
                    .id("directA2").process(new AsyncProcessor() {
                @Override
                public boolean process(Exchange exchange, AsyncCallback callback) {
                    String aa = exchange.getIn().getBody(String.class);
                    log.info("received = " + aa);
                    exchange.getIn().setBody(aa + "-->NewData");
                    log.info(" = added new data = ");
                    log.info(Thread.currentThread().toString());
                    if (1 < 2) {
                        throw new RuntimeException();
                    }
                    callback.done(true);
                    return true;
                }

                @Override
                public CompletableFuture<Exchange> processAsync(Exchange exchange) {
                    return null;
                }

                @Override
                public void process(Exchange exchange) {
                    System.out.println();
                }
            }).log("${body}" + Thread.currentThread());
        }
    }

    @RestController
    public class RC {
        @Autowired
        CamelContext camelContext;

        @GetMapping("/tests")
        public ResponseEntity<String> stringResponseEntity() {
            String res = camelContext.createProducerTemplate().sendBody("direct:a1", ExchangePattern.InOut, "somedata").toString();
            String res2 = camelContext.createProducerTemplate().sendBody("direct:a2", ExchangePattern.InOut, "somedata").toString();
            System.out.println("res2 = " + res2);
            return ResponseEntity.ok(res);
        }
    }

}
