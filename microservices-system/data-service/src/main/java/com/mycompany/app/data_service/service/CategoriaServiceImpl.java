package com.mycompany.app.data_service.service;


import com.mycompany.app.data_service.entity.Categoria;
import com.mycompany.app.data_service.exceptions.RecursoNoEncontradoException;
import com.mycompany.app.data_service.exceptions.ValidacionDatosException;
import com.mycompany.app.data_service.repositories.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> obtenerTodas() {
        return categoriaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada id=" + id));
    }

    @Override
    public Categoria guardar(Categoria categoria) {
        validarCategoria(categoria);
        return categoriaRepository.save(categoria);
    }

    @Override
    public Categoria actualizar(Long id, Categoria cambios) {
        if (cambios == null) throw new ValidacionDatosException("Categoría requerida.");
        Categoria existente = buscarPorId(id);

        if (cambios.getNombre() != null && !cambios.getNombre().isBlank()) {
            existente.setNombre(cambios.getNombre());
        }
        if (cambios.getDescripcion() != null) {
            existente.setDescripcion(cambios.getDescripcion());
        }
        return categoriaRepository.save(existente);
    }

    @Override
    public void eliminar(Long id) {
        Categoria existente = buscarPorId(id);
        categoriaRepository.delete(existente);
    }

    @Override
    @Transactional(readOnly = true)
    public Categoria buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ValidacionDatosException("El nombre de categoría es obligatorio.");
        }
        return categoriaRepository.findByNombreIgnoreCase(nombre.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada: " + nombre));
    }

    // --- Validaciones simples ---
    private void validarCategoria(Categoria c) {
        if (c == null) throw new ValidacionDatosException("Categoría requerida.");
        if (c.getNombre() == null || c.getNombre().isBlank()) {
            throw new ValidacionDatosException("El nombre de la categoría es obligatorio.");
        }
    }
}