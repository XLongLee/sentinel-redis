package com.auxxs.base.limiter;

/**
 * 异常接口的fallback处理方法
 *
 * @author Jason He
 * @version 1.0.0
 * @date 2020-01-06
 */
public interface FallbackHandler {

    /**
     *
     * @return
     */
    Object fallback(Throwable throwable) throws Throwable;
}
