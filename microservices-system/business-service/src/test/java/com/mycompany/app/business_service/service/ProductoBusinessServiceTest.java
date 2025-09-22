package com.mycompany.app.business_service.service;

import com.mycompany.app.business_service.client.DataServiceClient;
import com.mycompany.app.business_service.dto.ProductoDTO;
import com.mycompany.app.business_service.dto.ProductoRequest;
import com.mycompany.app.business_service.exceptions.MicroserviceCommunicationException;
import com.mycompany.app.business_service.exceptions.ProductoNoEncontradoException;
import com.mycompany.app.business_service.exceptions.ValidacionNegocioException;
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
class ProductoBusinessServiceTest {

    @Mock
    private DataServiceClient dataServiceClient;

    @InjectMocks
    private ProductoBusinessService productoBusinessService;

    // Helper para crear FeignException con un status dado
    private FeignException feignStatus(int status) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/fake",
                Map.of(),
                null,
                Charset.defaultCharset(),
                null
        );
        Response response = Response.builder()
                .status(status)
                .reason("reason")
                .request(request)
                .build();
        return FeignException.errorStatus("methodKey", response);
    }

    @Test
    void cuandoObtenerTodosLosProductos_entoncesRetornaLista() {
        ProductoDTO p1 = new ProductoDTO(1L, "Prod 1", "Desc 1", BigDecimal.valueOf(10), "Cat 1", 5, false);
        ProductoDTO p2 = new ProductoDTO(2L, "Prod 2", "Desc 2", BigDecimal.valueOf(20), "Cat 2", 3, true);

        when(dataServiceClient.obtenerTodosLosProductos()).thenReturn(List.of(p1, p2));

        List<ProductoDTO> out = productoBusinessService.obtenerTodosLosProductos();

        assertNotNull(out);
        assertEquals(2, out.size());
        assertEquals("Prod 1", out.get(0).getNombre());
        verify(dataServiceClient).obtenerTodosLosProductos();
    }

    @Test
    void cuandoObtenerTodosLosProductos_yFallaFeign_entoncesLanzaMicroserviceCommunicationException() {
        when(dataServiceClient.obtenerTodosLosProductos()).thenThrow(feignStatus(503));

        assertThrows(MicroserviceCommunicationException.class,
                () -> productoBusinessService.obtenerTodosLosProductos());
    }

    @Test
    void cuandoObtenerProductoPorId404_entoncesLanzaProductoNoEncontrado() {
        Long id = 999L;
        when(dataServiceClient.obtenerProductoPorId(id)).thenThrow(feignStatus(404));

        assertThrows(ProductoNoEncontradoException.class,
                () -> productoBusinessService.obtenerProductoPorId(id));
        verify(dataServiceClient).obtenerProductoPorId(id);
    }

    @Test
    void cuandoObtenerProductoPorIdErrorServidor_entoncesLanzaMicroserviceCommunicationException() {
        Long id = 7L;
        when(dataServiceClient.obtenerProductoPorId(id)).thenThrow(feignStatus(502));

        assertThrows(MicroserviceCommunicationException.class,
                () -> productoBusinessService.obtenerProductoPorId(id));
    }

    @Test
    void cuandoCrearProductoConPrecioInvalido_entoncesValidacionNegocio() {
        ProductoRequest req = new ProductoRequest("P", "D", BigDecimal.valueOf(0), 1L, 1);
        assertThrows(ValidacionNegocioException.class, () -> productoBusinessService.crearProducto(req));
        verify(dataServiceClient, never()).crearProducto(any());
    }

    @Test
    void cuandoCrearProductoConStockNegativo_entoncesValidacionNegocio() {
        ProductoRequest req = new ProductoRequest("P", "D", BigDecimal.valueOf(100), 1L, -1);
        assertThrows(ValidacionNegocioException.class, () -> productoBusinessService.crearProducto(req));
        verify(dataServiceClient, never()).crearProducto(any());
    }

    @Test
    void cuandoCrearProductoValido_entoncesDelegayDevuelveDTO() {
        ProductoRequest req = new ProductoRequest("Prod OK", "Desc", BigDecimal.valueOf(123.45), 10L, 7);
        ProductoDTO creado = new ProductoDTO(100L, "Prod OK", "Desc", BigDecimal.valueOf(123.45), "Cat A", 7, false);

        when(dataServiceClient.crearProducto(req)).thenReturn(creado);

        ProductoDTO out = productoBusinessService.crearProducto(req);

        assertNotNull(out);
        assertEquals(100L, out.getId());
        assertEquals("Prod OK", out.getNombre());
        verify(dataServiceClient).crearProducto(req);
    }

    @Test
    void cuandoCrearProducto_yFallaFeign_entoncesMicroserviceCommunicationException() {
        ProductoRequest req = new ProductoRequest("Prod", "Desc", BigDecimal.valueOf(10), 1L, 1);
        when(dataServiceClient.crearProducto(req)).thenThrow(feignStatus(503));

        assertThrows(MicroserviceCommunicationException.class,
                () -> productoBusinessService.crearProducto(req));
    }

    @Test
    void cuandoActualizarProductoValido_entoncesDelegayDevuelveDTO() {
        Long id = 5L;
        ProductoRequest req = new ProductoRequest("Nuevo", "Desc", BigDecimal.valueOf(50), 2L, 3);
        ProductoDTO actualizado = new ProductoDTO(id, "Nuevo", "Desc", BigDecimal.valueOf(50), "Cat B", 3, false);

        when(dataServiceClient.actualizarProducto(id, req)).thenReturn(actualizado);

        ProductoDTO out = productoBusinessService.actualizarProducto(id, req);

        assertEquals(id, out.getId());
        assertEquals("Nuevo", out.getNombre());
        verify(dataServiceClient).actualizarProducto(id, req);
    }

    @Test
    void cuandoActualizarProducto404_entoncesProductoNoEncontrado() {
        Long id = 77L;
        ProductoRequest req = new ProductoRequest("X", "Y", BigDecimal.valueOf(10), 1L, 1);
        when(dataServiceClient.actualizarProducto(id, req)).thenThrow(feignStatus(404));

        assertThrows(ProductoNoEncontradoException.class,
                () -> productoBusinessService.actualizarProducto(id, req));
    }

    @Test
    void cuandoEliminarProducto404_entoncesProductoNoEncontrado() {
        Long id = 9L;
        doThrow(feignStatus(404)).when(dataServiceClient).eliminarProducto(id);

        assertThrows(ProductoNoEncontradoException.class,
                () -> productoBusinessService.eliminarProducto(id));
        verify(dataServiceClient).eliminarProducto(id);
    }
}

