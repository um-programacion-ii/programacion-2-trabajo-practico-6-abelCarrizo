package com.mycompany.app.business_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String categoriaNombre; // lo mostramos “resuelto” en lugar de categoriaId
    private Integer stock;
    private Boolean stockBajo;
}