package com.mycompany.app.data_service.service;

import com.mycompany.app.data_service.entity.Categoria;
import com.mycompany.app.data_service.entity.Inventario;
import com.mycompany.app.data_service.entity.Producto;
import com.mycompany.app.data_service.exceptions.RecursoNoEncontradoException;
import com.mycompany.app.data_service.exceptions.ValidacionDatosException;
import com.mycompany.app.data_service.repositories.CategoriaRepository;
import com.mycompany.app.data_service.repositories.ProductoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado id=" + id));
    }

    @Override
    public Producto guardar(Producto producto) {
        validarProducto(producto);

        Long categoriaId = producto.getCategoria() != null ? producto.getCategoria().getId() : null;
        if (categoriaId == null) {
            throw new ValidacionDatosException("La categoría es obligatoria (id).");
        }
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada id=" + categoriaId));
        producto.setCategoria(categoria);

        Inventario inv = producto.getInventario();
        if (inv != null) {
            inv.setProducto(producto);
        }

        return productoRepository.save(producto);
    }

    @Override
    public Producto actualizar(Long id, Producto cambios) {
        validarProductoParaActualizar(cambios);

        Producto existente = buscarPorId(id);

        // Campos simples
        if (cambios.getNombre() != null) existente.setNombre(cambios.getNombre());
        if (cambios.getDescripcion() != null) existente.setDescripcion(cambios.getDescripcion());
        if (cambios.getPrecio() != null) existente.setPrecio(cambios.getPrecio());

        // Cambiar categoría si vino
        if (cambios.getCategoria() != null && cambios.getCategoria().getId() != null) {
            Long nuevaCatId = cambios.getCategoria().getId();
            Categoria nueva = categoriaRepository.findById(nuevaCatId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada id=" + nuevaCatId));
            existente.setCategoria(nueva);
        }

        if (cambios.getInventario() != null) {
            Inventario inv = cambios.getInventario();
            inv.setProducto(existente);
            existente.setInventario(inv);
        }

        return productoRepository.save(existente);
    }

    @Override
    public void eliminar(Long id) {
        Producto existente = buscarPorId(id);

        if (existente.getInventario() != null) {
            existente.setInventario(null);
        }
        productoRepository.delete(existente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarPorCategoria(String nombreCategoria) {
        if (nombreCategoria == null || nombreCategoria.isBlank()) {
            throw new ValidacionDatosException("El nombre de la categoría es obligatorio.");
        }
        return productoRepository.findByCategoria_NombreIgnoreCase(nombreCategoria.trim());
    }

    private void validarProducto(Producto p) {
        if (p == null) throw new ValidacionDatosException("Producto requerido.");
        if (p.getNombre() == null || p.getNombre().isBlank()) {
            throw new ValidacionDatosException("El nombre es obligatorio.");
        }
        if (p.getPrecio() == null || p.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacionDatosException("El precio no puede ser negativo ni nulo.");
        }
    }

    // --- Validaciones simples ---
    private void validarProductoParaActualizar(Producto p) {
        if (p == null) throw new ValidacionDatosException("Producto requerido.");
        if (p.getPrecio() != null && p.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacionDatosException("El precio no puede ser negativo.");
        }
    }
}
