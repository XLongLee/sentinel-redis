package com.github.hetianyi.plugin.sentinel.impl;

import com.github.hetianyi.plugin.sentinel.LimitKeyGenerator;

import javax.servlet.http.HttpServletRequest;

public class EmptyLimitKeyGenerator implements LimitKeyGenerator {
    @Override
    public String getKey(HttpServletRequest request, String path) {
        return "";
    }
}
