package com.fitmap.function.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
@SuppressWarnings("serial")
public class TerminalException extends RuntimeException {

    private final HttpStatus status;

    public TerminalException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}
