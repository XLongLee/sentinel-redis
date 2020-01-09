package com.github.hetianyi.plugin.rrl.annotation;

import com.github.hetianyi.plugin.rrl.BlockHandler;
import com.github.hetianyi.plugin.rrl.LimitKeyGenerator;
import com.github.hetianyi.plugin.rrl.FallbackHandler;
import com.github.hetianyi.plugin.rrl.config.LimitResourceProperties;
import com.auxxs.base.limiter.impl.*;
import com.github.hetianyi.plugin.rrl.impl.EmptyBlockHandler;
import com.github.hetianyi.plugin.rrl.impl.EmptyFallbackHandler;
import com.github.hetianyi.plugin.rrl.impl.EmptyLimitKeyGenerator;

import java.lang.annotation.*;

/**
 *
 * @author Jason He
 * @version 1.0.0
 * @date 2020-01-06
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LimitResource {

    /**
     * 限制周期（单位s）
     * @see LimitResourceProperties#getPeriod()
     */
    int period() default -1;

    /**
     * 一个周期内对每个API调用限制总次数 换算成每秒为：count/period， 在服务端会转换为每秒最大调用次数。
     * @see LimitResourceProperties#getCount()
     */
    int count() default -1;

    /**
     * 是否开启降级功能
     * @see LimitResourceProperties#isEnableDegrade()
     */
    boolean[] enableDegrade() default {};
    /**
     * 降级策略
     * RT: 平均响应时间
     * EP: 异常比例
     * EC: 异常数
     * @see LimitResourceProperties#getDegradeStrategy()
     */
    String degradeStrategy() default "";

    /**
     * 降级策略对应的值，根据degradeStrategy的不同，此处对应的含义也不通。
     * RT: 平均响应时间（ms）
     * EP: 异常比例（0.0-1.0）
     * EC: 一分钟内的异常数（int）
     * @see LimitResourceProperties#getDegradeValue()
     */
    String degradeValue() default "";

    /**
     * 降级窗口时间(单位s)
     * @see LimitResourceProperties#getDegradeWindow()
     */
    int degradeWindow() default -1;

    /**
     * 限流处理器
     */
    Class<? extends BlockHandler> blockHandler() default EmptyBlockHandler.class;

    /**
     * fallback处理器
     */
    Class<? extends FallbackHandler> fallbackHandler() default EmptyFallbackHandler.class;

    /**
     * 客户端key生成器
     */
    Class<? extends LimitKeyGenerator> limitKeyGenerator() default EmptyLimitKeyGenerator.class;
}
