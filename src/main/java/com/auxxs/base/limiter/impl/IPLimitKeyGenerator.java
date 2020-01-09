package com.auxxs.base.limiter.impl;

import com.auxxs.base.common.util.HeaderUtil;
import com.auxxs.base.limiter.LimitKeyGenerator;

import javax.servlet.http.HttpServletRequest;

/**
 * 基于远程IP的客户端标识生成器，如果应用位于反向代理后面，则需要自定义以进行更精准的获取远程客户端IP。<br/>
 * 此实现是基于nginx的x-forwarded-for头部，需要nginx增加配置：
 * <pre>
 * <code>
 * server {
 *        ...
 *        proxy_set_header Host $host;
 *        proxy_set_header X-Real-IP $remote_addr;
 *        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
 * </code>
 * </pre>
 * 模式：&lt;http_method&gt;_&lt;request_api&gt;_&lt;IP&gt;<br/><br/>
 */
public class IPLimitKeyGenerator implements LimitKeyGenerator {

    /**
     * 基于IP的客户端key生成器
     * @param request
     * @param path
     * @return
     */
    @Override
    public String getKey(HttpServletRequest request, String path) {
        HeaderUtil.IpInfo info = HeaderUtil.parseIpInfo(request);
        String ip;
        if (info.getXForwardedFor().length > 0) {
            ip = info.getXForwardedFor()[0];
        } else if (null != info.getXRealIP()) {
            ip = info.getXRealIP();
        } else {
            ip = info.getRemoteAddress();
        }
        return path + "_" + ip;
    }
}
