package com.mycompany.app.business_service.client;

import com.mycompany.app.business_service.dto.CategoriaDTO;
import com.mycompany.app.business_service.dto.InventarioDTO;
import com.mycompany.app.business_service.dto.ProductoDTO;
import com.mycompany.app.business_service.dto.ProductoRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "data-service",
        url = "${data.service.url}",
        configuration = com.mycompany.app.business_service.client.config.FeignClientConfig.class
)
public interface DataServiceClient {

    @GetMapping("/data/productos")
    List<ProductoDTO> obtenerTodosLosProductos();

    @GetMapping("/data/productos/{id}")
    ProductoDTO obtenerProductoPorId(@PathVariable("id") Long id);

    @PostMapping("/data/productos")
    ProductoDTO crearProducto(@RequestBody ProductoRequest request);

    @PutMapping("/data/productos/{id}")
    ProductoDTO actualizarProducto(@PathVariable("id") Long id, @RequestBody ProductoRequest request);

    @DeleteMapping("/data/productos/{id}")
    void eliminarProducto(@PathVariable("id") Long id);

    @GetMapping("/data/productos/categoria/{nombre}")
    List<ProductoDTO> obtenerProductosPorCategoria(@PathVariable("nombre") String nombre);

    @GetMapping("/data/categorias")
    List<CategoriaDTO> obtenerTodasLasCategorias();

    @GetMapping("/data/inventario/stock-bajo")
    List<InventarioDTO> obtenerProductosConStockBajo();
}

