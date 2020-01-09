package com.auxxs.base.limiter.exception;

/**
 * 请求阻断异常
 */
public class BlockException extends IllegalStateException {
    public BlockException() {
    }

    public BlockException(String s) {
        super(s);
    }

    public BlockException(String message, Throwable cause) {
        super(message, cause);
    }

    public BlockException(Throwable cause) {
        super(cause);
    }
}
