package com.mycompany.app.business_service.client;

import com.mycompany.app.business_service.BusinessServiceApplication;
import com.mycompany.app.business_service.dto.ProductoDTO;
import com.mycompany.app.business_service.dto.ProductoRequest;
import com.mycompany.app.business_service.exceptions.MicroserviceCommunicationException;
import com.mycompany.app.business_service.exceptions.ProductoNoEncontradoException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BusinessServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataServiceClientTest {

    static MockWebServer server;

    @Autowired
    DataServiceClient dataServiceClient;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) throws IOException {
        server = new MockWebServer();
        server.start();
        r.add("data.service.url", () -> server.url("/").toString());
    }

    @AfterAll
    void shutdown() throws IOException {
        server.shutdown();
    }

    // ---------------- Helpers SIN advertencias ----------------

    @BeforeEach
    void clearQueue() throws InterruptedException {
        // Drena cualquier request previa antes de cada test
        RecordedRequest req = server.takeRequest(50, TimeUnit.MILLISECONDS);
        while (req != null) {
            // consumida intencionalmente
            req = server.takeRequest(50, TimeUnit.MILLISECONDS);
        }
    }

    private RecordedRequest awaitRequest(String expectedMethod, String expectedPath) throws InterruptedException {
        // Espera hasta 2s leyendo la cola; devuelve la que matchee método y path
        long deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        RecordedRequest match = null;
        while (System.nanoTime() < deadlineNanos) {
            RecordedRequest r = server.takeRequest(200, TimeUnit.MILLISECONDS);
            if (r == null) continue;
            if (expectedMethod.equals(r.getMethod()) && expectedPath.equals(r.getPath())) {
                match = r;
                break;
            }
            // si no matchea, sigue buscando
        }
        return match;
    }

    // ---------------- GET /data/productos/{id} ----------------

    @Test
    @DisplayName("Feign: GET /data/productos/{id} → 200 mapea DTO y usa path correcto")
    void getProductoPorId_ok() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type","application/json")
                .setBody("""
                    {"id":1,"nombre":"Mate","descripcion":"Calabaza","precio":100.0,"categoriaNombre":"Bazar","stock":5,"stockBajo":false}
                """));

        ProductoDTO dto = dataServiceClient.obtenerProductoPorId(1L);
        assertEquals(1L, dto.getId());
        assertEquals("Mate", dto.getNombre());

        RecordedRequest req = awaitRequest("GET", "/data/productos/1");
        assertNotNull(req, "No llegó la request GET /data/productos/1");
    }

    @Test
    @DisplayName("Feign: GET /data/productos/{id} → 404 lanza ProductoNoEncontradoException")
    void getProductoPorId_404() {
        server.enqueue(new MockResponse().setResponseCode(404));
        assertThrows(ProductoNoEncontradoException.class,
                () -> dataServiceClient.obtenerProductoPorId(999L));
    }

    @Test
    @DisplayName("Feign: GET /data/productos/{id} → 503 lanza MicroserviceCommunicationException")
    void getProductoPorId_503() {
        server.enqueue(new MockResponse().setResponseCode(503));
        assertThrows(MicroserviceCommunicationException.class,
                () -> dataServiceClient.obtenerProductoPorId(1L));
    }

    // ---------------- POST /data/productos ----------------

    @Test
    @DisplayName("Feign: POST /data/productos → 201 envía JSON correcto y mapea DTO")
    void postCrearProducto_201() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Content-Type","application/json")
                .setBody("""
                    {"id":5,"nombre":"Mouse","descripcion":"Óptico","precio":50.0,"categoriaNombre":"Periféricos","stock":3,"stockBajo":false}
                """));

        var body = new ProductoRequest("Mouse", "Óptico", new BigDecimal("50.0"), 1L, 3);
        ProductoDTO out = dataServiceClient.crearProducto(body);
        assertEquals(5L, out.getId());
        assertEquals("Mouse", out.getNombre());

        RecordedRequest req = awaitRequest("POST", "/data/productos");
        assertNotNull(req, "No llegó la request POST /data/productos");
        String sent = req.getBody().readUtf8();
        assertTrue(sent.contains("\"nombre\":\"Mouse\""));
        assertTrue(sent.contains("\"precio\":50.0"));
        assertTrue(sent.contains("\"categoriaId\":1"));
        assertTrue(sent.contains("\"stock\":3"));
        assertNotNull(req.getHeader("Content-Type"));
    }

    @Test
    @DisplayName("Feign: POST /data/productos → 400 lanza MicroserviceCommunicationException")
    void postCrearProducto_400() {
        server.enqueue(new MockResponse()
                .setResponseCode(400)
                .addHeader("Content-Type","application/json")
                .setBody("{\"message\":\"precio inválido\"}"));

        var body = new ProductoRequest("X", "Sin desc", new BigDecimal("-1"), 1L, 0);
        assertThrows(MicroserviceCommunicationException.class,
                () -> dataServiceClient.crearProducto(body));
    }

    // ---------------- PUT /data/productos/{id} ----------------

    @Test
    @DisplayName("Feign: PUT /data/productos/{id} → 200 envía JSON y mapea DTO")
    void putActualizarProducto_200() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type","application/json")
                .setBody("""
                    {"id":1,"nombre":"Actualizado","descripcion":"Desc","precio":10,"categoriaNombre":"General","stock":1,"stockBajo":false}
                """));

        var body = new ProductoRequest("Actualizado", "Desc", new BigDecimal("10"), 1L, 1);
        ProductoDTO out = dataServiceClient.actualizarProducto(1L, body);
        assertEquals("Actualizado", out.getNombre());

        RecordedRequest req = awaitRequest("PUT", "/data/productos/1");
        assertNotNull(req, "No llegó la request PUT /data/productos/1");
        assertNotNull(req.getHeader("Content-Type"));
    }

    // ---------------- DELETE /data/productos/{id} ----------------

    @Test
    @DisplayName("Feign: DELETE /data/productos/{id} → 204 sin cuerpo")
    void deleteProducto_204() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(204));
        assertDoesNotThrow(() -> dataServiceClient.eliminarProducto(1L));

        RecordedRequest req = awaitRequest("DELETE", "/data/productos/1");
        assertNotNull(req, "No llegó la request DELETE /data/productos/1");
    }

    // ---------------- GET /data/productos/categoria/{nombre} ----------------

    @Test
    @DisplayName("Feign: GET /data/productos/categoria/{nombre} → 200 verifica path con pathVariable")
    void getProductosPorCategoria_200() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type","application/json")
                .setBody("""
                    [{"id":1,"nombre":"Teclado","descripcion":"Mecánico","precio":100.5,"categoriaNombre":"Periféricos","stock":10,"stockBajo":false}]
                """));

        var lista = dataServiceClient.obtenerProductosPorCategoria("Perifericos");
        assertEquals(1, lista.size());

        RecordedRequest req = awaitRequest("GET", "/data/productos/categoria/Perifericos");
        assertNotNull(req, "No llegó la request GET /data/productos/categoria/Perifericos");
    }
}

