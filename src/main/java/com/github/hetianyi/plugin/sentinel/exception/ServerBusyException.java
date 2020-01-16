package com.github.hetianyi.plugin.sentinel.exception;

/**
 * 服务器繁忙异常
 */
public class ServerBusyException extends IllegalStateException {
    public ServerBusyException() {
    }

    public ServerBusyException(String s) {
        super(s);
    }

    public ServerBusyException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerBusyException(Throwable cause) {
        super(cause);
    }
}
