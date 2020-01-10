package com.github.hetianyi.plugin.rrl.impl;

import com.github.hetianyi.common.util.StringUtil;
import com.github.hetianyi.plugin.rrl.LimitKeyGenerator;

import javax.servlet.http.HttpServletRequest;

/**
 * 基于session id的客户端标识生成器。<br/>
 * 模式：&lt;http_method&gt;_&lt;request_api&gt;_&lt;SessionId&gt;
 */
public class SessionIdLimitKeyGenerator implements LimitKeyGenerator {
    @Override
    public String getKey(HttpServletRequest request, String path) {
        return path + "_" + StringUtil.trimSafe(request.getRequestedSessionId());
    }
}
