package com.mycompany.app.data_service.service;

import com.mycompany.app.data_service.entity.Categoria;

import java.util.List;

public interface CategoriaService {
    List<Categoria> obtenerTodas();
    Categoria buscarPorId(Long id);
    Categoria guardar(Categoria categoria);
    Categoria actualizar(Long id, Categoria categoria);
    void eliminar(Long id);

    Categoria buscarPorNombre(String nombre);
}
