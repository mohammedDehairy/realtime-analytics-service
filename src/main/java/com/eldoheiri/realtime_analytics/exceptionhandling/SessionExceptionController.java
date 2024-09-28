package com.eldoheiri.realtime_analytics.exceptionhandling;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.eldoheiri.realtime_analytics.exceptionhandling.Exceptions.session.SessionException;

@ControllerAdvice
public class SessionExceptionController {
    @ExceptionHandler(value = SessionException.class)
    public ResponseEntity<Object> exception(SessionException exception) {
        return new ResponseEntity<>("{ \"errorMessage\": \"Failed to record heart beat\", \"errorCode\": \"500\" }", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
