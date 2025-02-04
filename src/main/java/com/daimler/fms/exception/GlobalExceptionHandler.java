package com.daimler.fms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TruckNotFoundException.class)
    public ResponseEntity<String> handleTruckNotFoundException(TruckNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ex.getMessage()
        );
    }

    public ResponseEntity<String> handleGeneralException(Exception ex){
        return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
        ).body(
                "An unexpected error occured" + ex.getMessage()
        );
    }
}
