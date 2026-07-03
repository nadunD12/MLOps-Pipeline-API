package com.mlops.exceptions;

public class LinkedWorkspaceNotFoundException extends RuntimeException {
    public LinkedWorkspaceNotFoundException(String message) {
        super(message);
    }
}
