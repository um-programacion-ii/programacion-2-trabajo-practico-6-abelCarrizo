package com.mycompany.app.data_service.controller;

import com.mycompany.app.data_service.DataServiceApplication;
import com.mycompany.app.data_service.entity.Producto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = DataServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
class DataControllerIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate rest;

    private String url(String path) {
        return "http://localhost:" + port + "/data" + path;
    }

    @Test
    @DisplayName("GET /data/productos/999 → 404 cuando no existe")
    void getProductoInexistente_notFound() {
        ResponseEntity<String> resp = rest.getForEntity(url("/productos/999"), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GET /data/productos → 200 y (posible) lista vacía")
    void getProductos_ok() {
        ResponseEntity<String> resp = rest.getForEntity(url("/productos"), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("POST /data/productos con precio negativo → 400 por ValidacionDatosException")
    void crearProducto_precioNegativo_badRequest() {
        Producto prod = new Producto();
        prod.setNombre("X");
        prod.setDescripcion("Prueba");
        prod.setPrecio(BigDecimal.valueOf(-10)); // fuerza ValidacionDatosException

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Producto> entity = new HttpEntity<>(prod, headers);

        ResponseEntity<String> resp = rest.postForEntity(url("/productos"), entity, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
