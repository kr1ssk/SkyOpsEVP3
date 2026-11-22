# SkyOps Backend

API REST de SkyOps (plataforma de abastecimiento de repuestos aeronáuticos). Proyecto de Desarrollo Fullstack II, Duoc UC.
Frontend: https://github.com/kr1ssk/SkyOpsEP3

**Stack:** Java 17+, Spring Boot 3.5, Spring Data JPA, MySQL para desarrollo y producción, H2 solo para pruebas, Spring Security con JWT (jjwt) y Swagger (springdoc).

## Perfiles

- **default (desarrollo)**: MySQL local, base `skyops_dev`. Si no existe, se crea automáticamente. Configuración:
  `DB_HOST` (localhost), `DB_NAME` (skyops_dev), `DB_USER` (root), `DB_PASSWORD` (vacía por defecto) y
  `CORS_ORIGIN` (`http://localhost:5173`). Ajusta la contraseña a la de tu instalación de MySQL.
- **test**: H2 en memoria, configurada en `src/test/resources/application.properties`. Las pruebas no necesitan MySQL.
- **prod**: MySQL, base `skyops`. Acepta las mismas variables de conexión; además **`JWT_SECRET` es obligatoria**
  y debe tener al menos 32 caracteres. No tiene valor por defecto en este perfil.

## Cómo ejecutarlo

Necesitas JDK 17 o superior y un servidor MySQL iniciado. El perfil predeterminado **no usa H2**. En Windows, desde PowerShell:

```powershell
$env:DB_HOST = "localhost"
$env:DB_NAME = "skyops_dev"
$env:DB_USER = "root"
$env:DB_PASSWORD = "tu_clave_de_MySQL"
.\mvnw.cmd spring-boot:run
```

En macOS o Linux puedes ejecutar `./mvnw spring-boot:run` y exportar antes las mismas variables. Si MySQL permite a `root` conectarse sin contraseña, deja `DB_PASSWORD` vacía. Spring crea `skyops_dev` y las tablas al iniciar.

Para ejecutar pruebas (usan H2 en memoria y no requieren MySQL):

```powershell
.\mvnw.cmd clean verify
```

Para arrancar el perfil de producción en local, define también `JWT_SECRET` (mínimo 32 caracteres) y activa el perfil:

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"
$env:DB_NAME = "skyops"
$env:JWT_SECRET = "reemplaza-por-un-secreto-aleatorio-de-al-menos-32-caracteres"
.\mvnw.cmd spring-boot:run
```

| Qué | Dirección |
| --- | --- |
| API | http://localhost:8080/api/v1 |
| Swagger | http://localhost:8080/swagger-ui.html |
| Consola H2 | Solo se configura para pruebas; no está disponible al ejecutar la aplicación normalmente. |

En el primer arranque se cargan datos de demostración: 3 categorías, 3 productos, 4 aeronaves y 3 cuentas, todas con
la clave `Skyops123`:

| Correo | Rol |
| --- | --- |
| `admin@skyops.cl` | ADMIN |
| `vendedor@skyops.cl` | VENDEDOR |
| `cliente@skyops.cl` | CLIENTE |

Para probar en Swagger: `POST /api/v1/auth/login`, copia el `accessToken`, botón **Authorize** y pégalo.

## Empaquetado

```bash
./mvnw clean package -DskipTests   # genera target/app.jar
SPRING_PROFILES_ACTIVE=prod DB_HOST=localhost DB_NAME=skyops DB_USER=root DB_PASSWORD=secreto CORS_ORIGIN=http://localhost:5173 JWT_SECRET=una-clave-de-32-caracteres-o-mas java -jar target/app.jar
```

## Arquitectura

```text
Controlador  →  Servicio  →  Repositorio  →  Base de datos
(recibe HTTP)   (reglas de    (Spring Data
                 negocio)      JPA)
