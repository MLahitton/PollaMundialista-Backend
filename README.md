# Polla Mundialista — Backend

## 1. Descripción del Proyecto

Polla Mundialista es una aplicación para competir pronosticando los partidos del **Mundial 2026**: cada persona predice los marcadores, suma puntos según sus aciertos y compite con el resto en un ranking.

Este repositorio contiene **únicamente el backend**: la API que guarda y calcula toda la información del juego. Se encarga de:

- Manejar los **partidos** del Mundial.
- Recibir y guardar los **pronósticos** de cada persona.
- Manejar los **participantes** y su inicio de sesión.
- **Calcular los puntos** de cada pronóstico.
- Generar el **ranking** del torneo.
- Controlar la **lógica del Mundial** (cuándo abre y cierra cada partido).
- Entregar la **API** que utiliza el frontend.

El frontend no calcula nada por su cuenta: consume esta API. Por eso el backend debe estar encendido para que la aplicación funcione.

> **Repositorio del frontend:** https://github.com/MLahitton/PollaMundialista-Frontend
>
> La interfaz web del proyecto está en ese repositorio, con su propia guía de instalación.

---

## 2. Objetivo

Ofrecer la API que sostiene la Polla Mundialista: autenticar a los participantes, guardar sus pronósticos, calcular sus puntos y mantener el ranking del torneo.

---

## 3. Características Destacadas

- Inicio de sesión con Google y sesión propia mediante JWT.
- Creación automática del participante en su primer inicio de sesión.
- Torneo Mundial 2026 con 48 equipos, 12 grupos, 7 fases y 104 partidos.
- Carga de todos los datos del Mundial con un solo comando.
- Pronósticos con cierre automático 15 minutos antes de cada partido.
- Cálculo de puntos: marcador exacto, resultado correcto y bonus por equipo clasificado.
- Puntuación manual (cuando tú la ejecutas) o automática (el backend la ejecuta solo).
- Ranking del torneo con Top 10 y posición del participante.
- Reloj configurable (real o histórico) para simular el Mundial aunque sus fechas ya hayan pasado.
- Creación automática de las tablas de la base de datos al arrancar.
- API REST documentada con Swagger.

---

## 4. Tecnologías Utilizadas

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje de desarrollo |
| Spring Boot 4.1 | Framework del backend |
| Maven Wrapper | Ejecución y dependencias (ya viene incluido) |
| PostgreSQL 17 | Base de datos |
| Flyway | Creación automática de las tablas |
| Spring Data JPA | Acceso a la base de datos |
| Spring Security | Seguridad y protección de la API |
| Google Identity | Inicio de sesión con Google |
| Swagger / OpenAPI | Documentación de la API |
| JUnit | Pruebas |

---

## 5. Estructura del Proyecto

```
PollaMundialista-Backend/
├─ scripts/                    Scripts del reloj (Windows)
├─ src/
│  ├─ main/
│  │  ├─ java/com/mundialpolla/
│  │  │  ├─ auth/              Inicio de sesión con Google y sesión propia
│  │  │  ├─ dataset/           Carga de los datos del Mundial
│  │  │  ├─ groups/            Grupos del torneo
│  │  │  ├─ matches/           Partidos
│  │  │  ├─ me/                Datos del usuario que inició sesión
│  │  │  ├─ participants/      Participantes
│  │  │  ├─ predictions/       Pronósticos
│  │  │  ├─ ranking/           Ranking
│  │  │  ├─ scoring/           Cálculo de puntos
│  │  │  ├─ shared/            Configuración, seguridad y reloj
│  │  │  ├─ stages/            Fases del torneo
│  │  │  ├─ teams/             Equipos
│  │  │  └─ tournaments/       Torneos
│  │  └─ resources/
│  │     ├─ db/migration/      Migraciones que crean las tablas
│  │     ├─ datasets/          Calendario del Mundial 2026
│  │     └─ application.properties
│  └─ test/                    Pruebas del proyecto
├─ .env.example                Plantilla de configuración local
├─ mvnw / mvnw.cmd             Maven incluido en el proyecto
└─ pom.xml                     Dependencias
```

Los archivos que conviene conocer al empezar:

