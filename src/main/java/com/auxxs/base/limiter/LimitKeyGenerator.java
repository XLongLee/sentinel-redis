package com.auxxs.base.limiter;

import javax.servlet.http.HttpServletRequest;

/**
 * 标识客户端key的生成器，用于redis存储时的hash的key。<br/>
 *
 * 通常限流的维度在服务端，以某个URL作为资源对象进行访问频率限制。
 * 但是限制对象也可以是客户端对象，如客户端IP，登录用户等。
 * 总之，根据不同的维度生成不同的key。
 *
 *
 * <pre>
 * Example：
 * <code>
 *
 * // 以URL为对象
 * key = &lt;full request path&gt;
 *
 * // 以客户端IP为对象
 * key = request.getRemoteAddress()
 * or
 * key = user.getUserId()
 * or
 * key = &lt;user login token&gt;
 *
 * // 以URL + 客户端IP为对象进行更细粒度的控制
 * key = &lt;full request path&gt; + '_' + user.getUserId()
 * </code></pre>
 *
 * @author Jason He
 * @version 1.0.0
 * @date 2020-01-06
 */
public interface LimitKeyGenerator {
    String getKey(HttpServletRequest request, String path);
}
