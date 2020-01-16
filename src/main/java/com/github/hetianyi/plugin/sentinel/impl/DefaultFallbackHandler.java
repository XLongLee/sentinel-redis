package com.github.hetianyi.plugin.sentinel.impl;

import com.github.hetianyi.plugin.sentinel.FallbackHandler;

/**
 *
 */
public class DefaultFallbackHandler implements FallbackHandler {
    @Override
    public Object fallback(Throwable throwable) throws Throwable {
        throw throwable;
    }
}
