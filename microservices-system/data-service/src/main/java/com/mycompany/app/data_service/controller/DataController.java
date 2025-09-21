package com.mycompany.app.data_service.controller;

import com.mycompany.app.data_service.entity.Categoria;
import com.mycompany.app.data_service.entity.Inventario;
import com.mycompany.app.data_service.entity.Producto;
import com.mycompany.app.data_service.service.CategoriaService;
import com.mycompany.app.data_service.service.InventarioService;
import com.mycompany.app.data_service.service.ProductoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/data")
@Validated
public class DataController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final InventarioService inventarioService;

    public DataController(ProductoService productoService,
                          CategoriaService categoriaService,
                          InventarioService inventarioService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
        this.inventarioService = inventarioService;
    }

    @GetMapping("/productos")
    public List<Producto> obtenerTodosLosProductos() {
        return productoService.obtenerTodos();
    }

    @GetMapping("/productos/{id}")
    public Producto obtenerProductoPorId(@PathVariable("id") @Min(1) Long id) {
        return productoService.buscarPorId(id);
    }

    @PostMapping("/productos")
    @ResponseStatus(HttpStatus.CREATED)
    public Producto crearProducto(@Valid @RequestBody Producto producto) {
        return productoService.guardar(producto);
    }

    @PutMapping("/productos/{id}")
    public Producto actualizarProducto(@PathVariable("id") @Min(1) Long id,
                                       @Valid @RequestBody Producto producto) {
        return productoService.actualizar(id, producto);
    }

    @DeleteMapping("/productos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarProducto(@PathVariable("id") @Min(1) Long id) {
        productoService.eliminar(id);
    }

    @GetMapping("/productos/categoria/{nombre}")
    public List<Producto> obtenerProductosPorCategoria(@PathVariable("nombre") @NotBlank String nombre) {
        return productoService.buscarPorCategoria(nombre);
    }

    @GetMapping("/categorias")
    public List<Categoria> obtenerTodasLasCategorias() {
        return categoriaService.obtenerTodas();
    }

    @GetMapping("/inventario/stock-bajo")
    public List<Inventario> obtenerProductosConStockBajo() {
        return inventarioService.obtenerProductosConStockBajo();
    }
}
