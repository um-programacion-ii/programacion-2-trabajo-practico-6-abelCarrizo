package com.mycompany.app.business_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequest {
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Long categoriaId;  // usamos Id para la relación
    private Integer stock;
}
