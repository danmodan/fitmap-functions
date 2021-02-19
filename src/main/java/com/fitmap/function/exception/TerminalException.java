package com.fitmap.function.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class TerminalException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -2015803516439943404L;

    private final HttpStatus status;

    public TerminalException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public TerminalException(String message, Throwable t, HttpStatus status) {
        super(message, t);
        this.status = status;
    }
}
