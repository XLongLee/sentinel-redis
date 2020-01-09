package com.github.hetianyi.plugin.rrl;

/**
 * 异常接口的fallback处理方法
 *
 * @author Jason He
 * @version 1.0.0
 * @date 2020-01-06
 */
public interface FallbackHandler {

    /**
     * 当拦截的方法执行发生非BlockException和DowngradeException等异常时，
     * 则会进入FallbackHandler的fallback回调方法。
     * @return
     */
    Object fallback(Throwable throwable) throws Throwable;
}
