package cl.skyops.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Aeronave de la flota. Estado: "AOG" (en tierra, esperando repuestos) u "Operativa". */
@Entity
public class Aeronave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String matricula;
    private String modelo;
    private String aerolinea;
    private String ubicacion;
    private String estado;

    public Aeronave() {
    }

    public Aeronave(Long id, String matricula, String modelo, String aerolinea, String ubicacion, String estado) {
        this.id = id;
        this.matricula = matricula;
        this.modelo = modelo;
        this.aerolinea = aerolinea;
        this.ubicacion = ubicacion;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getAerolinea() { return aerolinea; }
    public void setAerolinea(String aerolinea) { this.aerolinea = aerolinea; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
