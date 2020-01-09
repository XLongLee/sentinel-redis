package com.github.hetianyi.plugin.rrl;

import com.auxxs.base.common.util.StringUtil;
import com.github.hetianyi.plugin.rrl.annotation.LimitResource;
import com.github.hetianyi.plugin.rrl.config.AnnotatedLimitResourceProperties;
import com.github.hetianyi.plugin.rrl.config.LimitResourceProperties;
import com.github.hetianyi.plugin.rrl.exception.DowngradeException;
import com.github.hetianyi.plugin.rrl.impl.DefaultBlockHandler;
import com.github.hetianyi.plugin.rrl.impl.DefaultFallbackHandler;
import com.github.hetianyi.plugin.rrl.impl.EmptyLimitKeyGenerator;
import com.github.hetianyi.plugin.rrl.impl.UrlLimitKeyGenerator;
import com.github.hetianyi.plugin.rrl.util.ConfigPropertiesCheckingUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class AspectResolver {
    /**
     * The default limit key generator
     */
    private Class defaultLimitKeyGeneratorClass;

    /**
     * The default block handler class
     */
    private Class defaultBlockHandlerClass;

    /**
     * The default fallback handler class
     */
    private Class defaultFallbackHandlerClass;

    private AnnotatedLimitResourceProperties blankAnnotatedLimitResourceProperties;

    /**
     * universal config properties
     */
    @Autowired
    private LimitResourceProperties properties;

    /**
     * limit script
     */
    @Autowired
    @Qualifier("scriptLimiter")
    private RedisScript<Long> scriptClientLimiter;

    /**
     * statistic script
     */
    @Autowired
    @Qualifier("scriptIncreaseFailTimes")
    private RedisScript<Boolean> scriptIncreaseFailTimes;

    /**
     * redis client
     */
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * custom block handlers map
     */
    private Map<Class, BlockHandler> blockHandlers = new HashMap<>(2);

    /**
     * custom fallback handlers map
     */
    private Map<Class, FallbackHandler> fallbackHandlers = new HashMap<>(2);

    /**
     * custom limit key generators
     */
    private Map<Class, LimitKeyGenerator> limitKeyGenerators = new HashMap<>(2);

    /**
     * cached annotated properties
     */
    private Map<String, AnnotatedLimitResourceProperties> cachedApiLimitProperties = new HashMap<>(100);


    @PostConstruct
    public void init() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        // 检查配置
        checkConfigProperty();

        // 从配置初始化默认limit key生成器
        LimitKeyGenerator limitKeyGenerator =
                ClassInitializeFactory.initialize(
                        properties.getLimitKeyGenerator(), LimitKeyGenerator.class, UrlLimitKeyGenerator.class);
        limitKeyGenerators.put(limitKeyGenerator.getClass(), limitKeyGenerator);
        defaultLimitKeyGeneratorClass = limitKeyGenerator.getClass();
        log.info("defaultLimitKeyGeneratorClass = {}", defaultLimitKeyGeneratorClass.getName());

        // 从配置初始化默认的block handler
        BlockHandler blockHandler =
                ClassInitializeFactory.initialize(
                        properties.getBlockHandler(), BlockHandler.class, DefaultBlockHandler.class);
        blockHandlers.put(blockHandler.getClass(), blockHandler);
        defaultBlockHandlerClass = blockHandler.getClass();
        log.info("defaultBlockHandlerClass = {}", defaultBlockHandlerClass.getName());

        // 从配置初始化默认的fallback handler
        FallbackHandler fallbackHandler =
                ClassInitializeFactory.initialize(
                        properties.getFallbackHandler(), FallbackHandler.class, DefaultFallbackHandler.class);
        fallbackHandlers.put(fallbackHandler.getClass(), fallbackHandler);
        defaultFallbackHandlerClass = fallbackHandler.getClass();
        log.info("defaultFallbackHandlerClass = {}", defaultFallbackHandlerClass.getName());

        /*if (!StringUtil.isNullOrEmptyTrimed(generatorClassName)) {
            synchronized (LimitKeyGenerator.class) {
                Class generatorClass = Class.forName(generatorClassName);
                if (LimitKeyGenerator.class.isAssignableFrom(generatorClass)) {
                    LimitKeyGenerator clientKeyGenerator = (LimitKeyGenerator) generatorClass.newInstance();
                    limitKeyGenerators.put(generatorClass, clientKeyGenerator);
                    defaultLimitKeyGenerator = generatorClass;
                }
            }
        } else {
            defaultLimitKeyGenerator = UrlLimitKeyGenerator.class;
        }*/
        blankAnnotatedLimitResourceProperties = PropertiesFactory.create(
                properties.getPeriod(),
                properties.getCount(),
                properties.getDegradeStrategy(),
                properties.getDegradeValue(),
                properties.getDegradeWindow(),
                properties.isEnableDegrade()
        );
    }

    /**
     * 切面拦截处理
     * @param joinPoint
     * @param limitResource
     * @return
     * @throws Throwable
     */
    public Object resolve(ProceedingJoinPoint joinPoint, LimitResource limitResource) throws Throwable {
        // 获取接口限制处理器
        BlockHandler blockHandler =
                getHandlerInstance(limitResource.blockHandler(), defaultBlockHandlerClass);
        // 获取接口异常处理器
        FallbackHandler fallbackHandler =
                getHandlerInstance(limitResource.fallbackHandler(), defaultFallbackHandlerClass);

        // 如果类没有RequestMapping接口，则跳过 TODO 将来考虑扩展到其他类型的方法
        RequestMapping requestAnnotation = joinPoint.getTarget().getClass().getAnnotation(RequestMapping.class);
        if (null == requestAnnotation) {
            log.debug("limit passed due to: no \"RequestMapping\" annotation found on target class");
            return joinPoint.proceed();
        }

        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest req = servletRequestAttributes.getRequest();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = methodSignature.getMethod();

        // 子路径
        String subMapping = "";

        // 获取接口方法上的Mapping注解，以获取子路径
        Annotation[] methodAnnotations = targetMethod.getAnnotations();
        if (methodAnnotations.length > 0) {
            for (Annotation a : methodAnnotations) {
                Class t = a.annotationType();
                if (t == RequestMapping.class) {
                    subMapping = ((RequestMapping) a).value()[0];
                } else if (t.getAnnotations().length > 0) {
                    for (Annotation sa : t.getAnnotations()) {
                        if (sa.annotationType() == RequestMapping.class) {
                            subMapping = ((RequestMapping) a).value()[0];
                            break;
                        }
                    }
                }
            }
        } else {
            // 没有标注RequestMapping或GetMapping等Mapping跳过
            log.debug("limit passed due to: cannot find any \"Mapping\" annotation on target method");
            return joinPoint.proceed();
        }

        String apiPath = requestAnnotation.value()[0] + subMapping;

        String limitKey = StringUtil.join("{", searchLimitKeyGenerator(limitResource.limitKeyGenerator()).getKey(
                req, StringUtil.join(req.getMethod(), "_", apiPath)), "}");
        log.info("limit key: {}", limitKey);

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

    /**
     * 获取接口处理实例
     * @param tClass
     * @param defaultHandlerClass
     * @param <T>
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private <T> T getHandlerInstance(Class<T> tClass, Class defaultHandlerClass)
            throws IllegalAccessException, InstantiationException {

        if (null == tClass) {
            tClass = defaultHandlerClass;
        }

        if (BlockHandler.class.isAssignableFrom(defaultHandlerClass)) {
            BlockHandler blockHandler = blockHandlers.get(tClass);
            if (null == blockHandler) {
                synchronized (BlockHandler.class) {
                    if (null == blockHandler) {
                        blockHandler = (BlockHandler) tClass.newInstance();
                        blockHandlers.put(tClass, blockHandler);
                    }
                }
            }
            return (T) blockHandler;
        } else {
            FallbackHandler fallbackHandler = fallbackHandlers.get(tClass);
            if (null == fallbackHandler) {
                synchronized (BlockHandler.class) {
                    if (null == fallbackHandler) {
                        fallbackHandler = (FallbackHandler) tClass.newInstance();
                        fallbackHandlers.put(tClass, fallbackHandler);
                    }
                }
            }
            return (T) fallbackHandler;
        }
    }

    /**
     * 获取注解上的LimitKeyGenerator，如果处理失败，则使用默认的LimitKeyGenerator。
     * @param annotatedGenerator
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private LimitKeyGenerator searchLimitKeyGenerator(Class<? extends LimitKeyGenerator> annotatedGenerator)
            throws IllegalAccessException, InstantiationException {
        if (annotatedGenerator == EmptyLimitKeyGenerator.class) {
            annotatedGenerator = defaultLimitKeyGeneratorClass;
        }
        LimitKeyGenerator limitKeyGenerator = limitKeyGenerators.get(annotatedGenerator);
        if (null == limitKeyGenerator) {
            synchronized (LimitKeyGenerator.class) {
                if (null == limitKeyGenerator) {
                    limitKeyGenerator = annotatedGenerator.newInstance();
                    limitKeyGenerators.put(annotatedGenerator, limitKeyGenerator);
                }
            }
        }
        return limitKeyGenerator;
    }

    /**
     * 校验配置
     */
    private void checkConfigProperty() {
        ConfigPropertiesCheckingUtil.checkConfig(properties);
    }

    /**
     * 合并全局默认的配置和注解配置，注解配置优先覆盖默认全局配置。并返回合并后的配置。
     * @param limitResource
     * @return
     */
    private AnnotatedLimitResourceProperties exchangeProperties(String limitKey, LimitResource limitResource) {
        if (cachedApiLimitProperties.containsKey(limitKey)) {
            return cachedApiLimitProperties.get(limitKey);
        }

        // 计算参数
        int period = limitResource.period();
        period = period <= 0 ? properties.getPeriod() : period;
        int count = limitResource.count();
        count = count <= 0 ? properties.getPeriod() : count;
        boolean[] _enableDegrade = limitResource.enableDegrade();
        boolean enableDegrade = _enableDegrade == null || _enableDegrade.length == 0 ?
                properties.isEnableDegrade() : _enableDegrade[0];
        String degradeStrategy = limitResource.degradeStrategy().toUpperCase();
        if (!LimitResourceProperties.isValidDegradeStrategy(degradeStrategy)) {
            degradeStrategy = properties.getDegradeStrategy();
        }
        String degradeValue = limitResource.degradeValue();
        if (!ConfigPropertiesCheckingUtil.isValidDegradeStrategyValue(degradeStrategy, degradeValue)) {
            degradeValue = properties.getDegradeValue();
        }
        int degradeWindow = limitResource.degradeWindow();
        degradeWindow = degradeWindow <= 0 ? properties.getDegradeWindow() : degradeWindow;

        AnnotatedLimitResourceProperties props = PropertiesFactory.create(
                period,
                count,
                degradeStrategy,
                degradeValue,
                degradeWindow,
                enableDegrade
                );
        synchronized (cachedApiLimitProperties) {
            if (!cachedApiLimitProperties.containsKey(limitKey)) {
                cachedApiLimitProperties.put(limitKey, props);
            }
        }
        return props;
    }
}
