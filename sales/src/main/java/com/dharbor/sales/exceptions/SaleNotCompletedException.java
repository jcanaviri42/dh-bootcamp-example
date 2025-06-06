package com.dharbor.sales.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SaleNotCompletedException extends RuntimeException {
    private final HttpStatus status;

    public SaleNotCompletedException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
