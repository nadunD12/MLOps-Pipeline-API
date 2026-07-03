package com.mlops.exceptions;

public class ModelDeprecatedException extends RuntimeException {
    public ModelDeprecatedException(String message) {
        super(message);
    }
}