| Archivo | Para qué sirve |
|---|---|
| `pom.xml` | Versión de Java y dependencias del proyecto |
| `.env.example` | Plantilla de las variables necesarias para ejecutar el proyecto |
| `mvnw` / `mvnw.cmd` | Maven incluido: sirve para arrancar el backend sin instalar nada más |
| `src/main/resources/application.properties` | Configuración general (puerto, base de datos, Swagger) |
| `src/main/resources/db/migration/` | Archivos que crean las tablas automáticamente |
| `src/main/resources/datasets/world-cup-2026.json` | Calendario completo del Mundial 2026 |
| `scripts/simular-mundial.ps1` | Activa el reloj de simulación (Windows) |
| `scripts/restaurar-tiempo-real.ps1` | Vuelve al reloj real (Windows) |

---

## 6. Instalación y Configuración

Esto es lo que vas a hacer, en orden:

**Instalar → Descargar → Crear la base de datos → Configurar `.env` → Ejecutar → Cargar los datos → Activar el reloj → Verificar.**

### Paso 1 — Instalar lo necesario

Necesitas tres programas:

- **Git** — para descargar el proyecto.
- **Java 21** — para poder ejecutar el backend.
- **PostgreSQL 17** — la base de datos donde se guarda todo.

**No necesitas instalar Maven.** El proyecto ya lo trae incluido (Maven Wrapper).

#### Windows

Descargas oficiales:

- Git: https://git-scm.com/download/win
- Java 21 (Eclipse Temurin): https://adoptium.net/temurin/releases/?version=21
- PostgreSQL 17: https://www.postgresql.org/download/windows/

Al instalar PostgreSQL: mantén el puerto **5432**, conserva el usuario **postgres** y **guarda la contraseña que definas** (la necesitarás en el Paso 4).

Cierra y vuelve a abrir la terminal, y comprueba que todo quedó bien:

```powershell
git --version
java -version
psql --version
```

`java -version` debe indicar **21**.

#### macOS

