package net.brekker.auth.aspect;

import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.brekker.auth.config.RateLimiterConfig;
import net.brekker.common.exceptions.RateLimitException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

import static java.util.Objects.nonNull;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimiterAspect {

    private final RateLimiterConfig rateLimiterConfig;

    @Around("@annotation(net.brekker.common.annotation.RateLimited)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String clientKey = request.getHeader("clientId");
        if (clientKey == null || clientKey.isBlank()) {
            clientKey = request.getRemoteAddr();
        }

        Bucket bucket = getOrCreateBucket(clientKey);

        if (bucket.tryConsume(1)) {
            return joinPoint.proceed();
        } else {
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();

            if (nonNull(response)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            }

            throw new RateLimitException("Rate limit exceeded");
        }
    }

    private Bucket getOrCreateBucket(String clientKey) {
        return rateLimiterConfig.lettuceBasedProxyManager(rateLimiterConfig.redisClient())
                .builder()
                .build(clientKey, rateLimiterConfig.bucketConfiguration());
    }
}
