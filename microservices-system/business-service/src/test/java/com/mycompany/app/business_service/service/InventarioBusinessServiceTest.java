package com.mycompany.app.business_service.service;

import com.mycompany.app.business_service.client.DataServiceClient;
import com.mycompany.app.business_service.dto.InventarioDTO;
import com.mycompany.app.business_service.exceptions.MicroserviceCommunicationException;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioBusinessServiceTest {

    @Mock
    private DataServiceClient dataServiceClient;

    @InjectMocks
    private InventarioBusinessService inventarioBusinessService;

    private FeignException feignStatus(int status) {
        Request req = Request.create(Request.HttpMethod.GET, "/fake", Map.of(), null, Charset.defaultCharset(), null);
        Response res = Response.builder().status(status).reason("r").request(req).build();
        return FeignException.errorStatus("mk", res);
    }

    @Test
    void cuandoObtenerProductosConStockBajo_entoncesRetornaListaDeInventario() {
        InventarioDTO item = new InventarioDTO(5L, 2, 5, Instant.now());
        when(dataServiceClient.obtenerProductosConStockBajo()).thenReturn(List.of(item));

        List<InventarioDTO> out = inventarioBusinessService.obtenerProductosConStockBajo();

        assertNotNull(out);
        assertEquals(1, out.size());
        assertEquals(5L, out.get(0).getProductoId());
        verify(dataServiceClient).obtenerProductosConStockBajo();
    }

    @Test
    void cuandoObtenerProductosConStockBajo_yFallaFeign_entoncesMicroserviceCommunicationException() {
        when(dataServiceClient.obtenerProductosConStockBajo()).thenThrow(feignStatus(503));

        assertThrows(MicroserviceCommunicationException.class,
                () -> inventarioBusinessService.obtenerProductosConStockBajo());
    }
}
