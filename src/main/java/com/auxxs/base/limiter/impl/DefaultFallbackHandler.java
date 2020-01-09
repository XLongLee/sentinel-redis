package com.auxxs.base.limiter.impl;

import com.auxxs.base.limiter.FallbackHandler;

/**
 *
 */
public class DefaultFallbackHandler implements FallbackHandler {
    @Override
    public Object fallback(Throwable throwable) throws Throwable {
        throw throwable;
    }
}
