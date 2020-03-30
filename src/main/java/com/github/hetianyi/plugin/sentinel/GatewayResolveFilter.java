package com.github.hetianyi.plugin.sentinel;

import com.github.hetianyi.common.util.StringUtil;
import com.github.hetianyi.plugin.sentinel.config.AnnotatedLimitResourceProperties;
import com.github.hetianyi.plugin.sentinel.config.LimitResourceProperties;
import com.github.hetianyi.plugin.sentinel.exception.DowngradeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GatewayResolveFilter extends GenericFilterBean {

    /**
     * universal config properties
     */
    @Autowired
    private LimitResourceProperties properties;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!properties.isGateway()) {
            chain.doFilter(request, response);
            return;
        }

        String limitKey = "";
        log.debug("limit key: {}", limitKey);

        AnnotatedLimitResourceProperties props = exchangeProperties(limitKey, limitResource);

        // 尝试获取ticket
        Long ticketState = redisTemplate.execute(scriptClientLimiter,
                // 这里的key数组必须是同一个节点上的
                Arrays.asList(limitKey),
                String.valueOf(props.getPeriod()), //
                String.valueOf(props.getCount()),  //
                "1",                                           // 获取令牌数量
                String.valueOf(props.isEnableDegrade()),  // 是否开启快速失败检查
                props.getDegradeStrategy(),               // 降级策略
                props.getDegradeValue(),                  // 降级策略值
                String.valueOf(props.getDegradeWindow())  // 降级窗口时间
        );

        // 0: 成功
        // 1: 已被限制
        // 2:
        // 3: 快速失败
        if (ticketState == 3L) {
            // 在快速失败期内，执行快速失败操作
            return fallbackHandler.fallback(new DowngradeException("Server error"));
        } else if (ticketState == 1L) {
            // 否则，获取ticket失败
            return blockHandler.onBlock();
        } else {
            Throwable e = null;
            long start = System.currentTimeMillis();
            // ticket获取成功
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                e = throwable;
                return fallbackHandler.fallback(throwable);
            } finally {
                if (properties.isEnableDegrade()) {
                    long end = System.currentTimeMillis();
                    // 失败次数+1
                    redisTemplate.execute(scriptIncreaseFailTimes,
                            Arrays.asList(limitKey), (e == null ? "0" : "1"), String.valueOf(end - start));
                }
            }
        }

    }
}
