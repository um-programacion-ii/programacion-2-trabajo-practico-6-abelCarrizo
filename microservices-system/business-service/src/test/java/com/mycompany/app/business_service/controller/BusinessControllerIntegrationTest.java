package com.mycompany.app.business_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.app.business_service.dto.CategoriaDTO;
import com.mycompany.app.business_service.dto.InventarioDTO;
import com.mycompany.app.business_service.dto.ProductoDTO;
import com.mycompany.app.business_service.dto.ProductoRequest;
import com.mycompany.app.business_service.exceptions.GlobalExceptionHandler;
import com.mycompany.app.business_service.service.CategoriaBusinessService;
import com.mycompany.app.business_service.service.InventarioBusinessService;
import com.mycompany.app.business_service.service.ProductoBusinessService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BusinessController.class)
@Import(GlobalExceptionHandler.class)
class BusinessControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductoBusinessService productoBusinessService;

    @MockitoBean
    private CategoriaBusinessService categoriaBusinessService;

    @MockitoBean
    private InventarioBusinessService inventarioBusinessService;

    @Test
    @DisplayName("GET /api/productos → 200 y lista de productos")
    void getProductos_ok() throws Exception {
        List<ProductoDTO> lista = List.of(
                new ProductoDTO(1L, "Prod 1", "Desc", BigDecimal.TEN, "Cat 1", 5, false),
                new ProductoDTO(2L, "Prod 2", "Desc", BigDecimal.valueOf(20), "Cat 2", 3, true)
        );
        when(productoBusinessService.obtenerTodosLosProductos()).thenReturn(lista);

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Prod 1"));
    }

    @Test
    @DisplayName("GET /api/productos/{id} con id<1 → 400 por validación @Min")
    void getProducto_idInvalido_badRequest() throws Exception {
        mockMvc.perform(get("/api/productos/0"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(productoBusinessService);
    }

    @Test
    @DisplayName("POST /api/productos → 201 y producto creado")
    void postProducto_created() throws Exception {
        ProductoRequest req = new ProductoRequest("Nuevo", "Desc", BigDecimal.valueOf(99.9), 10L, 7);
        ProductoDTO resp = new ProductoDTO(100L, "Nuevo", "Desc", BigDecimal.valueOf(99.9), "Cat A", 7, false);

        when(productoBusinessService.crearProducto(any(ProductoRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.nombre").value("Nuevo"));
    }

    @Test
    @DisplayName("GET /api/productos/categoria/{nombre} → 200 y lista")
    void getProductosPorCategoria_ok() throws Exception {
        List<ProductoDTO> lista = List.of(
                new ProductoDTO(1L, "TV", "LED", BigDecimal.valueOf(1500), "Electrónica", 2, true)
        );
        when(categoriaBusinessService.obtenerProductosPorCategoria("Electrónica")).thenReturn(lista);

        mockMvc.perform(get("/api/productos/categoria/Electrónica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].categoriaNombre").value("Electrónica"));
    }

    @Test
    @DisplayName("GET /api/productos/categoria/{nombre} con vacío → 400 por @NotBlank")
    void getProductosPorCategoria_nombreVacio_badRequest() throws Exception {
        mockMvc.perform(get("/api/productos/categoria/ "))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(categoriaBusinessService);
    }

    @Test
    @DisplayName("GET /api/categorias → 200 y lista")
    void getCategorias_ok() throws Exception {
        when(categoriaBusinessService.obtenerTodasLasCategorias())
                .thenReturn(List.of(new CategoriaDTO(1L, "Hogar", "desc")));
        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Hogar"));
    }

    @Test
    @DisplayName("GET /api/reportes/stock-bajo → 200 y lista")
    void getStockBajo_ok() throws Exception {
        when(inventarioBusinessService.obtenerProductosConStockBajo())
                .thenReturn(List.of(new InventarioDTO(5L, 2, 5, Instant.now())));

        mockMvc.perform(get("/api/reportes/stock-bajo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].productoId").value(5));
    }
}
