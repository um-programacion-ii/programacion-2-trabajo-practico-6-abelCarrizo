package com.mycompany.app.data_service.repositories;

import com.mycompany.app.data_service.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByCategoria_NombreIgnoreCase(String nombreCategoria);
}
