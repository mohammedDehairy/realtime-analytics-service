package com.eldoheiri.realtime_analytics.exceptionhandling.Exceptions.heartbeat;

public class HeartBeatException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public HeartBeatException(Throwable cause) {
        super(cause);
    }

    public HeartBeatException() {
        super();
    }
}
