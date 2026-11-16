package cl.skyops.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;

/** Repuesto aeronáutico del catálogo. */
@Entity
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    /** Número de parte, único. */
    @Column(nullable = false, unique = true)
    private String pn;

    @ManyToOne(optional = false)
    private Categoria categoria;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(nullable = false)
    private String ubicacion;

    private int stock;

    /** Precio en CLP, sin descuento. */
    private int precio;

    /** Porcentaje de descuento (0 a 100). */
    private int descuento;

    private boolean certificado;

    private String imagen;

    /** Si dos compras descuentan stock a la vez, la segunda falla en vez de dejar stock negativo. */
    @Version
    private Long version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getPn() { return pn; }
    public void setPn(String pn) { this.pn = pn; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public int getPrecio() { return precio; }
    public void setPrecio(int precio) { this.precio = precio; }
    public int getDescuento() { return descuento; }
    public void setDescuento(int descuento) { this.descuento = descuento; }
    public boolean isCertificado() { return certificado; }
    public void setCertificado(boolean certificado) { this.certificado = certificado; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
