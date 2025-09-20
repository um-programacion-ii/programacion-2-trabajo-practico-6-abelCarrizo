package com.mycompany.app.business_service.exceptions;

public class MicroserviceCommunicationException extends RuntimeException {
    public MicroserviceCommunicationException(String message) { super(message); }
    public MicroserviceCommunicationException(String message, Throwable cause) { super(message, cause); }
}
