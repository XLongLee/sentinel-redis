package com.github.hetianyi.plugin.sentinel.impl;

import com.github.hetianyi.plugin.sentinel.BlockHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

/**
 * 限流阻断默认实现
 *
 * @author Jason He
 * @version 1.0.0
 * @date 2020-01-06
 */
public class DefaultBlockHandler implements BlockHandler {
    @Override
    public Object onBlock() {
        throw HttpClientErrorException.create(HttpStatus.TOO_MANY_REQUESTS,
                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(), null, null, null);
    }
}
