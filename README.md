# CineClub+ Backend

Este es el backend para la aplicación CineClub+.

## Clonar el repositorio

Es necesario clonar el repositorio de GitHub con el siguiente comando:

```bash
git clone https://github.com/Proyectos-ITP/cineclub-back.git
cd cineclub-back
```

Nota: De momento solo estamos trabajando en la rama `dev`, por ende después de clonar el repositorio, es necesario ejecutar el siguiente comando:

```bash
git checkout dev
```

## Instalación con Docker (Recomendado)

### Prerrequisitos

- Docker
- Docker Compose

### Variables de Entorno

Crea un archivo `.env` en la raíz del proyecto con las siguientes variables:

```
APP_PORT=xxxxxxx
CORS_ALLOWED_ORIGINS=xxxxxxx
CORS_ALLOWED_METHODS=xxxxxxx
CORS_ALLOWED_HEADERS=xxxxxxx
CORS_ALLOW_CREDENTIALS=xxxxxxx
MONGO_USER=xxxxxxx
MONGO_PASSWORD=xxxxxxx
MONGO_DB=xxxxxxx
SPRING_DATA_MONGODB_URI=xxxxxxx
SPRING_REDIS_HOST=xxxxxxx
SPRING_REDIS_PORT=xxxxxxx
SPRING_REDIS_USER=xxxxxxx
SPRING_REDIS_PASSWORD=xxxxxxx
SPRING_PROFILES_ACTIVE=xxxxxxx
```

### Local

Para levantar el entorno local, ejecuta:

```bash
docker-compose -f docker-compose.local.yml up --build -d
```

Esto levantará la aplicación en modo de desarrollo con hot-reload.

### Producción

Para levantar el entorno de producción, ejecuta:

```bash
docker-compose up --build -d
```

## Comandos Útiles

### Comandos Docker

#### Levantar contenedores

```bash
# Local (con hot-reload)
docker-compose -f docker-compose.local.yml up --build -d

# Desarrollo (con hot-reload)
docker-compose -f docker-compose.development.yml up --build -d

# Producción
docker-compose up --build -d
```

#### Detener contenedores

```bash
# Local
docker-compose -f docker-compose.local.yml down

# Desarrollo
docker-compose -f docker-compose.development.yml down

# Producción
docker-compose down
```

#### Ver logs de los contenedores

```bash
# Ver logs de todos los servicios
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f app
docker-compose logs -f mongodb
docker-compose logs -f redis
```

#### Verificar estado de contenedores

```bash
docker-compose ps
```

#### Reconstruir contenedores

```bash
# Local
docker-compose -f docker-compose.local.yml up --build --force-recreate

# Desarrollo
docker-compose -f docker-compose.development.yml up --build --force-recreate

# Producción
docker-compose up --build --force-recreate
```

### Comandos para MongoDB

#### Acceder a la consola de MongoDB

```bash
# Desde Docker
docker exec -it <nombre_contenedor_mongo> mongosh -u ${MONGO_USER} -p ${MONGO_PASSWORD} --authenticationDatabase admin

# O usando docker-compose
docker-compose exec mongodb mongosh -u ${MONGO_USER} -p ${MONGO_PASSWORD} --authenticationDatabase admin
```

#### Comandos básicos de MongoDB

```javascript
// Ver bases de datos
show dbs

// Usar una base de datos
use cineclub

// Ver colecciones
show collections

// Ver documentos de una colección
db.movies.find().pretty()
db.directors.find().pretty()
db.users.find().pretty()
```

### Comandos para Redis

**Nota**: Redis requiere una consola del subsistema Linux en Windows (WSL) o ejecutarse mediante Docker.

#### Acceder a Redis CLI

```bash
# Desde Docker
docker exec -it <nombre_contenedor_redis> redis-cli -a ${REDIS_PASSWORD}

# O usando docker-compose
docker-compose exec redis redis-cli -a ${REDIS_PASSWORD}

# Si usas WSL (Windows Subsystem for Linux)
redis-cli -h localhost -p ${REDIS_PORT} -a ${REDIS_PASSWORD}
```

#### Comandos básicos de Redis

```bash
# Ver todas las claves
KEYS *

# Ver valor de una clave específica
GET nombre_clave

# Limpiar todas las claves de la base de datos actual
FLUSHDB

# Limpiar todas las bases de datos
FLUSHALL

# Ver información del servidor Redis
INFO

# Verificar conectividad
PING
```

### Comandos para la Aplicación Spring Boot

#### Ejecutar sin Docker

```bash
# Compilar el proyecto
./mvnw clean install

# Ejecutar la aplicación
./mvnw spring-boot:run

# En Windows (PowerShell o CMD)
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run
```

#### Ejecutar tests

```bash
./mvnw test

# En Windows
.\mvnw.cmd test
```

#### Generar JAR

```bash
./mvnw clean package

# En Windows
.\mvnw.cmd clean package
```

### Verificar que los servicios están corriendo

#### Verificar la aplicación

```bash
# Verificar health
curl http://localhost:${APP_PORT}/health

# O en PowerShell
Invoke-WebRequest -Uri http://localhost:${APP_PORT}/health
```

#### Verificar MongoDB

```bash
# Verificar conexión
docker-compose exec mongodb mongosh --eval "db.adminCommand('ping')"
```

