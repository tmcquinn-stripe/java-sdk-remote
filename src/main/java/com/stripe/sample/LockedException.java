package com.stripe.sample;

public class LockedException extends RuntimeException {
    public LockedException() {
        super("Terminal Instance is currently locked.");
    }

    public LockedException(String message) {
        super(message);
    }

    public LockedException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockedException(Throwable cause) {
        super(cause);
    }
}
