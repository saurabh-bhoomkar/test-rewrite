import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Order(1)
public class RequestBodyFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Create a modified request
        return DataBufferUtils.join(request.getBody())
            .flatMap(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                DataBufferUtils.release(dataBuffer);
                
                String body = new String(bytes, StandardCharsets.UTF_8);
                System.out.println("Request body: " + body);

                // Create a new ServerHttpRequestDecorator
                ServerHttpRequestDecorator decoratedRequest = new ServerHttpRequestDecorator(request) {
                    @Override
                    public Flux<DataBuffer> getBody() {
                        return Flux.just(exchange.getResponse().bufferFactory().wrap(bytes));
                    }
                };

                // Create a new exchange with the decorated request
                ServerWebExchange modifiedExchange = exchange.mutate().request(decoratedRequest).build();

                // Continue the filter chain with the modified exchange
                return chain.filter(modifiedExchange);
            });
    }
}
