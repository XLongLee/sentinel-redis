package com.github.hetianyi.plugin.rrl;

import com.github.hetianyi.plugin.rrl.annotation.LimitResource;
import com.github.hetianyi.plugin.rrl.config.LimitResourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 *
 * @author Jason He
 * @version 1.0.0
 * @date 2020-01-06
 */
@Configuration
@EnableConfigurationProperties(LimitResourceProperties.class)
@Slf4j
@Order(1)
@Aspect
public class RateLimiterAutoConfiguration {

    @Autowired
    private AspectResolver aspectResolver;

    @Around("@annotation(limitResource)")
    public Object input(ProceedingJoinPoint joinPoint, LimitResource limitResource) throws Throwable {
        return aspectResolver.resolve(joinPoint, limitResource);
    }
}
