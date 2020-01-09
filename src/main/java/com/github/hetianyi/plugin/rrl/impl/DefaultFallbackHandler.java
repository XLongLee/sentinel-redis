package com.github.hetianyi.plugin.rrl.impl;

import com.github.hetianyi.plugin.rrl.FallbackHandler;

/**
 *
 */
public class DefaultFallbackHandler implements FallbackHandler {
    @Override
    public Object fallback(Throwable throwable) throws Throwable {
        throw throwable;
    }
}
