package com.mycompany.app.business_service.service;

import com.mycompany.app.business_service.client.DataServiceClient;
import com.mycompany.app.business_service.dto.InventarioDTO;
import com.mycompany.app.business_service.exceptions.MicroserviceCommunicationException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class InventarioBusinessService {

    private final DataServiceClient dataServiceClient;

    public InventarioBusinessService(DataServiceClient dataServiceClient) {
        this.dataServiceClient = dataServiceClient;
    }

    public List<InventarioDTO> obtenerProductosConStockBajo() {
        try {
            return dataServiceClient.obtenerProductosConStockBajo();
        } catch (FeignException e) {
            log.error("Error al consultar stock bajo en el microservicio de datos", e);
            throw new MicroserviceCommunicationException("Error de comunicación con el servicio de datos", e);
        }
    }
}
