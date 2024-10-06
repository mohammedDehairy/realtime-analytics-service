package com.eldoheiri.realtime_analytics.exceptionhandling.Exceptions;

public class DeviceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DeviceException(Throwable cause) {
        super(cause);
    }

    public DeviceException() {
        super();
    }
}
