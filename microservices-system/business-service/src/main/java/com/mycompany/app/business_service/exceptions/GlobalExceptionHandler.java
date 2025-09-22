package com.mycompany.app.business_service.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.mycompany.app.business_service")
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        List<Map<String, String>> violations = ex.getConstraintViolations()
                .stream()
                .map(this::toMap)
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("error", "Bad Request");
        body.put("violations", violations);

        return ResponseEntity.badRequest().body(body);
    }

    private Map<String, String> toMap(ConstraintViolation<?> v) {
        Map<String, String> m = new HashMap<>();
        m.put("param", v.getPropertyPath().toString()); // p.ej. obtenerProductoPorId.id
        m.put("message", v.getMessage());               // p.ej. must be greater than or equal to 1
        return m;
    }
}
