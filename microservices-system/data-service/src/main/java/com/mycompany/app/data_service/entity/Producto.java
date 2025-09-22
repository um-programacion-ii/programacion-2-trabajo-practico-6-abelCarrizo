package com.mycompany.app.data_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"categoria", "inventario"})
@Entity
@Table(name = "productos")
@JsonIgnoreProperties({"categoria", "inventario"})
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
    @JsonIgnoreProperties({"productos"})
    private Categoria categoria;

    @OneToOne(mappedBy = "producto", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "producto-inventario")
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

    @JsonProperty("categoriaId")
    public void setCategoriaIdFromJson(Long categoriaId) {
        if (categoriaId != null) {
            Categoria c = new Categoria();
            c.setId(categoriaId);
            this.setCategoria(c); // usa tu helper para mantener ambos lados
        }
    }

    @JsonProperty("stock")
    public void setStockFromJson(Integer cantidad) {
        if (cantidad == null) return;
        if (this.getInventario() == null) {
            Inventario inv = new Inventario();
            inv.setStockMinimo(0);          // valor por defecto para NOT NULL
            this.setInventario(inv);        // usa tu helper
        }
        this.getInventario().setCantidad(cantidad);
    }

    @JsonProperty("stockMinimo")
    public void setStockMinimoFromJson(Integer min) {
        if (min == null) return;
        if (this.getInventario() == null) {
            Inventario inv = new Inventario();
            this.setInventario(inv);
        }
        this.getInventario().setStockMinimo(min);
    }

    @JsonProperty("categoriaNombre")
    public String getCategoriaNombreJson() {
        return (categoria != null) ? categoria.getNombre() : null;
    }

    @JsonProperty("stock")
    public Integer getStockJson() {
        return (inventario != null) ? inventario.getCantidad() : null;
    }

    @JsonProperty("stockBajo")
    public Boolean getStockBajoJson() {
        Integer s = (inventario != null) ? inventario.getCantidad() : null;
        Integer m = (inventario != null) ? inventario.getStockMinimo() : null;
        return (s != null && m != null) ? s < m : null;
    }
}
