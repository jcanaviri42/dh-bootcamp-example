package com.dharbor.sales.dto;

import org.springframework.http.HttpStatus;

public record ErrorResponse(
        HttpStatus errorCode,
        String message
) {
}