Con [Homebrew](https://brew.sh):

```bash
brew install git
brew install --cask temurin@21
brew install postgresql@17
brew services start postgresql@17
```

Comprueba que todo quedó bien:

```bash
git --version
java -version
psql --version
```

En macOS, el usuario de PostgreSQL suele ser tu propio usuario y no `postgres`. Si es tu caso, tenlo en cuenta en el Paso 4.

---

### Paso 2 — Descargar el proyecto

```bash
git clone https://github.com/MLahitton/PollaMundialista-Backend.git
cd PollaMundialista-Backend
```

---

### Paso 3 — Crear la base de datos

Solo tienes que crear una base de datos **vacía**. Las tablas se crean solas la primera vez que arranques el backend.

```bash
psql -U postgres -c "CREATE DATABASE polla_mundialista;"
```

También puedes crearla desde DBeaver o pgAdmin: clic derecho en *Databases* → *Create New Database* → nombre `polla_mundialista`.

> **No crees tablas a mano.** El proyecto usa Flyway, que crea las 9 tablas automáticamente al arrancar.

---

### Paso 4 — Configurar el archivo `.env`

El proyecto ya trae el archivo `.env.example` con las variables que necesita. **No las crees desde cero: copia ese archivo.**

En PowerShell (Windows):

```powershell
Copy-Item .env.example .env
```

En Git Bash / macOS / Linux:

```bash
cp .env.example .env
```

Abre el archivo `.env` que acabas de crear. Solo tienes que completar **dos** variables:

```env
DB_PASSWORD=tu_contraseña_de_postgresql
JWT_SECRET=
```

**`DB_PASSWORD`** es la contraseña que definiste al instalar PostgreSQL.

**`JWT_SECRET`** es la clave con la que el backend mantiene la sesión de los usuarios. Cada persona genera la suya; no hay que compartirla. Debe ser un texto en Base64 de al menos 32 bytes, así que **no la inventes a mano**: genérala con este comando, que la añade sola al archivo.

En PowerShell (Windows):

```powershell
$bytes = New-Object byte[] 48
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
"JWT_SECRET=" + [Convert]::ToBase64String($bytes) | Out-File -Append -Encoding ascii .env
$rng.Dispose()
```

En macOS / Linux:

```bash
echo "JWT_SECRET=$(openssl rand -base64 48)" >> .env
```

> Una vez generada, **no la cambies**. Si la cambias, todas las sesiones abiertas dejan de funcionar.

El resto de variables ya vienen configuradas y **no necesitas tocarlas**:

| Variable | Para qué sirve |
|---|---|
| `GOOGLE_CLIENT_ID` | Identifica la aplicación ante Google. Ya viene con el valor del proyecto |
| `DB_URL` y `DB_USERNAME` | Conexión a PostgreSQL. Cámbialas solo si no usas los valores por defecto |
| `CORS_ALLOWED_ORIGINS` | Direcciones desde las que el backend acepta peticiones (por defecto `http://localhost:3000`) |
| `AUTO_SCORING_ENABLED` | Ya viene en `false`, que es lo recomendado para desarrollo (ver sección 9) |

> **Importante:** `.env` no se sube a GitHub (está en `.gitignore`). Nunca compartas tu `JWT_SECRET` ni tu contraseña de PostgreSQL.

> **No confundas estas tres cosas:** el **Google Client ID** identifica la aplicación ante Google y ya viene configurado. El **Google Client Secret** es otra cosa distinta y **este backend no lo usa nunca**. El **JWT_SECRET** es la clave interna del backend, no tiene nada que ver con Google.

> Si dejas una variable escrita pero vacía (por ejemplo `DB_URL=`), el backend puede no arrancar. Si no la necesitas, déjala comentada con `#`.

---

### Paso 5 — Ejecutar el backend

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

En macOS / Linux:

```bash
./mvnw spring-boot:run
```

La primera vez tarda varios minutos porque descarga las dependencias.

Cuando termine verás en la terminal:

```
Successfully applied 7 migrations to schema "public", now at version v7
Tomcat started on port 8080 (http)
Started MundialBackendApplication
```

Esa primera línea es Flyway creando las tablas por ti. El backend queda disponible en `http://localhost:8080`.

Para detenerlo, presiona `Ctrl + C` en la terminal.

> Deja esta terminal abierta. Para los pasos siguientes, abre **otra terminal**.

---

### Paso 6 — Cargar los datos del Mundial

Las tablas ya existen, pero están vacías. El proyecto incluye el calendario completo del Mundial 2026, así que **no tienes que crear ningún equipo ni partido a mano**: basta con una petición.

Con el backend encendido, en otra terminal:

En PowerShell (Windows):

```powershell
Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/v1/internal/dataset/world-cup-2026/import"
```

En macOS / Linux:

```bash
curl -X POST http://localhost:8080/api/v1/internal/dataset/world-cup-2026/import
```

También puedes hacerlo desde Swagger, en `http://localhost:8080/swagger-ui.html`.

Debe responder con algo así:

```json
{
  "tournamentsCreated": 1,
  "teamsCreated": 48,
  "stagesCreated": 7,
  "groupsCreated": 12,
  "groupTeamsCreated": 48,
  "matchesCreated": 104
}
```

Es decir: **1 torneo, 48 equipos y 104 partidos**.

> Puedes ejecutarlo las veces que quieras: no duplica ni borra nada.

---

### Paso 7 — Activar el reloj para ver los partidos

Si consultas los próximos partidos ahora, la lista vendrá **vacía**. Es normal y no es un error.

El Mundial 2026 se juega entre el **11 de junio** y el **19 de julio de 2026**. Como esas fechas ya pasaron, para el backend no queda ningún partido "próximo". Los 104 partidos siguen guardados; simplemente están todos en el pasado.

Para poder probar la aplicación, el backend puede simular que estamos justo antes del Mundial.

En Windows, con el backend encendido:

```powershell
.\scripts\simular-mundial.ps1
```

En macOS / Linux:

```bash
curl -X POST http://localhost:8080/api/v1/internal/clock/historical \
  -H "Content-Type: application/json" \
  -d '{"instant":"2026-06-10T12:00:00Z"}'
```

Listo: los 104 partidos aparecerán como próximos y podrás pronosticarlos.

> **Al reiniciar el backend, el reloj vuelve al modo real** y la lista volverá a estar vacía. No se pierde ningún dato: solo tienes que volver a ejecutar este paso. Más detalles en la sección 8.

---

### Paso 8 — Verificar que funciona

Comprueba estas direcciones en el navegador o con `curl`:

| Qué compruebas | Dirección | Qué debe mostrar |
|---|---|---|
| El backend responde | http://localhost:8080/actuator/health | `"status":"UP"` |
| Documentación de la API | http://localhost:8080/swagger-ui.html | La página de Swagger |
| El torneo se cargó | http://localhost:8080/api/v1/tournaments/active | *World Cup 2026* |
| Los partidos se cargaron | http://localhost:8080/api/v1/matches | Los 104 partidos |
| El reloj está simulando | http://localhost:8080/api/v1/internal/clock | `"mode":"HISTORICAL_REPLAY"` |

Si todo eso responde correctamente, la instalación está lista.

---

## 7. Comandos disponibles

Desde la carpeta del proyecto. En Windows usa `.\mvnw.cmd`; en macOS / Linux, `./mvnw`.

| Comando | Qué hace |
|---|---|
| `.\mvnw.cmd spring-boot:run` | Ejecuta el backend (puerto 8080) |
| `.\mvnw.cmd clean test` | Compila y ejecuta las pruebas |
| `.\mvnw.cmd clean package` | Genera el archivo `.jar` |
| `.\scripts\simular-mundial.ps1` | Activa el reloj de simulación (Windows) |
| `.\scripts\restaurar-tiempo-real.ps1` | Vuelve al reloj real (Windows) |

> `clean test` no solo descarga dependencias: enciende la aplicación completa para probarla. Necesita PostgreSQL encendido, la base de datos creada y el `.env` ya configurado.

---

## 8. El reloj del Mundial

El backend tiene un reloj con dos modos:

- **REAL** — usa la fecha y hora actuales. Es el modo normal, y el que se activa siempre al arrancar.
- **HISTORICAL_REPLAY** — simula que estamos en una fecha del Mundial, para poder probar el torneo.

Sirve para resolver un problema práctico: como las fechas del Mundial 2026 ya pasaron, sin simulación no aparecería ningún partido próximo.

El reloj decide qué partidos ya se jugaron y cuáles no. Es la pieza que mueve toda la simulación:

- Si el reloj está **antes** de un partido, ese partido está por jugarse y se puede pronosticar.
- Si el reloj está **después** de un partido, ese partido ya terminó y puede generar puntos.

### Cómo controlarlo

| Acción | Windows | macOS / Linux |
|---|---|---|
| Ver el modo actual | `curl http://localhost:8080/api/v1/internal/clock` | igual |
| Simular el Mundial | `.\scripts\simular-mundial.ps1` | `curl -X POST .../clock/historical` con el instante en el cuerpo |
| Volver al modo real | `.\scripts\restaurar-tiempo-real.ps1` | `curl -X POST .../clock/real` |

Por defecto la simulación se sitúa en **2026-06-10**, un día antes del primer partido. Para avanzar a otra fecha del Mundial, indica el instante que quieras.

En Windows:

```powershell
.\scripts\simular-mundial.ps1 -Instant "2026-06-20T12:00:00Z"
```

En macOS / Linux:

```bash
curl -X POST http://localhost:8080/api/v1/internal/clock/historical \
  -H "Content-Type: application/json" \
  -d '{"instant":"2026-06-20T12:00:00Z"}'
```

**Qué pasa al reiniciar el backend:** el modo del reloj vuelve siempre a **REAL**. Los datos no se borran y los 104 partidos siguen ahí; lo único que cambia es el reloj. Si quieres seguir viendo los partidos, vuelve a ejecutar la simulación.

---

## 9. Cómo hacer que el ranking funcione

El ranking muestra a los participantes que **ya tienen partidos puntuados**. Si nadie tiene puntos todavía, aparece vacío.

Para que el ranking se llene hacen falta tres cosas, en este orden:

1. **Que haya pronósticos guardados.** Alguien tiene que haber pronosticado desde el frontend, con el reloj antes del partido.
2. **Que el reloj esté después de esos partidos.** Solo los partidos ya terminados pueden generar puntos.
3. **Que se ejecute la puntuación (scoring).** Es el paso que convierte los pronósticos en puntos.

### Paso a paso durante una simulación

**1) Sitúa el reloj antes del Mundial y guarda pronósticos**

