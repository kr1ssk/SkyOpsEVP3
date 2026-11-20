# Despliegue de SkyOps en AWS EC2

Todo corre en **una sola instancia t2.micro**: MySQL, el backend y nginx sirviendo el
frontend. Un solo origen, una sola URL.

```
                 Internet
                     |
                  puerto 80
                     |
              +--------------+
              |    nginx     |
              +--------------+
               /            \
        /  (estatico)      /api/  (proxy)
       |                      |
  /var/www/skyops      127.0.0.1:8080
   (frontend dist)      backend Spring Boot
                              |
                        127.0.0.1:3306
                           MySQL  (base "skyops")
```

**El puerto 8080 no se abre al exterior.** Solo nginx entra al backend, y solo por
localhost. Una pagina servida por https tampoco puede llamar a una API http, asi que
servir las dos cosas desde el mismo origen evita ese bloqueo del navegador.

## 1. Grupo de seguridad

| Puerto | Protocolo | Origen | Para que |
| --- | --- | --- | --- |
| 22 | TCP | solo tu IP | SSH |
| 80 | TCP | 0.0.0.0/0 | la aplicacion |

No abrir el 8080 ni el 3306.

## 2. Instalar lo que hace falta (Ubuntu 24.04)

```bash
sudo apt update
sudo apt install -y openjdk-17-jre-headless mysql-server nginx
```

## 3. Crear la base y su usuario

No se usa `root` para la aplicacion: se crea un usuario con permisos solo sobre su base.

```bash
sudo mysql -e "CREATE DATABASE IF NOT EXISTS skyops CHARACTER SET utf8mb4;"
sudo mysql -e "CREATE USER 'skyops'@'localhost' IDENTIFIED BY 'LA_CLAVE_REAL';"
sudo mysql -e "GRANT ALL PRIVILEGES ON skyops.* TO 'skyops'@'localhost'; FLUSH PRIVILEGES;"
```

## 4. Compilar el backend (en tu PC) y subirlo

Una t2.micro tiene 1 GB de RAM: compilar Spring Boot ahi falla o tarda muchisimo.

```powershell
.\mvnw.cmd clean package
scp -i tu-llave.pem target\app.jar ubuntu@LA_IP:/tmp/app.jar
```

En la EC2:

```bash
sudo mkdir -p /opt/skyops && sudo mv /tmp/app.jar /opt/skyops/app.jar
```

## 5. Los secretos

```bash
sudo cp deploy/skyops.env.example /etc/skyops.env
sudo nano /etc/skyops.env
sudo chmod 600 /etc/skyops.env
openssl rand -base64 48      # para generar el JWT_SECRET
```

## 6. El servicio

```bash
sudo cp deploy/skyops.service /etc/systemd/system/skyops.service
sudo systemctl daemon-reload
sudo systemctl enable --now skyops
sudo systemctl status skyops
journalctl -u skyops -n 50 --no-pager     # si no arranca, la causa esta aqui
```

## 7. Compilar el frontend (en tu PC) y subirlo

La URL de la API se fija al compilar. Vacia = rutas relativas, que es lo que corresponde
cuando nginx sirve las dos cosas desde el mismo origen:

```powershell
$env:VITE_API_URL=""
npm run build
scp -i tu-llave.pem -r dist\* ubuntu@LA_IP:/tmp/dist/
```

En la EC2:

```bash
sudo mkdir -p /var/www/skyops && sudo cp -r /tmp/dist/* /var/www/skyops/
sudo chown -R www-data:www-data /var/www/skyops
```

## 8. nginx

```bash
sudo cp deploy/nginx-skyops.conf /etc/nginx/sites-available/skyops
sudo ln -sf /etc/nginx/sites-available/skyops /etc/nginx/sites-enabled/skyops
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t && sudo systemctl reload nginx
```

## 9. Verificar

```bash
curl -s -o /dev/null -w "frontend: %{http_code}\n" http://localhost/
curl -s -o /dev/null -w "api:      %{http_code}\n" http://localhost/api/v1/productos
```

Las dos tienen que dar 200. Despues, en el navegador: `http://LA_IP/`
Swagger queda en `http://LA_IP/swagger-ui.html`.

## 10. Evidencia para la presentacion

```bash
journalctl -u skyops -f                   # logs en vivo

for i in $(seq 1 8); do                   # los 8 pings del heartbeat
  printf "ping %d: " "$i"
  curl -s -o /dev/null -w "HTTP %{http_code}  %{time_total}s\n" http://localhost/api/v1/productos
  sleep 1
done

sudo systemctl restart skyops && sleep 12 && systemctl is-active skyops
```

## 11. El paso siguiente, si preguntan por escalabilidad

Esta arquitectura **no escala horizontalmente todavia**, y hay que decirlo asi: MySQL
vive dentro de la misma instancia, asi que una segunda EC2 tendria su propia base.

1. Mover la base a **RDS**. Es cambiar `DB_HOST` en `/etc/skyops.env`, ninguna linea de
   Java. El backend ya es stateless por el JWT, asi que la base era el ultimo estado que
   quedaba dentro de la instancia.
2. Crear una **AMI** y poner un **Application Load Balancer** delante.
3. Un **Auto Scaling Group** con minimo 2 instancias en zonas distintas.

El orden importa: hasta que la base este afuera, agregar instancias crea inconsistencia
en vez de capacidad.
