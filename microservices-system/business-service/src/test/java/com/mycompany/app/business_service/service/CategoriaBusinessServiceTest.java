package com.mycompany.app.business_service.service;

import com.mycompany.app.business_service.client.DataServiceClient;
import com.mycompany.app.business_service.dto.CategoriaDTO;
import com.mycompany.app.business_service.dto.ProductoDTO;
import com.mycompany.app.business_service.exceptions.MicroserviceCommunicationException;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaBusinessServiceTest {

    @Mock
    private DataServiceClient dataServiceClient;

    @InjectMocks
    private CategoriaBusinessService categoriaBusinessService;

    private FeignException feignStatus(int status) {
        Request req = Request.create(Request.HttpMethod.GET, "/fake", Map.of(), null, Charset.defaultCharset(), null);
        Response res = Response.builder().status(status).reason("r").request(req).build();
        return FeignException.errorStatus("mk", res);
    }

    @Test
    void cuandoObtenerTodasLasCategorias_entoncesRetornaLista() {
        CategoriaDTO c1 = new CategoriaDTO(1L, "Electrónica", "Desc");
        CategoriaDTO c2 = new CategoriaDTO(2L, "Hogar", "Desc");

        when(dataServiceClient.obtenerTodasLasCategorias()).thenReturn(List.of(c1, c2));

        List<CategoriaDTO> out = categoriaBusinessService.obtenerTodasLasCategorias();

        assertEquals(2, out.size());
        verify(dataServiceClient).obtenerTodasLasCategorias();
    }

    @Test
    void cuandoObtenerTodasLasCategorias_yFallaFeign_entoncesMicroserviceCommunicationException() {
        when(dataServiceClient.obtenerTodasLasCategorias()).thenThrow(feignStatus(503));

        assertThrows(MicroserviceCommunicationException.class,
                () -> categoriaBusinessService.obtenerTodasLasCategorias());
    }

    @Test
    void cuandoObtenerProductosPorCategoria_conNombreVacio_entoncesIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> categoriaBusinessService.obtenerProductosPorCategoria("   "));
        verifyNoInteractions(dataServiceClient);
    }

    @Test
    void cuandoObtenerProductosPorCategoria_entoncesRetornaLista() {
        String nombre = "Electrónica";
        ProductoDTO p = new ProductoDTO(1L, "TV", "LED", BigDecimal.valueOf(1000), nombre, 3, true);

        when(dataServiceClient.obtenerProductosPorCategoria(nombre)).thenReturn(List.of(p));

        List<ProductoDTO> out = categoriaBusinessService.obtenerProductosPorCategoria(nombre);

        assertEquals(1, out.size());
        assertEquals("TV", out.get(0).getNombre());
        verify(dataServiceClient).obtenerProductosPorCategoria(nombre);
    }

    @Test
    void cuandoObtenerProductosPorCategoria_yFallaFeign_entoncesMicroserviceCommunicationException() {
        when(dataServiceClient.obtenerProductosPorCategoria("Electro")).thenThrow(feignStatus(502));

        assertThrows(MicroserviceCommunicationException.class,
                () -> categoriaBusinessService.obtenerProductosPorCategoria("Electro"));
    }
}
