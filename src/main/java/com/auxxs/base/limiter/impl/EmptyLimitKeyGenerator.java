package com.auxxs.base.limiter.impl;

import com.auxxs.base.limiter.LimitKeyGenerator;

import javax.servlet.http.HttpServletRequest;

public class EmptyLimitKeyGenerator implements LimitKeyGenerator {
    @Override
    public String getKey(HttpServletRequest request, String path) {
        return "";
    }
}