```powershell
.\scripts\simular-mundial.ps1
```

Entra al frontend en `http://localhost:3000` y guarda algunos pronósticos.

**2) Avanza el reloj a una fecha en la que ya se hayan jugado partidos**

```powershell
.\scripts\simular-mundial.ps1 -Instant "2026-06-20T12:00:00Z"
```

**3) Ejecuta la puntuación**

En PowerShell (Windows):

```powershell
Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/v1/internal/scoring/run"
```

En macOS / Linux:

```bash
curl -X POST http://localhost:8080/api/v1/internal/scoring/run
```

Responde con cuántos partidos encontró y cuántos procesó:

```json
{
  "asOf": "2026-06-20T12:00:00Z",
  "candidates": 32,
  "processed": 32,
  "failed": 0
}
```

**4) Mira el ranking**

En el frontend, entra a `http://localhost:3000/ranking`. También puedes consultarlo directamente: primero pide el torneo activo en `http://localhost:8080/api/v1/tournaments/active`, copia su `id` y abre:

```
http://localhost:8080/api/v1/rankings/tournaments/EL_ID_DEL_TORNEO
```

Repite los pasos 2 y 3 cada vez que quieras avanzar en el Mundial: el ranking se irá llenando a medida que avancen los partidos y se ejecute el scoring.

