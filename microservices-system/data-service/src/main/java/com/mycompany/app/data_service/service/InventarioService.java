package com.mycompany.app.data_service.service;

import com.mycompany.app.data_service.entity.Inventario;

import java.util.List;

public interface InventarioService {
    List<Inventario> obtenerTodos();
    Inventario buscarPorId(Long id);
    Inventario guardar(Inventario inventario);
    Inventario actualizar(Long id, Inventario inventario);
    void eliminar(Long id);

    List<Inventario> obtenerProductosConStockBajo();
}
