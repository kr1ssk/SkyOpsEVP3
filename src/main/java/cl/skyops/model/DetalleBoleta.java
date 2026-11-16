package cl.skyops.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** Una línea de la boleta: qué producto, cuántas unidades y a qué precio se vendió. */
@Entity
public class DetalleBoleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Boleta boleta;

    @ManyToOne(optional = false)
    private Producto producto;

    private int cantidad;

    /** Precio con descuento al momento de la compra: si el producto cambia de precio, la boleta no cambia. */
    private int precioUnitario;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Boleta getBoleta() { return boleta; }
    public void setBoleta(Boleta boleta) { this.boleta = boleta; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public int getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(int precioUnitario) { this.precioUnitario = precioUnitario; }
}
