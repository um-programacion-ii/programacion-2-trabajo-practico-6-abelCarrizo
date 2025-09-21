// business-service/src/test/java/com/mycompany/app/business_service/communication/DataServiceCommunicationTest.java
package com.mycompany.app.business_service.communication;

import com.mycompany.app.business_service.BusinessServiceApplication;
import com.mycompany.app.business_service.client.DataServiceClient;
import com.mycompany.app.business_service.dto.CategoriaDTO;
import com.mycompany.app.business_service.dto.InventarioDTO;
import com.mycompany.app.business_service.dto.ProductoDTO;
import com.mycompany.app.business_service.dto.ProductoRequest;
import com.mycompany.app.business_service.exceptions.MicroserviceCommunicationException;
import com.mycompany.app.business_service.exceptions.ProductoNoEncontradoException;
import com.mycompany.app.business_service.service.CategoriaBusinessService;
import com.mycompany.app.business_service.service.InventarioBusinessService;
import com.mycompany.app.business_service.service.ProductoBusinessService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BusinessServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataServiceCommunicationTest {

    static MockWebServer server;

    @Autowired DataServiceClient dataServiceClient;
    @Autowired ProductoBusinessService productoBusinessService;
    @Autowired CategoriaBusinessService categoriaBusinessService;
    @Autowired InventarioBusinessService inventarioBusinessService;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) throws IOException {
        server = new MockWebServer();
        server.start();
        // Exponer la URL del servidor falso al Feign Client
        r.add("data.service.url", () -> server.url("/").toString());
    }

    @AfterAll
    void shutdown() throws IOException {
        server.shutdown();
    }

    // -------------------------
    // Productos
    // -------------------------

    @Test
    @DisplayName("GET /data/productos/{id} → 200 (Feign devuelve DTO correcto)")
    void obtenerProductoPorId_ok() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type","application/json")
                .setBody("""
                    {
                      "id": 1,
                      "nombre": "Mate Imperial",
                      "descripcion": "Calabaza forrada",
                      "precio": 12000.50,
                      "categoriaNombre": "Bazar",
                      "stock": 8,
                      "stockBajo": false
                    }
                """));

        ProductoDTO dto = dataServiceClient.obtenerProductoPorId(1L);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getNombre()).isEqualTo("Mate Imperial");
        assertThat(dto.getCategoriaNombre()).isEqualTo("Bazar");
        assertThat(dto.getStockBajo()).isFalse();
    }

    @Test
    @DisplayName("GET /data/productos/{id} → 404 (ErrorDecoder → ProductoNoEncontradoException)")
    void obtenerProductoPorId_404() {
        server.enqueue(new MockResponse().setResponseCode(404));
        assertThrows(ProductoNoEncontradoException.class,
                () -> dataServiceClient.obtenerProductoPorId(99L));
    }

    @Test
    @DisplayName("GET /data/productos/{id} con delay > readTimeout → timeout (vía capa de negocio)")
    void obtenerProductoPorId_timeout() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type","application/json")
                .setBody("""
                    {
                      "id": 2,
                      "nombre": "Termo",
                      "descripcion": "Acero",
                      "precio": 20000,
                      "categoriaNombre": "Bazar",
                      "stock": 3,
                      "stockBajo": false
                    }
                """)
                .setBodyDelay(4, TimeUnit.SECONDS)); // mayor al readTimeout de Feign

        assertThrows(MicroserviceCommunicationException.class,
                () -> productoBusinessService.obtenerProductoPorId(2L));
    }

    @Test
    @DisplayName("POST /data/productos → 201 (creación OK)")
    void crearProducto_201() {
        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Content-Type","application/json")
                .setBody("""
                    {
                      "id": 5,
                      "nombre": "Mouse",
                      "descripcion": "Óptico",
                      "precio": 50.0,
                      "categoriaNombre": "Periféricos",
                      "stock": 3,
                      "stockBajo": false
                    }
                """));

        // Constructor correcto: (nombre, descripcion, precio, categoriaId, stock)
        var reqBody = new ProductoRequest("Mouse", "Óptico", new BigDecimal("50.0"), 1L, 3);
        ProductoDTO creado = dataServiceClient.crearProducto(reqBody);

        assertEquals(5L, creado.getId());
        assertEquals("Mouse", creado.getNombre());
    }

    @Test
    @DisplayName("POST /data/productos → 400 (Error del cliente → MicroserviceCommunicationException)")
    void crearProducto_400() {
        server.enqueue(new MockResponse()
                .setResponseCode(400)
                .addHeader("Content-Type","application/json")
                .setBody("{\"message\":\"precio inválido\"}"));

        var reqBody = new ProductoRequest("X", "Sin desc", new BigDecimal("-1"), 1L, 0);
        assertThrows(MicroserviceCommunicationException.class,
                () -> dataServiceClient.crearProducto(reqBody));
    }

    @Test
    @DisplayName("PUT /data/productos/{id} → 200 (actualización OK)")
    void actualizarProducto_ok() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type","application/json")
                .setBody("""
                    {
                      "id": 1,
                      "nombre": "Actualizado",
                      "descripcion": "Desc",
                      "precio": 10,
                      "categoriaNombre": "General",
                      "stock": 1,
                      "stockBajo": false
                    }
                """));

        var reqBody = new ProductoRequest("Actualizado", "Desc", new BigDecimal("10"), 1L, 1);
        ProductoDTO out = dataServiceClient.actualizarProducto(1L, reqBody);

        assertEquals("Actualizado", out.getNombre());
        assertEquals(1L, out.getId());
    }

    @Test
    @DisplayName("DELETE /data/productos/{id} → 204 (eliminación OK)")
    void eliminarProducto_noContent() {
        server.enqueue(new MockResponse().setResponseCode(204));
        assertDoesNotThrow(() -> dataServiceClient.eliminarProducto(1L));
        // sin asserts sobre la RecordedRequest (evitamos flake por cola compartida)
    }

    @Test
    @DisplayName("GET /data/productos/categoria/{nombre} → 200 (lista)")
    void obtenerProductosPorCategoria_ok() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type","application/json")
                .setBody("""
                    [
                      {
                        "id": 1,
                        "nombre": "Teclado",
                        "descripcion": "Mecánico",
                        "precio": 100.5,
                        "categoriaNombre": "Periféricos",
                        "stock": 10,
                        "stockBajo": false
                      }
                    ]
                """));

        List<ProductoDTO> lista = dataServiceClient.obtenerProductosPorCategoria("Perifericos");
        assertThat(lista).hasSize(1);
        assertThat(lista.getFirst().getNombre()).isEqualTo("Teclado"); // Java 21
    }

    // -------------------------
    // Categorías
    // -------------------------

    @Test
    @DisplayName("GET /data/categorias → 200 (lista de categorías)")
    void obtenerTodasLasCategorias_ok() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type","application/json")
                .setBody("""
                    [
                      {"id":1,"nombre":"Electro","descripcion":"Electrodomésticos"},
                      {"id":2,"nombre":"Bazar","descripcion":"Hogar y cocina"}
                    ]
                """));

        List<CategoriaDTO> categorias = dataServiceClient.obtenerTodasLasCategorias();
        assertThat(categorias).hasSize(2);
        assertThat(categorias.getFirst().getNombre()).isEqualTo("Electro");
    }

    @Test
    @DisplayName("GET /data/categorias → 503 (vía capa de negocio → MicroserviceCommunicationException)")
    void obtenerTodasLasCategorias_503() {
        server.enqueue(new MockResponse().setResponseCode(503));
        assertThrows(MicroserviceCommunicationException.class,
                () -> categoriaBusinessService.obtenerTodasLasCategorias());
    }

    // -------------------------
    // Inventario
    // -------------------------

    @Test
    @DisplayName("GET /data/inventario/stock-bajo → 200 (lista)")
    void obtenerProductosConStockBajo_ok() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type","application/json")
                .setBody("""
                    [
                      {"productoId":5,"cantidad":2,"stockMinimo":5,"fechaActualizacion":"2025-09-20T15:00:00Z"}
                    ]
                """));

        List<InventarioDTO> out = dataServiceClient.obtenerProductosConStockBajo();
        assertThat(out).hasSize(1);
        assertThat(out.getFirst().getProductoId()).isEqualTo(5L);
        assertThat(out.getFirst().getCantidad()).isEqualTo(2);
        assertThat(out.getFirst().getStockMinimo()).isEqualTo(5);
    }

    @Test
    @DisplayName("GET /data/inventario/stock-bajo → 502 (vía capa de negocio → MicroserviceCommunicationException)")
    void obtenerProductosConStockBajo_502() {
        server.enqueue(new MockResponse().setResponseCode(502));
        assertThrows(MicroserviceCommunicationException.class,
                () -> inventarioBusinessService.obtenerProductosConStockBajo());
    }
}
