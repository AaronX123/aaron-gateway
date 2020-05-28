package gateway.filter;

import aaron.common.data.common.CacheConstants;
import aaron.common.data.exception.StarterError;
import aaron.common.data.exception.StarterException;
import aaron.common.utils.jwt.JwtUtil;
import aaron.common.utils.jwt.UserPermission;
import gateway.common.GatewayError;
import gateway.common.GatewayException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * @author xiaoyouming
 * @version 1.0
 * @since 2020-04-14
 */
/**
 *读取 yml 文件下的 org.my.jwt
 */
@Component
@ConfigurationProperties("org.my.jwt")
public class ApiGatewayFilter implements GlobalFilter, Ordered {
    @Autowired
    CacheManager cacheManager;

    private String[] skipAuthUrls;
    private Set<String> skipAuthUrlSet;
    private static final String TOKEN = "X-Token";
    /**
     * 登录的url
     */
    private static final String LOGIN_URL = "/auth/login/login";
    private static final String PREFIX = "RESOURCE_";

    public String[] getSkipAuthUrls() {
        return skipAuthUrls;
    }

    public void setSkipAuthUrls(String[] skipAuthUrls) {
        this.skipAuthUrls = skipAuthUrls;
        this.skipAuthUrlSet = new HashSet<>();
        this.skipAuthUrlSet.addAll(Arrays.asList(this.skipAuthUrls));
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String uri = exchange.getRequest().getURI().getPath();
        if (skipAuthUrlSet != null && skipAuthUrlSet.contains(uri)){
            return chain.filter(exchange);
        }
        ServerHttpRequest request = exchange.getRequest();

        if (uri.contains(LOGIN_URL)){
            return chain.filter(exchange);
        }

        String token = request.getHeaders().getFirst(TOKEN);
        if (StringUtils.isEmpty(token)){
            throw new StarterException(StarterError.SYSTEM_ACCESS_INVALID);
        }

        try {
            UserPermission userPermission = JwtUtil.parseJwt(token);
            String userId = String.valueOf(userPermission.getId());
            Cache tokenCache = cacheManager.getCache(CacheConstants.TOKEN);
            Cache.ValueWrapper wrapper = tokenCache.get(userId);
            if (wrapper == null){
                throw new GatewayException(GatewayError.TOKEN_NOT_EXIST);
            }
            String tokenFromCache = (String) wrapper.get();
            if (!tokenFromCache.equals(token)){
                throw new GatewayException(GatewayError.LOGIN_IN_OTHER_PLACE);
            }
            // 续约
            tokenCache.put(userId,token);
            Cache resourceCache = cacheManager.getCache(CacheConstants.RESOURCE_MAP);
            Cache.ValueWrapper resourceWrapper = resourceCache.get(userId);
            if (resourceWrapper != null){
                Map<Object,Object> resourceMap = (Map<Object, Object>) resourceWrapper.get();
                List<String> uriList = new ArrayList<>();
                for (Object value : resourceMap.values()) {
                    uriList.add(value.toString());
                }
                if (uriList.contains(uri)){
                    resourceCache.put(userId,resourceMap);
                    return chain.filter(exchange);
                }
                throw new GatewayException(GatewayError.NO_AUTHORITY);
            }

        } catch (Exception e) {
            if (e instanceof GatewayException){
                throw (GatewayException)e;
            }
            throw new GatewayException(StarterError.SYSTEM_TOKEN_EXPIRED);
        }
        return null;
    }

    @Override
    public int getOrder() {
        return -11;
    }
}
