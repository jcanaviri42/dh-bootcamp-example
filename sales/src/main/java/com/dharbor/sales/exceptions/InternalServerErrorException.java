package com.dharbor.sales.exceptions;

import org.springframework.http.HttpStatus;

public class InternalServerErrorException extends RuntimeException {
    private final HttpStatus status;

    public InternalServerErrorException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
