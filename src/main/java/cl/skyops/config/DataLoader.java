package cl.skyops.config;

import cl.skyops.model.Aeronave;
import cl.skyops.model.Categoria;
import cl.skyops.model.Producto;
import cl.skyops.model.Rol;
import cl.skyops.model.Usuario;
import cl.skyops.repository.AeronaveRepository;
import cl.skyops.repository.CategoriaRepository;
import cl.skyops.repository.ProductoRepository;
import cl.skyops.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Carga los datos de demostración la primera vez que arranca con la BD vacía (los mismos del frontend). */
@Component
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarios;
    private final CategoriaRepository categorias;
    private final ProductoRepository productos;
    private final AeronaveRepository aeronaves;
    private final PasswordEncoder encoder;

    public DataLoader(UsuarioRepository usuarios,
            CategoriaRepository categorias,
            ProductoRepository productos,
            AeronaveRepository aeronaves,
            PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.categorias = categorias;
        this.productos = productos;
        this.aeronaves = aeronaves;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (usuarios.count() > 0) {
            return;
        }

        usuario("Administrador SkyOps", "admin@skyops.cl", Rol.ADMIN);
        usuario("Vendedor SkyOps", "vendedor@skyops.cl", Rol.VENDEDOR);
        usuario("Cliente Demo", "cliente@skyops.cl", Rol.CLIENTE);

        var ata27 = categoria("ATA 27");
        var ata32 = categoria("ATA 32");
        var ata72 = categoria("ATA 72");

        producto("Actuador de Superficie de Control", "PN-27-6612", ata27,
                "Actuador para el sistema de control de vuelo de una aeronave.",
                "Bodega ANF-A03", 4, 850000, 10, true, "actuador-superficie.svg");
        producto("Actuador de Tren Principal", "PN-32-4471", ata32,
                "Componente del sistema de accionamiento del tren de aterrizaje.",
                "Bodega SCL-A17", 1, 1250000, 0, true, "actuador-tren.svg");
        producto("Álabe de Turbina", "PN-72-7754", ata72,
                "Repuesto de turbina. Su certificación debe revisarse antes de instalarlo.",
                "Bodega ANF-D11", 2, 420000, 5, false, "alabe-turbina.svg");

        aeronaves.save(new Aeronave(null, "CC-BFA", "A320neo", "LATAM", "SCL · Puerta 14", "AOG"));
        aeronaves.save(new Aeronave(null, "CC-AVE", "A321", "LATAM", "SCL · Plataforma Remota 3", "Operativa"));
        aeronaves.save(new Aeronave(null, "CC-AZS", "B737-800", "Sky", "ANF · Hangar 2", "AOG"));
        aeronaves.save(new Aeronave(null, "CC-DBC", "A320", "JetSMART", "SCL · Puerta 22", "Operativa"));
    }

    /** Cuentas de demostración, todas con la clave Skyops123. */
    private void usuario(String nombre, String correo, Rol rol) {
        var u = new Usuario();
        u.setNombre(nombre);
        u.setCorreo(correo);
        u.setClave(encoder.encode("Skyops123"));
        u.setRol(rol);
        u.setDireccion("");
        u.setLicencia("");
        usuarios.save(u);
    }

    private Categoria categoria(String nombre) {
        var c = new Categoria();
        c.setNombre(nombre);
        return categorias.save(c);
    }

    private void producto(String nombre, String pn, Categoria categoria, String descripcion, String ubicacion,
                          int stock, int precio, int descuento, boolean certificado, String imagen) {
        var p = new Producto();
        p.setNombre(nombre);
        p.setPn(pn);
        p.setCategoria(categoria);
        p.setDescripcion(descripcion);
        p.setUbicacion(ubicacion);
        p.setStock(stock);
        p.setPrecio(precio);
        p.setDescuento(descuento);
        p.setCertificado(certificado);
        p.setImagen(imagen);
        productos.save(p);
    }
}
