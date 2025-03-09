package com.example.projecmntserver.advisor;

import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleValidationException(
            ConstraintViolationException exception) {

        log.warn("handleValidatedException - {}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(
            MethodArgumentNotValidException exception) {

        log.warn("handleValidatedException - {}", exception.getMessage(), exception);

        final String message = exception.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(message);
    }
}
