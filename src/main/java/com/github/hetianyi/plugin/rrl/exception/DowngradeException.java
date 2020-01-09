package com.github.hetianyi.plugin.rrl.exception;

/**
 * 降级异常
 */
public class DowngradeException extends IllegalStateException {
    public DowngradeException() {
    }

    public DowngradeException(String s) {
        super(s);
    }

    public DowngradeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DowngradeException(Throwable cause) {
        super(cause);
    }
}
