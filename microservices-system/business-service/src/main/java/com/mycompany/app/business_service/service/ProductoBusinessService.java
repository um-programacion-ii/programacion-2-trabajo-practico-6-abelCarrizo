package com.mycompany.app.business_service.service;

import com.mycompany.app.business_service.client.DataServiceClient;
import com.mycompany.app.business_service.dto.ProductoDTO;
import com.mycompany.app.business_service.dto.ProductoRequest;
import com.mycompany.app.business_service.exceptions.MicroserviceCommunicationException;
import com.mycompany.app.business_service.exceptions.ProductoNoEncontradoException;
import com.mycompany.app.business_service.exceptions.ValidacionNegocioException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class ProductoBusinessService {

    private final DataServiceClient dataServiceClient;

    public ProductoBusinessService(DataServiceClient dataServiceClient) {
        this.dataServiceClient = dataServiceClient;
    }

    public List<ProductoDTO> obtenerTodosLosProductos() {
        try {
            return dataServiceClient.obtenerTodosLosProductos();
        } catch (FeignException e) {
            log.error("Error al obtener productos del microservicio de datos", e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos", e);
        }
    }

    public ProductoDTO obtenerProductoPorId(Long id) {
        try {
            return dataServiceClient.obtenerProductoPorId(id);
        } catch (FeignException.NotFound e) {
            throw new ProductoNoEncontradoException("Producto no encontrado con ID: " + id);
        } catch (FeignException e) {
            log.error("Error al obtener producto del microservicio de datos", e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos", e);
        }
    }

    public ProductoDTO crearProducto(ProductoRequest request) {
        validarProducto(request);
        try {
            return dataServiceClient.crearProducto(request);
        } catch (FeignException e) {
            log.error("Error al crear producto en el microservicio de datos", e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos", e);
        }
    }

    public ProductoDTO actualizarProducto(Long id, ProductoRequest request) {
        validarProducto(request);
        try {
            return dataServiceClient.actualizarProducto(id, request);
        } catch (FeignException.NotFound e) {
            throw new ProductoNoEncontradoException("Producto no encontrado con ID: " + id);
        } catch (FeignException e) {
            log.error("Error al actualizar producto en el microservicio de datos", e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos", e);
        }
    }

    public void eliminarProducto(Long id) {
        try {
            dataServiceClient.eliminarProducto(id);
        } catch (FeignException.NotFound e) {
            // Podrías considerar la eliminación como idempotente y no fallar.
            throw new ProductoNoEncontradoException("Producto no encontrado con ID: " + id);
        } catch (FeignException e) {
            log.error("Error al eliminar producto en el microservicio de datos", e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos", e);
        }
    }

    private void validarProducto(ProductoRequest request) {
        if (request.getPrecio() == null || request.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacionNegocioException("El precio debe ser mayor a cero");
        }
        if (request.getStock() == null || request.getStock() < 0) {
            throw new ValidacionNegocioException("El stock no puede ser negativo");
        }
    }
}