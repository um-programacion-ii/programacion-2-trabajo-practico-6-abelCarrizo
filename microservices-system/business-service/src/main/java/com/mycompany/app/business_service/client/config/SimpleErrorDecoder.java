package com.mycompany.app.business_service.client.config;

import com.mycompany.app.business_service.exceptions.MicroserviceCommunicationException;
import com.mycompany.app.business_service.exceptions.ProductoNoEncontradoException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class SimpleErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        return switch (status) {
            case 404 -> new ProductoNoEncontradoException("Recurso no encontrado en data-service");
            case 400, 409 -> new MicroserviceCommunicationException("Error del cliente al invocar data-service");
            case 502, 503, 504 -> new MicroserviceCommunicationException("data-service no disponible");
            default -> defaultDecoder.decode(methodKey, response);
        };
    }
}
