package com.example.projecmntserver.Advisor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.projecmntserver.exception.NotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ApiExceptionAdvisor {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleValidatedException(NotFoundException exception) {
        log.error("handleNotFoundException - {}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(exception.getMessage());
    }
}
