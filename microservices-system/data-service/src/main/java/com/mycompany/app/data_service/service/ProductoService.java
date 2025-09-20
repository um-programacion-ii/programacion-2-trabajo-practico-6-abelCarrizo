package com.mycompany.app.data_service.service;

import com.mycompany.app.data_service.entity.Producto;

import java.util.List;

public interface ProductoService {
    List<Producto> obtenerTodos();
    Producto buscarPorId(Long id);
    Producto guardar(Producto producto);
    Producto actualizar(Long id, Producto producto);
    void eliminar(Long id);

    List<Producto> buscarPorCategoria(String nombreCategoria);
}