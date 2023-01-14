package com.example.fullstackbookreview.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Getter
@Setter
public class ErrorResponse {
    private final int status;
    private final String message;
    private String stackTrace;
    private List<ValidationError> errors;

    private record ValidationError(String field, String message) { }

    public void addValidationError(String field, String message) {
        if (Objects.isNull(errors)) this.errors = new ArrayList<>();
        this.errors.add(new ValidationError(field, message));
    }
}
