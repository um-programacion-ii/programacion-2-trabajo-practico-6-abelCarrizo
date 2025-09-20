package com.mycompany.app.business_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioDTO {
    private Long productoId;
    private Integer cantidad;
    private Integer stockMinimo;
    private Instant fechaActualizacion;
}