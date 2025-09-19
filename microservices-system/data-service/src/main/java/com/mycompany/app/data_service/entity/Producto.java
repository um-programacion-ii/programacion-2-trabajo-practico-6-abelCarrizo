package com.mycompany.app.data_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"categoria", "inventario"})
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @OneToOne(mappedBy = "producto", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Inventario inventario;

    // Helpers para mantener la relación en ambos lados
    public void setInventario(Inventario inventario) {
        this.inventario = inventario;
        if (inventario != null && inventario.getProducto() != this) {
            inventario.setProducto(this);
        }
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
        if (categoria != null && !categoria.getProductos().contains(this)) {
            categoria.getProductos().add(this);
        }
    }
}
