package com.github.hetianyi.plugin.rrl.exception;

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