#### Verificar Redis

```bash
# Verificar conexión
docker-compose exec redis redis-cli -a ${REDIS_PASSWORD} PING
```

### Tipo de Consola a Emplear

- **Docker**: Cualquier consola (CMD, PowerShell, Git Bash, WSL)
- **Redis CLI**:
  - Con Docker: Cualquier consola
  - Instalación local: WSL (Windows Subsystem for Linux) o Git Bash
- **MongoDB Shell**:
  - Con Docker: Cualquier consola
  - Instalación local: Cualquier consola con mongosh instalado
- **Maven (mvnw)**:
  - Linux/Mac: Terminal estándar
  - Windows: PowerShell, CMD o Git Bash (usar `.\mvnw.cmd` en PowerShell/CMD)

## Documentación de Endpoints

### Health & Sistema

#### GET /

Endpoint raíz del servidor.

- **Respuesta**: `"Cineclub Backend is running!"`

#### GET /health

Verifica el estado del servidor.

- **Respuesta**: `"OK"`

#### DELETE /cache/clear

Limpia el caché de la aplicación (excepto openapi:spec).

- **Respuesta**:

```json
{
  "status": "success",
  "message": "Cache cleared successfully",
  "cachesCleared": 2,
  "cachesSkipped": 1
}
```

### Películas (Movies)

Base URL: `/movies`

#### GET /movies

Obtiene un listado paginado de películas.

- **Parámetros de consulta**:
  - `title` (opcional): Filtrar por título
  - `page` (opcional): Número de página (default: 0)
  - `size` (opcional): Tamaño de página (default: 10)
  - `sort` (opcional): Campo de ordenamiento
- **Respuesta**: Objeto PagedResponseDto con lista de MovieDto

#### GET /movies/{id}

Obtiene una película por su ID.

- **Parámetros de ruta**:
  - `id`: ID de la película
- **Respuesta**: MovieDto

#### POST /movies

Crea una nueva película.

- **Body**: CreateMovieDto (validado)
- **Respuesta**: MovieDto creado

#### PATCH /movies/{id}

Actualiza una película existente.

- **Parámetros de ruta**:
  - `id`: ID de la película
- **Body**: UpdateMovieDto (validado)
- **Respuesta**: MovieDto actualizado

#### DELETE /movies/{id}

Elimina una película.

- **Parámetros de ruta**:
  - `id`: ID de la película
- **Respuesta**: Sin contenido

### Directores (Directors)

Base URL: `/directors`

#### GET /directors/movies

Obtiene un listado paginado de directores con sus películas.

- **Parámetros de consulta**:
  - `director` (opcional): Filtrar por nombre de director
  - `page` (opcional): Número de página (default: 0)
  - `size` (opcional): Tamaño de página (default: 10)
  - `sort` (opcional): Campo de ordenamiento
- **Respuesta**: Objeto PagedResponseDto con lista de DirectorDto (incluye películas)

#### GET /directors

Obtiene un listado paginado de directores.

- **Parámetros de consulta**:
  - `director` (opcional): Filtrar por nombre de director
  - `page` (opcional): Número de página (default: 0)
  - `size` (opcional): Tamaño de página (default: 10)
  - `sort` (opcional): Campo de ordenamiento
- **Respuesta**: Objeto PagedResponseDto con lista de DirectorDto

#### GET /directors/{id}

Obtiene un director por su ID.

- **Parámetros de ruta**:
  - `id`: ID del director
- **Respuesta**: DirectorDto

#### POST /directors

Crea un nuevo director.

- **Body**: CreateDirectorDto (validado)
- **Respuesta**: DirectorDto creado

#### PATCH /directors/{id}

Actualiza un director existente.

- **Parámetros de ruta**:
  - `id`: ID del director
- **Body**: UpdateDirectorDto (validado)
- **Respuesta**: DirectorDto actualizado

#### DELETE /directors/{id}

Elimina un director.

- **Parámetros de ruta**:
  - `id`: ID del director
- **Respuesta**: Sin contenido

### Usuarios (Users)

Base URL: `/api/users`

#### GET /api/users/

Obtiene todos los usuarios.

- **Respuesta**: Lista de User

#### GET /api/users/{id}

Obtiene un usuario por su ID.

- **Parámetros de ruta**:
  - `id`: ID del usuario
- **Respuesta**: User

#### GET /api/users/email/{email}

Obtiene un usuario por su email.

- **Parámetros de ruta**:
  - `email`: Email del usuario
- **Respuesta**: User

#### POST /api/users

Crea un nuevo usuario.

- **Body**:

```json
{
  "record": {
    // Datos del usuario
  }
}
```

- **Respuesta**: Sin contenido

#### PATCH /api/users/{id}

Actualiza un usuario existente.

- **Parámetros de ruta**:
  - `id`: ID del usuario
- **Body**: User
- **Respuesta**: User actualizado

#### DELETE /api/users/{id}

Elimina un usuario.

- **Parámetros de ruta**:
  - `id`: ID del usuario
- **Respuesta**: Sin contenido

## Documentación OpenAPI

Una vez que el servidor esté en ejecución, puedes acceder a la documentación interactiva de la API en:

- **SCALAR UI**: `http://cineclub-back.onrender.com/docs`
