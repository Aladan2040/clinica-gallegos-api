# 🐳 Docker Compose - Clínica Gallegos API

Este proyecto incluye una configuración completa de Docker Compose para ejecutar la aplicación de manera local con todos los servicios necesarios.

## 📋 Requisitos Previos

- **Docker Desktop** instalado y ejecutándose
  - Windows: [Descargar Docker Desktop](https://www.docker.com/products/docker-desktop/)
  - Verificar instalación: `docker --version` y `docker-compose --version`
- **Java 21** (opcional, solo si quieres compilar fuera de Docker)
- **Maven** (opcional, solo si quieres compilar fuera de Docker)

## 🚀 Inicio Rápido

### 1. Iniciar todos los servicios

```powershell
# Construir e iniciar todos los servicios
docker-compose up -d --build
```

Este comando iniciará:
- ✅ **PostgreSQL** en el puerto `5432`
- ✅ **API Spring Boot** en el puerto `8080`
- ✅ **pgAdmin** (opcional) en el puerto `5050`

### 2. Verificar que los servicios están funcionando

```powershell
# Ver el estado de los contenedores
docker-compose ps

# Ver los logs de la API
docker-compose logs -f api

# Ver los logs de PostgreSQL
docker-compose logs -f postgres
```

### 3. Probar la API

Una vez que los servicios estén ejecutándose, puedes probar la API:

```
http://localhost:8080/api/servicios
```

## 🛠️ Comandos Útiles

### Gestión de Contenedores

```powershell
# Iniciar servicios (sin reconstruir)
docker-compose up -d

# Detener servicios
docker-compose stop

# Detener y eliminar contenedores
docker-compose down

# Detener y eliminar contenedores + volúmenes (⚠️ borra datos de BD)
docker-compose down -v

# Reconstruir solo la API
docker-compose up -d --build api

# Reiniciar solo la API
docker-compose restart api
```

### Ver Logs

```powershell
# Ver logs de todos los servicios
docker-compose logs -f

# Ver logs solo de la API
docker-compose logs -f api

# Ver logs solo de PostgreSQL
docker-compose logs -f postgres

# Ver últimas 100 líneas
docker-compose logs --tail=100 api
```

### Acceso a Contenedores

```powershell
# Acceder al contenedor de la API
docker-compose exec api sh

# Acceder al contenedor de PostgreSQL
docker-compose exec postgres psql -U postgres -d clinicagallegos_bd
```

## 🗄️ Base de Datos

### Credenciales por Defecto

- **Host:** localhost
- **Puerto:** 5432
- **Base de datos:** clinicagallegos_bd
- **Usuario:** postgres
- **Contraseña:** admin

### Conectarse a PostgreSQL

#### Desde línea de comandos:

```powershell
docker-compose exec postgres psql -U postgres -d clinicagallegos_bd
```

#### Desde pgAdmin (incluido en Docker Compose):

1. Abre tu navegador: http://localhost:5050
2. Credenciales:
   - **Email:** admin@clinica.com
   - **Contraseña:** admin
3. Agregar servidor:
   - **Host:** postgres
   - **Puerto:** 5432
   - **Usuario:** postgres
   - **Contraseña:** admin

### Script de Inicialización

El archivo `init-db.sql` se ejecuta automáticamente al crear el contenedor de PostgreSQL por primera vez. Este script:
- Crea las tablas necesarias (usuarios, servicios, citas)
- Inserta los servicios por defecto
- Crea índices para mejorar el rendimiento

Si necesitas reiniciar la base de datos:

```powershell
docker-compose down -v
docker-compose up -d
```

## ⚙️ Configuración

### Variables de Entorno

Puedes modificar las variables de entorno en el archivo `docker-compose.yml`:

#### API (servicio `api`):

```yaml
environment:
  DB_URL: jdbc:postgresql://postgres:5432/clinicagallegos_bd
  DB_USERNAME: postgres
  DB_PASSWORD: admin
  PORT: 8080
  JWT_SECRET: tu-secret-key-aqui
  BREVO_API_KEY: tu-api-key  # Opcional
  BREVO_SENDER_EMAIL: tu-email@ejemplo.com  # Opcional
```

#### PostgreSQL (servicio `postgres`):

```yaml
environment:
  POSTGRES_DB: clinicagallegos_bd
  POSTGRES_USER: postgres
  POSTGRES_PASSWORD: admin
```

### Cambiar Puerto de la API

Para cambiar el puerto de la API (por ejemplo, a 9090):

```yaml
api:
  ports:
    - "9090:8080"  # Puerto-host:Puerto-contenedor
```

## 🔧 Desarrollo Local

### Opción 1: Usar Docker para TODO (Recomendado)

```powershell
# Iniciar servicios
docker-compose up -d --build

# Los cambios de código requieren reconstruir
docker-compose up -d --build api
```

### Opción 2: PostgreSQL en Docker, API en tu IDE

Si prefieres ejecutar la API desde IntelliJ/Eclipse:

```powershell
# Solo iniciar PostgreSQL
docker-compose up -d postgres

# Luego ejecuta la API desde tu IDE (Run/Debug)
# La API se conectará a PostgreSQL en localhost:5432
```

## 🧪 Testing

### Probar Endpoints con cURL

```powershell
# Listar servicios
curl http://localhost:8080/api/servicios

# Registrar usuario
curl -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{\"nombre\":\"Juan\",\"apellido\":\"Pérez\",\"email\":\"juan@test.com\",\"contrasena\":\"password123\",\"telefono\":\"123456789\"}'

# Login
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"juan@test.com\",\"contrasena\":\"password123\"}'
```

## 🐛 Solución de Problemas

### La API no inicia

```powershell
# Ver logs detallados
docker-compose logs -f api

# Verificar que PostgreSQL esté listo
docker-compose logs postgres | Select-String "ready to accept connections"
```

### PostgreSQL no se conecta

```powershell
# Verificar estado del contenedor
docker-compose ps postgres

# Verificar health check
docker inspect clinica-gallegos-postgres --format='{{.State.Health.Status}}'

# Reiniciar PostgreSQL
docker-compose restart postgres
```

### Puerto ocupado

Si el puerto 8080 o 5432 está ocupado:

```powershell
# Ver qué está usando el puerto
netstat -ano | findstr :8080

# Cambiar el puerto en docker-compose.yml o detener el proceso
```

### Limpiar todo y empezar de nuevo

```powershell
# Detener y eliminar TODO (contenedores, volúmenes, imágenes)
docker-compose down -v --rmi all

# Reconstruir desde cero
docker-compose up -d --build
```

### Eliminar volúmenes huérfanos

```powershell
docker volume prune
```

## 📊 Monitoreo

### Health Checks

Los servicios incluyen health checks para verificar su estado:

```powershell
# Verificar estado de salud de la API
docker inspect clinica-gallegos-api --format='{{.State.Health.Status}}'

# Verificar estado de salud de PostgreSQL
docker inspect clinica-gallegos-postgres --format='{{.State.Health.Status}}'
```

### Uso de Recursos

```powershell
# Ver uso de CPU y memoria
docker stats

# Ver uso de un contenedor específico
docker stats clinica-gallegos-api
```

## 📁 Estructura de Volúmenes

Los datos persisten en volúmenes Docker:

- **postgres_data:** Datos de PostgreSQL
- **pgadmin_data:** Configuración de pgAdmin

Para hacer backup:

```powershell
# Backup de la base de datos
docker-compose exec postgres pg_dump -U postgres clinicagallegos_bd > backup.sql

# Restaurar backup
docker-compose exec -T postgres psql -U postgres clinicagallegos_bd < backup.sql
```

## 🌐 Integración con Frontend

Si tienes un frontend en React (puerto 5174), asegúrate de que CORS esté configurado en el backend.

La API ya tiene configurado CORS para:
- `http://localhost:5174`
- `http://localhost:3000`

Para agregar más orígenes, modifica `CorsConfig.java`.

## 📝 Notas Adicionales

- **Java 21:** La aplicación usa Java 21, asegúrate de que tu Dockerfile lo especifique.
- **PostgreSQL 16:** Se usa PostgreSQL 16 Alpine por ser ligero y eficiente.
- **Multi-stage Build:** El Dockerfile usa compilación multi-etapa para optimizar el tamaño de la imagen.
- **Variables de entorno:** Nunca subas credenciales reales al repositorio. Usa `.env` o variables de entorno del sistema.

## 🔐 Seguridad

Para producción, recuerda:

1. Cambiar todas las contraseñas por defecto
2. Usar variables de entorno del sistema en lugar de hardcodear valores
3. Configurar un `JWT_SECRET` fuerte y aleatorio
4. Habilitar SSL/TLS para PostgreSQL
5. Usar redes Docker privadas
6. Implementar rate limiting
7. Actualizar dependencias regularmente

## 📞 Soporte

Si encuentras problemas:

1. Revisa los logs: `docker-compose logs -f`
2. Verifica el estado: `docker-compose ps`
3. Verifica health checks
4. Limpia y reconstruye: `docker-compose down -v && docker-compose up -d --build`

---

**¡Listo!** Tu aplicación debería estar corriendo en http://localhost:8080 🎉
