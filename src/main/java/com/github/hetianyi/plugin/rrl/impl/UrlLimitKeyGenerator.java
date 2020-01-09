package com.github.hetianyi.plugin.rrl.impl;

import com.github.hetianyi.plugin.rrl.LimitKeyGenerator;

import javax.servlet.http.HttpServletRequest;

/**
 * 默认的全局客户端标识生成器，不限制某个具体客户端，而是针对API进行全局限制。<br/>
 * 模式：&lt;http_method&gt;_&lt;request_api&gt;
 */
public class UrlLimitKeyGenerator implements LimitKeyGenerator {
    @Override
    public String getKey(HttpServletRequest request, String path) {
        return path;
    }
}
