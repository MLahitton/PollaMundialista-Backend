# Polla Mundialista 2026 — Backend

Guía de instalación, configuración y ejecución del **backend** desde un computador limpio.

Repositorio: https://github.com/MLahitton/PollaMundialista-Backend

Este documento cubre únicamente el backend. El frontend tiene su propio repositorio y su
propia documentación.

Este proyecto **no usa Docker**.

---

## 1. Descripción del proyecto

Polla Mundialista 2026 es una aplicación para organizar una polla (quiniela) amistosa sobre la
Copa Mundial de la FIFA 2026 entre los integrantes de un equipo.

Este repositorio contiene el **backend**: una API REST que se encarga de:

- **Autenticación.** Recibe un *Google ID Token*, lo valida contra las claves públicas de
  Google y emite su **propio JWT**, que es el que autoriza el resto de peticiones.
- **Participantes.** En el primer acceso de una cuenta de Google crea automáticamente su
  *Participant*; en los siguientes reutiliza el mismo, con el mismo identificador interno.
- **Torneo y partidos.** Carga el Mundial 2026 desde un dataset incluido en el repositorio:
  1 torneo, 48 selecciones, 12 grupos, 7 fases y 104 partidos.
- **Pronósticos.** Registra el marcador que predice cada participante y controla la ventana de
  edición, que se cierra automáticamente 15 minutos antes del inicio de cada partido.
- **Puntuación.** Calcula marcador exacto, resultado correcto y bonus por equipo clasificado.
- **Ranking.** Construye la tabla del torneo con el Top 10 y la posición del participante.
- **Reloj configurable.** Permite simular una fecha pasada para poder probar el torneo
  (ver sección 12).

---

## 2. Objetivo

Ofrecer la API sobre la que se apoya la Polla Mundialista: autenticar a los participantes,
guardar sus pronósticos, calcular sus puntos y mantener el ranking del torneo.

---

## 3. Qué ofrece el backend

- Login con Google mediante validación de ID Token y emisión de JWT propio.
- Consulta del torneo activo, selecciones, fases, grupos y calendario completo.
- Listado de partidos próximos según el reloj de la aplicación.
- Alta y edición de pronósticos, con control de la ventana de cierre.
- Consulta de los pronósticos propios y de los pronósticos públicos de otros participantes.
- Cálculo de puntos: marcador exacto, resultado correcto y bonus por equipo clasificado.
- Ranking del torneo con Top 10 y posición del participante autenticado.
- Importador del dataset oficial del Mundial 2026, idempotente.
- Reloj configurable en modo real o histórico, para simulación.
- Migraciones automáticas de base de datos con Flyway.
- Documentación interactiva de la API con Swagger UI.

---

## 4. Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje |
| Spring Boot 4.1 | Framework de la aplicación |
| Spring Data JPA / Hibernate | Persistencia |
| Spring Security | Seguridad |
| Spring OAuth2 Resource Server | Validación de JWT |
| Spring Validation | Validación de peticiones |
| Maven (Maven Wrapper) | Build y ejecución |
| PostgreSQL 17 | Base de datos |
| Flyway | Migraciones de base de datos |
| springdoc-openapi 3.0.3 | Swagger UI / OpenAPI |
| Spring Boot Actuator | Health checks |
| Google Identity | Validación de los ID Token de Google |

---

## 5. Requisitos previos

Para el backend necesitas **Git**, **Java 21** y **PostgreSQL 17**.

**No necesitas instalar Maven**: el repositorio incluye Maven Wrapper (`mvnw` y `mvnw.cmd`),
que descarga por sí solo la versión correcta.

### 5.1 Windows

#### Git — descargar el código

Descarga desde https://git-scm.com/download/win y mantén las opciones por defecto.

```powershell
git --version
```

Si vas a clonar dentro de una carpeta muy anidada, habilita antes las rutas largas. Algunos
archivos del proyecto tienen rutas profundas y Windows limita a 260 caracteres:

```powershell
git config --global core.longpaths true
```

#### Java JDK 21 — ejecutar el backend

Se recomienda Eclipse Temurin 21: https://adoptium.net/temurin/releases/?version=21

