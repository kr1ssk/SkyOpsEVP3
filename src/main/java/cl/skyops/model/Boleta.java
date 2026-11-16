package cl.skyops.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Boleta de una compra (en el frontend es la "orden" o solicitud de despacho que se confirma desde el manifiesto). */
@Entity
public class Boleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    private EstadoBoleta estado;

    private int total;

    @ManyToOne(optional = false)
    private Usuario usuario;

    // Datos de entrega
    private String matricula;
    private String ingeniero;
    private String licencia;
    private String correo;
    private String direccion;
    private String destino;
    private String tipoEntrega;

    @OneToMany(mappedBy = "boleta", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<DetalleBoleta> detalles = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public EstadoBoleta getEstado() { return estado; }
    public void setEstado(EstadoBoleta estado) { this.estado = estado; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public String getIngeniero() { return ingeniero; }
    public void setIngeniero(String ingeniero) { this.ingeniero = ingeniero; }
    public String getLicencia() { return licencia; }
    public void setLicencia(String licencia) { this.licencia = licencia; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }
    public String getTipoEntrega() { return tipoEntrega; }
    public void setTipoEntrega(String tipoEntrega) { this.tipoEntrega = tipoEntrega; }
    public List<DetalleBoleta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleBoleta> detalles) { this.detalles = detalles; }
}
