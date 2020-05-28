package gateway.filter;

import io.netty.buffer.ByteBufAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 网关日志记录-request
 *
*/
@Component
public class GlobalRequestLogFilter implements GlobalFilter, Ordered {

    private static Logger logger = LoggerFactory.getLogger(GlobalRequestLogFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        logger.info("path: " + request.getPath());
        logger.info("address: " + request.getRemoteAddress());
        logger.info("method: " + request.getMethodValue());
        logger.info("URI: " + request.getURI());
        logger.info("Headers: " + request.getHeaders());
        Object requestBody = exchange.getAttribute("cachedRequestBodyObject");
        logger.info("requestBody: "+ requestBody);
        if ("POST".equals(request.getMethodValue())){
            String bodyStr = resolveBodyFromRequest(request);
            ServerHttpRequest req;
            DataBuffer data = stringBuffer(bodyStr);
            Flux<DataBuffer> flux = Flux.just(data);
            req = new ServerHttpRequestDecorator(request){
                @Override
                public Flux<DataBuffer> getBody() {
                    return flux;
                }
            };
            return chain.filter(exchange.mutate().request(req).build());
        }
        if ("GET".equals(request.getMethodValue())){
            Map requestParams = request.getQueryParams();
        }
        return chain.filter(exchange);
    }


    /**
     * 从Flux<DataBuffer>中获取字符串的方法
     * @return 请求体
     */
    private String resolveBodyFromRequest(ServerHttpRequest serverHttpRequest) {
        //获取请求体
        Flux<DataBuffer> body = serverHttpRequest.getBody();

        AtomicReference<String> bodyRef = new AtomicReference<>();
        body.subscribe(buffer -> {
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer.asByteBuffer());
            DataBufferUtils.release(buffer);
            bodyRef.set(charBuffer.toString());
        });
        //获取request body
        return bodyRef.get();
    }

    private DataBuffer stringBuffer(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
        DataBuffer buffer = nettyDataBufferFactory.allocateBuffer(bytes.length);
        buffer.write(bytes);
        return buffer;
    }


    @Override
    public int getOrder() {
        return -99;
    }
}