```powershell
winget install EclipseAdoptium.Temurin.21.JDK
```

Cierra y vuelve a abrir PowerShell. Verifica:

```powershell
java -version
javac -version
```

Ambos deben indicar **21**. Si aparece otra versión, revisa `JAVA_HOME` y el orden del `PATH`:

```powershell
$env:JAVA_HOME
where.exe java
```

`JAVA_HOME` debe apuntar a algo como `C:\Program Files\Eclipse Adoptium\jdk-21.x.x.x-hotspot`.

#### PostgreSQL 17 — base de datos

Descarga desde https://www.postgresql.org/download/windows/

Durante la instalación:

- mantén el puerto **5432**;
- conserva el usuario administrador **postgres**;
- **guarda la contraseña que definas**: la necesitarás en la sección 8;
- instala las herramientas de línea de comandos cuando el instalador las ofrezca.

```powershell
psql --version
```

Si `psql` no se reconoce, el binario suele estar en `C:\Program Files\PostgreSQL\17\bin`.
Puedes añadir esa carpeta al `PATH` o usar DBeaver en su lugar.

#### Comprobar el Maven Wrapper

```powershell
.\mvnw.cmd --version
```

Debe mostrar Maven y **Java 21**.

#### Herramientas recomendadas (opcionales)

- **DBeaver Community** (https://dbeaver.io/download/) para inspeccionar la base de datos.
- **Visual Studio Code** (https://code.visualstudio.com) con *Extension Pack for Java* y
  *Spring Boot Extension Pack*.

### 5.2 macOS

Se asume [Homebrew](https://brew.sh) instalado.

```bash
# Git
brew install git
git --version

# Java 21
brew install --cask temurin@21
java -version
javac -version

# PostgreSQL 17
brew install postgresql@17
brew services start postgresql@17
psql --version
```

`java -version` y `javac -version` deben indicar **21**.

Con Homebrew, el usuario de PostgreSQL por defecto es tu usuario de macOS y no `postgres`.
Si es tu caso, tendrás que ajustar `DB_USERNAME` en la sección 8.

La primera vez puede hacer falta dar permiso de ejecución al wrapper:

```bash
chmod +x mvnw
./mvnw --version
```

### 5.3 Diferencias de comandos entre sistemas

| Acción | Windows (PowerShell) | macOS / Linux |
|---|---|---|
| Maven Wrapper | `.\mvnw.cmd` | `./mvnw` |
| Copiar la plantilla de entorno | `Copy-Item .env.example .env` | `cp .env.example .env` |
| Permiso de ejecución del wrapper | no aplica | `chmod +x mvnw` |
| Scripts del reloj | `.\scripts\*.ps1` | no disponibles, ver sección 12.5 |

El **código del proyecto es idéntico** en ambos sistemas: solo cambian los comandos.

---

## 6. Clonar el repositorio

```powershell
git clone https://github.com/MLahitton/PollaMundialista-Backend.git
cd PollaMundialista-Backend
```

La rama principal es `main`.

```powershell
git status
git branch --show-current
```

---

## 7. Crear la base de datos

El backend crea las **tablas** automáticamente, pero la **base de datos** debe existir antes.
Flyway no puede crear la base que va a migrar.

**Opción A — línea de comandos**

```bash
psql -U postgres -c "CREATE DATABASE polla_mundialista;"
```

**Opción B — DBeaver**

Conéctate con el usuario `postgres`, clic derecho en *Databases* → *Create New Database* →
nombre `polla_mundialista`.

**No crees tablas, relaciones ni índices a mano.** De eso se encarga Flyway (sección 10).

---

## 8. Configurar las variables de entorno

El repositorio no contiene contraseñas ni secretos. Cada desarrollador configura los suyos
**una sola vez** en un archivo `.env` en la raíz del backend.

`.env` está en `.gitignore` y nunca se sube. Lo carga automáticamente `application.properties`:

```properties
spring.config.import=optional:file:./.env[.properties]
```

No tienes que exportar variables cada vez que abres una terminal.

### 8.1 Crear tu `.env`

```powershell
Copy-Item .env.example .env      # Windows
```

```bash
cp .env.example .env             # macOS / Linux
```

`.env.example` documenta cada variable. Nunca escribas valores reales en `.env.example`.

> **Cuidado con los valores vacíos.** Si dejas una variable escrita pero sin valor
> (por ejemplo `DB_URL=`), ese valor vacío **sobrescribe** el valor por defecto del proyecto y
> el backend puede no arrancar. Si no necesitas una variable, déjala comentada.

### 8.2 Variables

| Variable | Obligatoria | Valor por defecto | Para qué sirve |
|---|---|---|---|
| `DB_PASSWORD` | **Sí** | — | Contraseña de tu PostgreSQL |
| `JWT_SECRET` | **Sí** | — | Firma los JWT que emite el backend |
| `AUTO_SCORING_ENABLED` | No | `true` | Scheduler de puntuación automática |
| `GOOGLE_CLIENT_ID` | No | Client ID del proyecto | Valida el Google ID Token |
| `DB_URL` | No | `jdbc:postgresql://localhost:5432/polla_mundialista` | Conexión JDBC |
| `DB_USERNAME` | No | `postgres` | Usuario de PostgreSQL |
| `CORS_ALLOWED_ORIGINS` | No | `http://localhost:3000` | Orígenes permitidos por CORS |
| `JWT_ISSUER` | No | `mundial-polla` | Claim `iss` del token |
| `JWT_AUDIENCE` | No | `mundial-polla-api` | Claim `aud` del token |
| `JWT_TTL` | No | `PT8H` | Vigencia del token |
| `AUTO_SCORING_FIXED_DELAY` | No | `PT1M` | Frecuencia del scheduler |

En la práctica solo necesitas rellenar **`DB_PASSWORD`** y **`JWT_SECRET`**.

### 8.3 PostgreSQL

```properties
DB_PASSWORD=tu_password_local_de_postgres
```

Si instalaste PostgreSQL con Homebrew en macOS y tu usuario no es `postgres`, descomenta
también `DB_USERNAME` con tu usuario.

### 8.4 Generar tu `JWT_SECRET`

`JWT_SECRET` es el secreto **interno** con el que este backend firma los JWT que emite después
de validar el Google ID Token. Cada desarrollador genera el suyo; no hace falta compartirlo
entre computadores.

**Requisito real del proyecto** (validado en `AppJwtConfig`): Base64 **estándar** de al menos
**32 bytes**. El algoritmo de firma es HMAC-SHA256 (HS256). No existe valor por defecto: si
falta, el backend falla al arrancar en lugar de firmar con un secreto conocido.

Los comandos siguientes generan 48 bytes.

Windows (PowerShell), lo añade al `.env` sin mostrarlo en pantalla:

```powershell
$bytes = New-Object byte[] 48
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
"JWT_SECRET=" + [Convert]::ToBase64String($bytes) | Out-File -Append -Encoding ascii .env
$rng.Dispose()
```

macOS / Linux:

```bash
echo "JWT_SECRET=$(openssl rand -base64 48)" >> .env
```

Debe permanecer **estable entre reinicios**: si lo cambias, todos los JWT emitidos antes dejan
de ser válidos y las peticiones autenticadas empezarán a devolver 401.

### 8.5 Google

El backend usa **únicamente** el Google Client ID, para comprobar que el ID Token recibido fue
emitido para esta aplicación. Valida además la firma contra las claves públicas de Google, el
emisor, la expiración y que el email esté verificado.

El proyecto ya trae configurado el Client ID compartido, así que **normalmente no tienes que
hacer nada**. Solo defínelo en tu `.env` si vas a usar otro cliente OAuth:

```properties
GOOGLE_CLIENT_ID=tu_client_id.apps.googleusercontent.com
```

Para obtener uno propio: Google Cloud Console → *APIs & Services* → *Credentials* → *Create
credentials* → *OAuth client ID* → tipo **Web application**. El origen desde el que se sirva la
aplicación cliente debe estar registrado como *Authorized JavaScript origin* de ese cliente;
en desarrollo local eso es `http://localhost:3000`.

El Client ID es un valor **público**: viaja al navegador y no es un secreto.

> **Tres cosas distintas que se confunden a menudo:**
>
> | | Qué es | ¿Lo usa este backend? |
> |---|---|---|
> | `GOOGLE_CLIENT_ID` | Identifica la aplicación ante Google. Público. | Sí |
> | `GOOGLE_CLIENT_SECRET` | Credencial privada de Google OAuth. | **No, nunca** |
> | `JWT_SECRET` | Secreto interno del backend para firmar sus propios JWT. | Sí |
>
> Este backend **no utiliza el Google Client Secret** en ningún punto: el flujo se basa en
> validar el ID Token, no en un intercambio de código de autorización. No lo configures.
>
> Pegar un Client Secret de Google (formato `GOCSPX-...`) en `JWT_SECRET` hace que el backend
> falle al arrancar con `Illegal base64 character 2d`, porque el guion no pertenece al
> alfabeto Base64.

### 8.6 CORS

El backend solo acepta peticiones del navegador procedentes de los orígenes configurados en
`CORS_ALLOWED_ORIGINS`. Por defecto permite `http://localhost:3000`.

Admite varios orígenes separados por coma:

```properties
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://192.168.1.100:3000
```

No se admite el comodín `*`: el backend rechaza esa configuración al arrancar.

### 8.7 Scoring automático

Durante el desarrollo se recomienda desactivar el scheduler para que no se puntúen de golpe
todos los partidos al arrancar:

```properties
AUTO_SCORING_ENABLED=false
```

Ya viene así en `.env.example`.

---

## 9. Compilar y ejecutar las pruebas

```powershell
.\mvnw.cmd clean test        # Windows
```

```bash
./mvnw clean test            # macOS / Linux
```

La primera ejecución descarga las dependencias de Maven, así que tarda varios minutos.

> **Este comando no solo descarga dependencias.** La prueba `MundialBackendApplicationTests`
> es un `@SpringBootTest` que levanta el contexto completo de Spring. Antes de ejecutarlo
> necesitas PostgreSQL en marcha, la base creada (sección 7) y tu `.env` configurado
> (sección 8). Si lo lanzas antes, fallará por conexión o configuración: eso no significa que
> el proyecto esté roto, sino que falta configurar el entorno.

Resultado esperado: `BUILD SUCCESS`.

---

## 10. Arrancar el backend y migraciones

```powershell
.\mvnw.cmd spring-boot:run   # Windows
```

```bash
./mvnw spring-boot:run       # macOS / Linux
```

El flujo en el primer arranque es:

```
PostgreSQL vacío
      ↓
iniciar el backend
      ↓
Flyway ejecuta las migraciones
      ↓
tablas creadas
```

Verás en el log cómo Flyway crea el esquema desde cero:

```
Creating Schema History table "public"."flyway_schema_history"
Migrating schema "public" to version "1 - create participants"
...
Successfully applied 7 migrations to schema "public", now at version v7
Tomcat started on port 8080 (http)
Started MundialBackendApplication
```

Las **7 migraciones** de `src/main/resources/db/migration` crean estas 9 tablas:

`participants`, `tournaments`, `teams`, `stages`, `tournament_groups`, `group_teams`,
`matches`, `predictions`, `prediction_scores`.

**No ejecutes los scripts SQL a mano.** Flyway los aplica automáticamente y lleva su propio
registro en la tabla `flyway_schema_history`. Para revisar el estado:

```sql
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Enlaces útiles con el backend en marcha:

| Recurso | URL |
|---|---|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Health | http://localhost:8080/actuator/health |

---

## 11. Importar el dataset del Mundial

Las migraciones crean la estructura, pero **no insertan datos**. El torneo, las selecciones y
el calendario se cargan con el importador incluido en el proyecto.

**El dataset ya viene en el repositorio**: `src/main/resources/datasets/world-cup-2026.json`.
No hay que crear ningún dato a mano ni descargar nada, y el importador no necesita conexión a
internet.

Con el backend arrancado, ejecuta:

```
POST /api/v1/internal/dataset/world-cup-2026/import
```

No lleva body y no requiere autenticación. Puedes lanzarlo desde Swagger UI o por terminal:

```powershell
Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/v1/internal/dataset/world-cup-2026/import"
```

```bash
curl -X POST http://localhost:8080/api/v1/internal/dataset/world-cup-2026/import
```

Resultado esperado sobre una base recién creada:

```json
{
  "tournamentsCreated": 1,
  "tournamentsUpdated": 0,
  "teamsCreated": 48,
  "teamsUpdated": 0,
  "stagesCreated": 7,
  "stagesUpdated": 0,
  "groupsCreated": 12,
  "groupsUpdated": 0,
  "groupTeamsCreated": 48,
  "matchesCreated": 104,
  "matchesUpdated": 0,
  "matchesUnchanged": 0
}
```

Es decir: **1 torneo, 48 equipos y 104 partidos**, además de 12 grupos y 7 fases.

El importador es **idempotente**: ejecutarlo de nuevo no duplica nada, solo actualiza lo que
haya cambiado, y nunca borra información.

Comprobación:

```sql
SELECT
  (SELECT COUNT(*) FROM tournaments) AS torneos,
  (SELECT COUNT(*) FROM teams)       AS equipos,
  (SELECT COUNT(*) FROM matches)     AS partidos;
```

---

## 12. El reloj de la aplicación

Esta sección es importante: sin ella parecerá que el backend está roto.

### 12.1 Modo REAL

`REAL` utiliza la fecha y hora real del sistema. **Es el modo normal del backend** y el que se
activa siempre al arrancar.

### 12.2 Modo HISTORICAL_REPLAY

`HISTORICAL_REPLAY` congela la aplicación en un instante concreto del pasado. Sirve para
simular que el backend está situado antes o durante el Mundial, de modo que los partidos
aparezcan como próximos y las ventanas de pronóstico se comporten según esa fecha simulada.

Consultar el estado en cualquier momento:

```
GET /api/v1/internal/clock
```

### 12.3 ¿Por qué no aparecen los partidos?

El dataset contiene partidos del **2026-06-11** al **2026-07-19**.

El endpoint `GET /api/v1/matches/upcoming` devuelve los partidos cuya fecha de inicio sea
**posterior** a la hora del reloj. Si el backend está en `REAL` y la fecha actual ya es
posterior al Mundial, la respuesta será `200` con una lista vacía:

```json
[]
```

**Esto no significa que los partidos se hayan eliminado.** Los 104 partidos siguen almacenados
en la base de datos y `GET /api/v1/matches` los devuelve todos. Lo único que ocurre es que
ninguno está en el futuro respecto al reloj.

### 12.4 Activar el reloj histórico (Windows)

Con el backend arrancado:

```powershell
.\scripts\simular-mundial.ps1
```

El script comprueba que el backend responda, sitúa el reloj en `2026-06-10T12:00:00Z` (un día
antes del primer partido) y muestra el modo, la fecha simulada, el torneo, la cantidad de
partidos próximos y el primero de ellos con su ventana de pronóstico. Si algo no cuadra,
imprime un diagnóstico y no cambia nada más.

Puedes situarte en otro momento del torneo:

```powershell
.\scripts\simular-mundial.ps1 -Instant "2026-06-25T12:00:00Z"
```

### 12.5 Activar el reloj histórico (macOS / Linux)

Todavía no existe un equivalente `.sh` de esos scripts. Usa el endpoint directamente:

```bash
curl -X POST http://localhost:8080/api/v1/internal/clock/historical \
  -H "Content-Type: application/json" \
  -d '{"instant":"2026-06-10T12:00:00Z"}'
```

Consultar el resultado:

```bash
curl http://localhost:8080/api/v1/internal/clock
```

Debe responder con `"mode": "HISTORICAL_REPLAY"`.

### 12.6 Volver al tiempo real

Windows:

```powershell
.\scripts\restaurar-tiempo-real.ps1
```

macOS / Linux, o desde Swagger:

```bash
curl -X POST http://localhost:8080/api/v1/internal/clock/real
```

### 12.7 Qué ocurre al reiniciar

El estado del reloj vive **solo en memoria**. Por lo tanto:

```
HISTORICAL_REPLAY
      ↓
reiniciar el backend
      ↓
REAL
```

Los datos **no** se borran. Los 104 partidos **no** se borran. Lo único que cambia es el
reloj, que vuelve a su modo por defecto.

Es intencional: `HISTORICAL_REPLAY` es una herramienta de simulación, nunca el modo de trabajo
por defecto. Si reinicias y quieres seguir viendo el Mundial, vuelve a activarlo.

---

## 13. Verificar que el backend funciona

Con el backend arrancado y el dataset importado:

**1. Estado general**

```bash
curl http://localhost:8080/actuator/health
```

Debe responder `"status":"UP"`, incluida la base de datos.

**2. Torneo activo**

```bash
curl http://localhost:8080/api/v1/tournaments/active
```

Debe devolver *World Cup 2026*. Anota el `id`: lo necesitas en el paso siguiente.

**3. Partidos**

```bash
curl "http://localhost:8080/api/v1/matches?tournamentId=<ID>"
```

Debe devolver los 104 partidos.

**4. Partidos próximos**

```bash
curl "http://localhost:8080/api/v1/matches/upcoming?tournamentId=<ID>"
```

Con el reloj en `REAL` devolverá `[]` (ver sección 12.3). Tras activar el modo histórico debe
devolver los 104.

**5. Seguridad**

```bash
curl -i http://localhost:8080/api/v1/me
```

Sin token debe responder **401**. Es la respuesta correcta: confirma que los endpoints
protegidos exigen autenticación.

**6. Swagger**

Abre http://localhost:8080/swagger-ui.html para explorar y probar toda la API.

---

## 14. Endpoints principales

Requieren cabecera `Authorization: Bearer <JWT>`:

| Endpoint | Descripción |
|---|---|
| `GET /api/v1/me` | Perfil del participante autenticado |
| `GET /api/v1/me/predictions` | Mis pronósticos |
| `PUT /api/v1/me/predictions/matches/{matchId}` | Crear o actualizar un pronóstico |
| `GET /api/v1/me/scores` | Mis puntos |
| `GET /api/v1/me/rankings/tournaments/{tournamentId}` | Ranking con mi posición |
| `GET /api/v1/me/public-predictions/matches/{matchId}` | Pronósticos visibles de un partido |

Públicos:

| Endpoint | Descripción |
|---|---|
| `POST /api/v1/auth/google` | Intercambia el Google ID Token por un JWT propio |
| `GET /api/v1/tournaments/active` | Torneo activo |
| `GET /api/v1/matches?tournamentId=...` | Todos los partidos |
| `GET /api/v1/matches/upcoming?tournamentId=...` | Partidos próximos según el reloj |
| `GET /api/v1/matches/{id}` | Detalle de un partido |

Herramientas de desarrollo (públicas en local):

| Endpoint | Descripción |
|---|---|
| `POST /api/v1/internal/dataset/world-cup-2026/import` | Importar el dataset |
| `GET /api/v1/internal/clock` | Estado del reloj |
| `POST /api/v1/internal/clock/historical` | Activar el modo histórico |
| `POST /api/v1/internal/clock/real` | Volver a la hora real |
| `POST /api/v1/internal/scoring/run` | Ejecutar un ciclo de puntuación |
| `POST /api/v1/internal/scoring/matches/{matchId}` | Puntuar un partido concreto |

La lista completa está en Swagger UI.

---

## 15. Resumen: de cero a funcionando

```
1. Instalar Git, Java 21 y PostgreSQL 17
2. git clone del repositorio
3. CREATE DATABASE polla_mundialista;
4. Copy-Item .env.example .env      (o cp)
5. Rellenar DB_PASSWORD y generar JWT_SECRET
6. .\mvnw.cmd spring-boot:run       → Flyway crea las 9 tablas
7. POST /api/v1/internal/dataset/world-cup-2026/import
     → 1 torneo, 48 equipos, 104 partidos
8. .\scripts\simular-mundial.ps1    → activa el modo histórico
9. Verificar con Swagger o curl (sección 13)
```

---

## 16. Problemas comunes

**`Port 8080 was already in use`**

Otro proceso ocupa el puerto. Identifícalo y detenlo:

```powershell
Get-NetTCPConnection -LocalPort 8080 -State Listen | ForEach-Object { Get-Process -Id $_.OwningProcess }
```

```bash
lsof -i :8080
```

**`JWT_SECRET must be a valid Base64 secret with at least 32 bytes`**

El valor de `JWT_SECRET` no es Base64 estándar o es demasiado corto. Causa habitual: haber
pegado un Google Client Secret (`GOCSPX-...`). Genera uno nuevo con la sección 8.4.

**`GOOGLE_CLIENT_ID must be configured`**

La variable está definida pero vacía. Coméntala en tu `.env` para usar el valor por defecto
del proyecto, o dale un valor real.

**Java no es la versión 21**

```powershell
java -version
where.exe java
$env:JAVA_HOME
```

Java 21 debe aparecer primero en el `PATH`.

**Error de conexión con PostgreSQL**

Comprueba que el servicio esté iniciado, que el puerto sea 5432, que la base
`polla_mundialista` exista y que `DB_PASSWORD` y `DB_USERNAME` sean correctos.

**Las peticiones autenticadas devuelven 401**

- El `GOOGLE_CLIENT_ID` del backend debe ser el mismo que usa la aplicación cliente al pedir
  el ID Token a Google.
- No has puesto un Client Secret donde iba el Client ID.
- **Comprueba que no tengas una variable de entorno del sistema sobrescribiendo el `.env`.**
  Las variables del sistema tienen prioridad sobre `.env`, así que exportar un valor de
  ejemplo hace que el backend use ese valor falso:

  ```powershell
  $env:GOOGLE_CLIENT_ID
  ```

  Debe estar vacío.

**Peticiones bloqueadas por CORS**

El origen desde el que se llama al backend debe estar en `CORS_ALLOWED_ORIGINS` (sección 8.6).

**`/matches/upcoming` devuelve `[]`**

Es lo esperado con el reloj en `REAL`. Activa el modo histórico (sección 12) o revisa que
hayas importado el dataset (sección 11).

**Al arrancar se puntúan muchos partidos**

Detén el backend y asegúrate de que tu `.env` contenga `AUTO_SCORING_ENABLED=false`.

**`Filename too long` al clonar en Windows**

```powershell
git config --global core.longpaths true
```

---

## 17. Seguridad

Nunca subas al repositorio:

- contraseñas de PostgreSQL;
- `JWT_SECRET`;
- Google Client Secrets;
- Google ID Tokens ni JWT emitidos por el backend;
- volcados de base de datos con datos personales;
- logs con credenciales.

El archivo `.env` está en `.gitignore` y debe permanecer así. Todo lo sensible se configura
mediante variables de entorno; `application.properties` no contiene ningún secreto.

---

## 18. Estructura del repositorio

```
PollaMundialista-Backend/
├─ .mvn/wrapper/           Configuración del Maven Wrapper
├─ scripts/                Utilidades de desarrollo del reloj (PowerShell)
├─ src/
│  ├─ main/
│  │  ├─ java/com/mundialpolla/
│  │  │  ├─ auth/          Validación del ID Token de Google y emisión de JWT
│  │  │  ├─ dataset/       Importador del Mundial 2026
│  │  │  ├─ matches/       Partidos
│  │  │  ├─ me/            Endpoints del participante autenticado
│  │  │  ├─ participants/  Participantes
│  │  │  ├─ predictions/   Pronósticos
│  │  │  ├─ ranking/       Ranking
│  │  │  ├─ scoring/       Cálculo de puntos
│  │  │  ├─ shared/        Configuración, seguridad, CORS y reloj
│  │  │  ├─ stages/ groups/ teams/ tournaments/
│  │  └─ resources/
│  │     ├─ db/migration/  Las 7 migraciones de Flyway
│  │     ├─ datasets/      world-cup-2026.json
│  │     └─ application.properties
│  └─ test/
├─ .env.example            Plantilla de configuración local
├─ mvnw / mvnw.cmd         Maven Wrapper
└─ pom.xml
```

---

## 19. Flujo de trabajo diario

```powershell
git pull
.\mvnw.cmd spring-boot:run
.\scripts\simular-mundial.ps1     # solo si quieres ver los partidos próximos
```

La configuración vive en `.env`, así que no hay que exportar nada.
