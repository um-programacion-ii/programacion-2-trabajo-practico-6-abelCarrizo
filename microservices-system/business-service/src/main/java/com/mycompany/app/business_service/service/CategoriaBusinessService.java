package com.mycompany.app.business_service.service;

import com.mycompany.app.business_service.client.DataServiceClient;
import com.mycompany.app.business_service.dto.CategoriaDTO;
import com.mycompany.app.business_service.dto.ProductoDTO;
import com.mycompany.app.business_service.exceptions.MicroserviceCommunicationException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CategoriaBusinessService {

    private final DataServiceClient dataServiceClient;

    public CategoriaBusinessService(DataServiceClient dataServiceClient) {
        this.dataServiceClient = dataServiceClient;
    }

    public List<CategoriaDTO> obtenerTodasLasCategorias() {
        try {
            return dataServiceClient.obtenerTodasLasCategorias();
        } catch (FeignException e) {
            log.error("Error al obtener categorías del microservicio de datos", e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos", e);
        }
    }

    public List<ProductoDTO> obtenerProductosPorCategoria(String nombreCategoria) {
        if (nombreCategoria == null || nombreCategoria.trim().isEmpty()) {
            // Validación simple sin crear nuevas excepciones (mantenerlo mínimo)
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }
        try {
            return dataServiceClient.obtenerProductosPorCategoria(nombreCategoria.trim());
        } catch (FeignException e) {
            log.error("Error al obtener productos por categoría en el microservicio de datos", e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos", e);
        }
    }
}

