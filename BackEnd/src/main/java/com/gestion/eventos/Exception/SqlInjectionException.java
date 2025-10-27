package com.gestion.eventos.Exception;

public class SqlInjectionException extends RuntimeException {
    public SqlInjectionException(String message) {
        super(message);
    }
}
