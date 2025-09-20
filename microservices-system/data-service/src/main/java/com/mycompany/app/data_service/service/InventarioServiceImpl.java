package com.mycompany.app.data_service.service;

import com.mycompany.app.data_service.entity.Inventario;
import com.mycompany.app.data_service.entity.Producto;
import com.mycompany.app.data_service.exceptions.RecursoNoEncontradoException;
import com.mycompany.app.data_service.exceptions.ValidacionDatosException;
import com.mycompany.app.data_service.repositories.InventarioRepository;
import com.mycompany.app.data_service.repositories.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventarioServiceImpl implements InventarioService {

    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Inventario> obtenerTodos() {
        return inventarioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Inventario buscarPorId(Long id) {
        return inventarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Inventario no encontrado id=" + id));
    }

    @Override
    public Inventario guardar(Inventario inventario) {
        validarInventario(inventario);

        // asegurar que el producto exista y asociarlo
        Long productoId = inventario.getProducto() != null ? inventario.getProducto().getId() : null;
        if (productoId == null) {
            throw new ValidacionDatosException("El id del producto es obligatorio para el inventario.");
        }
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado id=" + productoId));

        inventario.setProducto(producto);
        return inventarioRepository.save(inventario);
    }

    @Override
    public Inventario actualizar(Long id, Inventario cambios) {
        if (cambios == null) throw new ValidacionDatosException("Inventario requerido.");

        Inventario existente = buscarPorId(id);

        if (cambios.getCantidad() != null) {
            if (cambios.getCantidad() < 0) throw new ValidacionDatosException("La cantidad no puede ser negativa.");
            existente.setCantidad(cambios.getCantidad());
        }
        if (cambios.getStockMinimo() != null) {
            if (cambios.getStockMinimo() < 0) throw new ValidacionDatosException("El stock mínimo no puede ser negativo.");
            existente.setStockMinimo(cambios.getStockMinimo());
        }
        if (cambios.getProducto() != null && cambios.getProducto().getId() != null) {
            Long nuevoProductoId = cambios.getProducto().getId();
            Producto nuevoProducto = productoRepository.findById(nuevoProductoId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado id=" + nuevoProductoId));
            existente.setProducto(nuevoProducto);
        }
        return inventarioRepository.save(existente);
    }

    @Override
    public void eliminar(Long id) {
        Inventario existente = buscarPorId(id);
        inventarioRepository.delete(existente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventario> obtenerProductosConStockBajo() {
        return inventarioRepository.findConStockBajo();
    }

    // --- Validaciones simples ---
    private void validarInventario(Inventario i) {
        if (i == null) throw new ValidacionDatosException("Inventario requerido.");
        if (i.getCantidad() == null || i.getCantidad() < 0) {
            throw new ValidacionDatosException("La cantidad no puede ser nula ni negativa.");
        }
        if (i.getStockMinimo() == null || i.getStockMinimo() < 0) {
            throw new ValidacionDatosException("El stock mínimo no puede ser nulo ni negativo.");
        }
        if (i.getProducto() == null || i.getProducto().getId() == null) {
            throw new ValidacionDatosException("Debe asociarse un producto (id).");
        }
    }
}
