package com.mycompany.app.data_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidacionDatosException extends RuntimeException {
    public ValidacionDatosException(String message) {
        super(message);
    }
}