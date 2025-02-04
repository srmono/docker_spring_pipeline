package com.daimler.fms.exception;

public class TruckNotFoundException extends RuntimeException {
    public TruckNotFoundException(String message){
        super(message);
    }
}
