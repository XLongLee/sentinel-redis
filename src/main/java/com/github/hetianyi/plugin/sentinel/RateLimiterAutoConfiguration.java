package com.github.hetianyi.plugin.sentinel;

import com.github.hetianyi.plugin.sentinel.annotation.LimitResource;
import com.github.hetianyi.plugin.sentinel.config.LimitResourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletContextInitializerBeans;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.boot.web.servlet.support.ServletContextApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.context.support.ServletContextAwareProcessor;

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


    @Bean
    public FilterRegistrationBean someFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new GatewayResolveFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
}