```

```text
src/main/java/cl/skyops/
├── controller/    Endpoints REST (+ anotaciones de Swagger)
├── service/       Lógica de negocio
├── repository/    Interfaces JpaRepository
├── model/         Entidades JPA (tablas)
├── dto/           Datos que entran y salen por la API, con sus validaciones
├── security/      JWT: JwtService, JwtFilter, SecurityConfig
├── config/        CORS (CorsConfig), Swagger (OpenApiConfig) y datos iniciales (DataLoader)
└── exception/     GlobalExceptionHandler: formato único de errores
```

### Modelo de datos

Sigue la propuesta del Anexo 1 (USUARIO, BOLETA, DETALLE_BOLETA, PRODUCTO, CATEGORIA):

```text
USUARIO 1 ──── * BOLETA 1 ──── * DETALLE_BOLETA * ──── 1 PRODUCTO * ──── 1 CATEGORIA
```

- `Producto` tiene `@ManyToOne Categoria`. Si se renombra la categoría, los productos la siguen automáticamente.
- `Boleta` tiene `@ManyToOne Usuario` y `@OneToMany` hacia `DetalleBoleta`. Cada detalle guarda el producto, la
  cantidad y el precio con descuento del momento de la compra, así la boleta no cambia si luego cambia el precio.
- Dos tablas extra que no están en la figura pero el frontend las usa: `Aeronave` (página Flota) y `Contacto`
  (formulario de contacto). Ninguna se relaciona con las demás.

### Lógica de negocio (dónde mirar)

| Regla | Archivo |
| --- | --- |
| El servidor recalcula precios con descuento y total, no confía en lo que manda el cliente | `BoletaService.crear` |
| No se puede comprar más que el stock; la compra aprobada descuenta stock; la rechazada no lo toca | `BoletaService.crear` |
| Toda la compra es una transacción: si algo falla, no queda stock descontado a medias | `@Transactional` en `BoletaService` |
| Dos compras simultáneas no pueden dejar stock negativo | `@Version` en `Producto` |
| El número de parte (PN) no se repite; la categoría del producto debe existir | `ProductoService.guardar` |
| Un producto ya vendido no se borra; una categoría con productos tampoco | `ProductoService.eliminar`, `CategoriaService.eliminar` |
| Correo único; siempre queda al menos un admin; no te puedes borrar a ti mismo | `UsuarioService` |
| El cliente solo ve sus boletas; admin y vendedor ven todas | `BoletaService.listar / obtener` |

## Autenticación y roles (JWT)

1. `POST /api/v1/auth/login` valida correo y contraseña (la clave se guarda con **BCrypt**, nunca en texto) y devuelve
   dos tokens firmados con HMAC-SHA256:
   - `accessToken`: dura 15 minutos y se envía en cada petición como `Authorization: Bearer <token>`.
   - `refreshToken`: dura 7 días y solo sirve en `POST /api/v1/auth/refresh` para obtener tokens nuevos.
2. `JwtFilter` revisa el token en cada petición. Si es válido y de tipo `acceso`, deja al usuario autenticado con su rol.
   Un refresh token no sirve para entrar a la API.
3. `SecurityConfig` define qué rutas son públicas; el resto exige token (sin token: **401**).
4. En el mismo `SecurityConfig` está la regla de cada ruta por rol (rol insuficiente: **403**). Se evalúa antes
   de llegar al controlador, así quien no tiene permiso recibe 403 aunque mande datos inválidos.
5. La API no guarda sesión en el servidor (`STATELESS`): el estado vive en el token. Por eso el logout lo hace el
   frontend borrando los tokens.

Roles (Anexo 1):

| Rol | Puede |
| --- | --- |
| ADMIN | Todo |
| VENDEDOR | Ver productos y su detalle, ver boletas y su detalle. Nada más |
| CLIENTE | La tienda: ver productos, comprar, ver sus propias boletas y editar su perfil |

El registro público (`/auth/registro`) siempre crea CLIENTE, aunque el cuerpo traiga otro rol. Solo un ADMIN puede crear
vendedores o administradores desde `/usuarios`.

El secreto de firma sale de la variable de entorno `JWT_SECRET` (mínimo 32 caracteres). Si no existe usa uno de
desarrollo definido en `application.properties`; **cámbialo antes de publicar**.

## Endpoints

Todas las rutas llevan versión (`/api/v1`). Errores con el mismo formato: `{"mensaje": "...", "errores": {"campo": "mensaje"}}`.
Códigos: 400 datos inválidos, 401 sin token o token vencido, 403 sin permiso, 404 no existe, 409 conflicto de negocio.

| Método | Ruta | Acceso | Descripción |
| --- | --- | --- | --- |
| POST | `/api/v1/auth/registro` | Público | Crea una cuenta CLIENTE |
| POST | `/api/v1/auth/login` | Público | Devuelve `accessToken`, `refreshToken` y el usuario |
| POST | `/api/v1/auth/refresh` | Público | Cambia un `refreshToken` válido por tokens nuevos |
| GET | `/api/v1/productos` | Público | Lista productos |
| GET | `/api/v1/productos/{id}` | Público | Detalle de un producto |
| POST | `/api/v1/productos` | ADMIN | Crea un producto |
| PUT | `/api/v1/productos/{id}` | ADMIN | Actualiza un producto |
| DELETE | `/api/v1/productos/{id}` | ADMIN | Elimina un producto |
| GET | `/api/v1/categorias` | Público | Lista categorías |
| POST | `/api/v1/categorias` | ADMIN | Crea una categoría (`ATA 27`) |
| PUT | `/api/v1/categorias/{id}` | ADMIN | Renombra una categoría |
| DELETE | `/api/v1/categorias/{id}` | ADMIN | Elimina una categoría sin productos |
| GET | `/api/v1/usuarios/me` | Con sesión | Perfil propio |
| PUT | `/api/v1/usuarios/me` | Con sesión | Edita el perfil propio (no puede cambiar su rol) |
| GET | `/api/v1/usuarios` | ADMIN | Lista usuarios |
| GET | `/api/v1/usuarios/{id}` | ADMIN | Detalle de un usuario |
| POST | `/api/v1/usuarios` | ADMIN | Crea un usuario con rol |
| PUT | `/api/v1/usuarios/{id}` | ADMIN | Edita un usuario y su rol |
| DELETE | `/api/v1/usuarios/{id}` | ADMIN | Elimina un usuario |
| POST | `/api/v1/boletas` | CLIENTE, ADMIN | Confirma el manifiesto y crea la boleta |
| GET | `/api/v1/boletas` | Con sesión | ADMIN y VENDEDOR: todas. CLIENTE: solo las suyas |
| GET | `/api/v1/boletas/{id}` | Con sesión | Detalle (un CLIENTE solo ve las suyas) |
| PUT | `/api/v1/boletas/{id}/resolver` | ADMIN | Pasa una boleta PENDIENTE a RESUELTO |
| GET | `/api/v1/flota` | Público | Lista aeronaves |
| POST | `/api/v1/contactos` | Público | Envía un mensaje de contacto |
| GET | `/api/v1/contactos` | ADMIN | Lista los mensajes recibidos |

Ejemplo de `POST /api/v1/boletas` (el frontend manda solo ids y cantidades; los precios los calcula el servidor):

```json
{
  "entrega": {
    "matricula": "CC-BFA", "ingeniero": "Juan Perez", "licencia": "AB-1234",
    "correo": "juan@correo.cl", "direccion": "Av. Siempre Viva 123",
    "destino": "SCL", "tipoEntrega": "Retiro en bodega"
  },
  "detalles": [{ "productoId": 1, "cantidad": 2 }],
  "aprobada": true
}
```

`aprobada` simula el resultado del pago: `true` crea la boleta `PENDIENTE` y descuenta stock; `false` la crea
`RECHAZADO` sin tocar el stock (igual que la simulación que ya hacía el frontend).

## Integración con el frontend

- Todas las llamadas van a `http://localhost:8080/api/v1`. CORS permite el origen de `CORS_ORIGIN`
  (por defecto `http://localhost:5173`, el servidor de desarrollo de Vite); lo configura `CorsConfig`.
- Flujo: login → el frontend guarda `accessToken` y `refreshToken` → envía `Authorization: Bearer` en cada llamada →
  si recibe 401, llama a `/auth/refresh` y repite la petición; si el refresh también falla, vuelve a `/login`.
- Diferencias de formato respecto al frontend original en localStorage: `rol` llega como `"ADMIN"` / `"VENDEDOR"` /
  `"CLIENTE"`, los pedidos pasan a llamarse boletas (`detalles` en vez de `lineas`), el `estado` es `"PENDIENTE"` /
  `"RECHAZADO"` / `"RESUELTO"` y los ids son números.

## Pruebas

`src/test/java/cl/skyops/ApiIntegrationTest.java` levanta la aplicación completa (con H2 en memoria, así no
depende de MySQL) y llama a la API real: login, 401/403 por rol (admin, vendedor, cliente), CRUD de productos,
renovación de tokens, CORS y compra con control de stock.

