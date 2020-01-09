package com.github.hetianyi.plugin.rrl.impl;

import com.github.hetianyi.plugin.rrl.LimitKeyGenerator;

import javax.servlet.http.HttpServletRequest;

public class EmptyLimitKeyGenerator implements LimitKeyGenerator {
    @Override
    public String getKey(HttpServletRequest request, String path) {
        return "";
    }
}
