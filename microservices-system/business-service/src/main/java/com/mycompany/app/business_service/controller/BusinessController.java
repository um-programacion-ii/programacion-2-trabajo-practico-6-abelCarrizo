package com.mycompany.app.business_service.controller;

import com.mycompany.app.business_service.dto.CategoriaDTO;
import com.mycompany.app.business_service.dto.InventarioDTO;
import com.mycompany.app.business_service.dto.ProductoDTO;
import com.mycompany.app.business_service.dto.ProductoRequest;
import com.mycompany.app.business_service.service.CategoriaBusinessService;
import com.mycompany.app.business_service.service.InventarioBusinessService;
import com.mycompany.app.business_service.service.ProductoBusinessService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class BusinessController {

    private final ProductoBusinessService productoBusinessService;
    private final CategoriaBusinessService categoriaBusinessService;
    private final InventarioBusinessService inventarioBusinessService;

    public BusinessController(ProductoBusinessService productoBusinessService,
                              CategoriaBusinessService categoriaBusinessService,
                              InventarioBusinessService inventarioBusinessService) {
        this.productoBusinessService = productoBusinessService;
        this.categoriaBusinessService = categoriaBusinessService;
        this.inventarioBusinessService = inventarioBusinessService;
    }

    @GetMapping("/productos")
    public List<ProductoDTO> obtenerTodosLosProductos() {
        return productoBusinessService.obtenerTodosLosProductos();
    }

    @GetMapping("/productos/{id}")
    public ProductoDTO obtenerProductoPorId(@PathVariable("id") @Min(1) Long id) {
        return productoBusinessService.obtenerProductoPorId(id);
    }

    @PostMapping("/productos")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoDTO crearProducto(@Valid @RequestBody ProductoRequest request) {
        return productoBusinessService.crearProducto(request);
    }

    @PutMapping(value = "/productos/{id}", consumes = "application/json", produces = "application/json")
    public ProductoDTO actualizarProducto(@PathVariable("id") @Min(1) Long id,
                                          @Valid @RequestBody ProductoRequest request) {
        return productoBusinessService.actualizarProducto(id, request);
    }

    @DeleteMapping("/productos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarProducto(@PathVariable("id") @Min(1) Long id) {
        productoBusinessService.eliminarProducto(id);
    }

    @GetMapping("/productos/categoria/{nombre}")
    public List<ProductoDTO> obtenerProductosPorCategoria(@PathVariable("nombre") @NotBlank String nombre) {
        return categoriaBusinessService.obtenerProductosPorCategoria(nombre);
    }

    @GetMapping("/categorias")
    public List<CategoriaDTO> obtenerTodasLasCategorias() {
        return categoriaBusinessService.obtenerTodasLasCategorias();
    }

    @GetMapping("/reportes/stock-bajo")
    public List<InventarioDTO> obtenerProductosConStockBajo() {
        return inventarioBusinessService.obtenerProductosConStockBajo();
    }
}
