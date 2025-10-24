# CineClub+ Backend

Este es el backend para la aplicación CineClub+.

## Instalación con Docker

### Prerrequisitos

- Docker
- Docker Compose

### Variables de Entorno

Crea un archivo `.env` en la raíz del proyecto con las siguientes variables:

```
MONGO_USER=xxxx
MONGO_PASSWORD=xxxx
MONGO_DB=xxxx
REDIS_PASSWORD=xxxx
APP_PORT=xxxx
MONGO_PORT=xxxx
REDIS_PORT=xxxx
```

### Desarrollo

Para levantar el entorno de desarrollo, ejecuta:

```bash
docker-compose -f docker-compose.development.yml up --build -d
```

Esto levantará la aplicación en modo de desarrollo con hot-reload.

### Producción

Para levantar el entorno de producción, ejecuta:

```bash
docker-compose up --build -d
```
