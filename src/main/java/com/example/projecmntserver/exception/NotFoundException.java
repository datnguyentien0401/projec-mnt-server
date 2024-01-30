package com.example.projecmntserver.exception;

import java.io.Serial;

public class NotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 8131464301985658650L;

    public NotFoundException(String message) {
        super(message);
    }

}