### Puntuación manual o automática

| Valor en `.env` | Qué pasa |
|---|---|
| `AUTO_SCORING_ENABLED=false` | Tú decides cuándo puntuar, con `POST /api/v1/internal/scoring/run`. **Es lo recomendado para desarrollo** y es como viene el `.env.example` |
| `AUTO_SCORING_ENABLED=true` | El backend puntúa solo cada minuto, sin que tengas que pedirlo |

> Se recomienda `false` en desarrollo para que no se puntúen de golpe todos los partidos al arrancar, y para que puedas controlar el ritmo de la simulación.

### Si el ranking aparece vacío

Revisa esto en orden:

1. **¿El backend está encendido?** Comprueba `http://localhost:8080/actuator/health`.
2. **¿Hay datos del Mundial?** Si no hay partidos, ejecuta el Paso 6 de la instalación.
3. **¿Hay pronósticos guardados?** Sin pronósticos no hay puntos que calcular.
4. **¿El reloj está después de algún partido?** Con el reloj antes del inicio del Mundial es **normal** que el ranking esté vacío: todavía no ha terminado ningún partido.
5. **¿Ejecutaste el scoring?** Lanza `POST /api/v1/internal/scoring/run` y revisa que `processed` sea mayor que cero.

---

## 10. Problemas frecuentes

| Problema | Solución |
|---|---|
| La lista de próximos partidos viene vacía | Es normal con el reloj en modo real. Ejecuta el Paso 7 |
| No aparece ningún equipo ni partido | Falta cargar los datos del Mundial. Ejecuta el Paso 6 |
| El ranking aparece vacío | Comprueba que el reloj esté después de algún partido y que hayas ejecutado el scoring. Ver sección 9 |
| El scoring responde `processed: 0` | El reloj todavía está antes de los partidos, o esos partidos ya estaban puntuados |
| `Port 8080 was already in use` | Ya tienes otro backend encendido. Ciérralo con `Ctrl + C` antes de arrancar de nuevo |
| `JWT_SECRET must be a valid Base64 secret...` | El `JWT_SECRET` no es válido. Genéralo con el comando del Paso 4, sin escribirlo a mano |
| `GOOGLE_CLIENT_ID must be configured` | Dejaste esa variable vacía en el `.env`. Coméntala con `#` para usar el valor del proyecto |
| Error de conexión con PostgreSQL | Comprueba que PostgreSQL esté encendido, que exista la base `polla_mundialista` y que `DB_PASSWORD` sea correcta |
| `java -version` no muestra 21 | Instala Java 21 y reinicia la terminal. En Windows revisa además `JAVA_HOME` |
| El login falla con error 401 | El Client ID de Google del backend y el del frontend deben ser el mismo |
| El frontend no carga datos | El backend debe estar en `http://localhost:8080` y el frontend en `http://localhost:3000` |
| `Filename too long` al clonar en Windows | Ejecuta `git config --global core.longpaths true` y vuelve a clonar |

> Después de cambiar el archivo `.env`, detén el backend con `Ctrl + C` y vuelve a arrancarlo.

---

## 11. Autores

Proyecto desarrollado por:

- **Manuel José Gómez Laiton**
- **Valentina Mancilla**
- **Tomas Esteban González Quintero**
- **Sara Brigete Carlier Méndez**
- **luis**

Polla Mundialista 2026, proyecto universitario desarrollado en el marco de Globant.
