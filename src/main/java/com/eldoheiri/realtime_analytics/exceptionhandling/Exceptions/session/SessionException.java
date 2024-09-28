package com.eldoheiri.realtime_analytics.exceptionhandling.Exceptions.session;

public class SessionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SessionException(Throwable cause) {
        super(cause);
    }

    public SessionException() {
        super();
    }
}
