# Polla Mundialista 2026 — Backend

Guía de instalación, configuración y ejecución local desde un computador limpio.

Proyecto: Backend de la Polla Mundialista 2026Stack principal: Java 21, Spring Boot 4.1, PostgreSQL 17, Flyway, Spring Security, Google Identity / JWTContenedores: este proyecto no usa Docker.Sistema operativo de referencia: Windows 10/11 x64.

1. Qué necesitas instalar

Instala estas herramientas antes de clonar el repositorio.

1.1 Git

Descarga Git para Windows desde:

https://git-scm.com/install/windows

Durante la instalación puedes mantener las opciones recomendadas por defecto.

Verifica:

git --version

Debe mostrar una versión válida de Git.

1.2 Java JDK 21

El backend está desarrollado con Java 21. Se recomienda Eclipse Temurin 21.

Descarga oficial:

https://adoptium.net/temurin/releases/?version=21

También puede instalarse con winget:

winget install EclipseAdoptium.Temurin.21.JDK

Cierra y vuelve a abrir PowerShell después de instalar.

Verifica:

java -version
javac -version

Ambos deben indicar Java 21.

Si Windows sigue usando otra versión de Java, revisa JAVA_HOME y el orden del PATH.

Ejemplo de JAVA_HOME:

C:\Program Files\Eclipse Adoptium\jdk-21.x.x.x-hotspot

No es necesario instalar Maven globalmente. El repositorio incluye Maven Wrapper (mvnw / mvnw.cmd).

1.3 PostgreSQL 17

El proyecto fue desarrollado y validado con PostgreSQL 17.x.

Descarga oficial para Windows:

https://www.postgresql.org/download/windows/

Durante la instalación:

Mantén el puerto 5432, salvo que tengas una razón para cambiarlo.

Conserva el usuario administrador postgres.

Define y guarda tu contraseña local de PostgreSQL.

Instala las herramientas de línea de comandos cuando el instalador las ofrezca.

Verifica, si psql está disponible en PATH:

psql --version

Debe indicar PostgreSQL 17.x.

1.4 DBeaver Community — recomendado

No es obligatorio para que el backend funcione, pero es la herramienta recomendada para revisar la base de datos.

Descarga:

https://dbeaver.io/download/

Conexión local habitual:

Host: localhost
Port: 5432
Database: polla_mundialista
Username: postgres
Password: tu contraseña local

1.5 Visual Studio Code — recomendado

Descarga:

https://code.visualstudio.com/docs/setup/windows

Extensiones recomendadas:

Extension Pack for Java — Microsoft

Spring Boot Extension Pack — VMware

Estas extensiones facilitan IntelliSense, ejecución, Maven, JUnit y soporte de Spring Boot.

2. Clonar el repositorio

En una carpeta de trabajo, abre PowerShell:

git clone https://github.com/MLahitton/PollaMundialista-Backend.git
cd PollaMundialista-Backend

Comprueba el estado:

git status
git branch --show-current

La rama principal del proyecto es main.

Antes de comenzar a trabajar:

git pull

3. Crear la base de datos local

El backend crea las tablas mediante Flyway, pero la base de datos debe existir previamente.

Opción A — DBeaver

Conéctate a PostgreSQL con el usuario postgres.

Click derecho en Databases.

Selecciona Create New Database.

Nombre:

polla_mundialista

Guarda.

Opción B — SQL

CREATE DATABASE polla_mundialista;

No necesitas crear manualmente tablas, relaciones ni esquemas adicionales.

Cuando el backend arranca, Flyway aplica las migraciones automáticamente.

4. Variables de entorno del backend

El repositorio no contiene contraseñas ni secretos reales. Cada desarrollador configura los suyos
UNA SOLA VEZ en un archivo `.env` en la raíz del backend.

`.env` está ignorado por Git y lo carga automáticamente `application.properties` mediante:

spring.config.import=optional:file:./.env[.properties]

Esto significa que NO tienes que exportar variables cada vez que abres una terminal.
Configuras el `.env` una vez y a partir de ahí basta con:

.\mvnw.cmd spring-boot:run

4.1 Crear tu .env

Copia la plantilla versionada y complétala:

Copy-Item .env.example .env

`.env.example` documenta cada variable. Nunca escribas valores reales en `.env.example`.

4.2 PostgreSQL

En tu `.env`:

DB_PASSWORD=tu_password_local_de_postgres

Opcionalmente, el proyecto admite `DB_USERNAME` y `DB_URL`.
Si no los defines, se usan los valores locales por defecto del proyecto.

4.3 Google Client ID

Solicita al responsable del proyecto el Google OAuth Web Client ID usado por la Polla Mundialista.

En tu `.env`:

GOOGLE_CLIENT_ID=tu_client_id.apps.googleusercontent.com

El Client ID es público: viaja al navegador y no es un secreto.
El mismo valor debe usarse en el frontend como `NEXT_PUBLIC_GOOGLE_CLIENT_ID`.

IMPORTANTE: el Client ID NO es el Client Secret. Este proyecto nunca necesita el
Google Client Secret; no lo pongas en ninguna variable, ni aquí ni en el frontend.

4.4 JWT Secret local

`JWT_SECRET` es el secreto INTERNO con el que este backend firma sus propios JWT,
los que emite después de validar el Google ID Token. No tiene ninguna relación con Google:
no es el Client ID ni el Client Secret.

Requisito exigido por `AppJwtConfig`: Base64 estándar de al menos 32 bytes.
Pegar ahí un Client Secret de Google (formato `GOCSPX-...`) falla con
`Illegal base64 character 2d`, porque el guion no pertenece al alfabeto Base64.

Genera el tuyo y añádelo al `.env` sin imprimirlo en pantalla:

$bytes = New-Object byte[] 48
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
"JWT_SECRET=" + [Convert]::ToBase64String($bytes) | Out-File -Append -Encoding ascii .env
$rng.Dispose()

Debe permanecer ESTABLE entre reinicios. Si lo cambias, todas las sesiones abiertas
dejan de ser válidas y el frontend empezará a recibir 401.

No publiques JWT_SECRET en GitHub, chats, capturas ni documentación.

4.5 Scoring automático durante desarrollo

Para desarrollo y reproducción histórica se recomienda desactivar el scheduler automático.
En tu `.env`:

AUTO_SCORING_ENABLED=false

Esto evita que, al arrancar el backend en tiempo real, se procesen inmediatamente todos los partidos históricos.

Cuando queramos probar el scheduler de forma explícita podremos activarlo temporalmente.

4.6 CORS

El backend permite por defecto el frontend local:

http://localhost:3000

Si necesitas otro origen, en tu `.env`:

CORS_ALLOWED_ORIGINS=http://localhost:3000

Puede recibir varios orígenes separados por coma.

5. Resumen de variables para desarrollo

Todo vive en el archivo `.env` de la raíz del backend, que configuras UNA VEZ:

DB_PASSWORD=tu_password_local_de_postgres
GOOGLE_CLIENT_ID=tu_client_id.apps.googleusercontent.com
JWT_SECRET=tu_secreto_base64_de_32_bytes_o_mas
AUTO_SCORING_ENABLED=false

Este archivo persiste entre reinicios y entre terminales. No hay que exportar nada
manualmente antes de arrancar, y `JWT_SECRET` se mantiene estable, de modo que las
sesiones abiertas siguen siendo válidas después de reiniciar el backend.

`.env` está en `.gitignore`: nunca se sube al repositorio.

Nota: si además defines alguna de estas variables como variable de entorno del sistema,
esa tiene prioridad sobre el `.env`. Exportar un valor de ejemplo (por ejemplo
`$env:GOOGLE_CLIENT_ID = "TU_CLIENT_ID..."`) hace que el backend use ese valor falso
y que el login devuelva 401. Ante un 401 inesperado, comprueba primero:

$env:GOOGLE_CLIENT_ID

6. Primera instalación / compilación y pruebas

No necesitas instalar Maven manualmente.

Desde la raíz del backend:

.\mvnw.cmd clean test

La primera ejecución descargará las dependencias Maven necesarias.

IMPORTANTE: este comando no solo descarga dependencias. La prueba
`MundialBackendApplicationTests` es un `@SpringBootTest` que levanta el
contexto completo de Spring, así que antes de ejecutarlo necesitas:

- PostgreSQL en ejecución;
- la base `polla_mundialista` ya creada (sección 3);
- tu `.env` configurado con DB_PASSWORD y JWT_SECRET (sección 4).

Si lo lanzas antes de eso, fallará con un error de conexión o de configuración
que no significa que el proyecto esté roto: significa que aún falta configurar
el entorno.

Resultado esperado al final:

BUILD SUCCESS

Si Java no es 21, corrige Java antes de continuar.

7. Arrancar el backend

.\mvnw.cmd spring-boot:run

Resultado esperado:

Tomcat started on port 8080
Started MundialBackendApplication

Backend:

http://localhost:8080

Swagger:

http://localhost:8080/swagger-ui.html

OpenAPI JSON:

http://localhost:8080/v3/api-docs

Health:

http://localhost:8080/actuator/health

8. Qué hace Flyway en el primer arranque

Flyway valida/aplica automáticamente las migraciones en:

src/main/resources/db/migration

No ejecutes manualmente los scripts de migración en DBeaver.

Para revisar el estado:

SELECT
    installed_rank,
    version,
    description,
    success
FROM flyway_schema_history
ORDER BY installed_rank;

El esquema debe quedar actualizado a la última migración incluida en el repositorio.

9. Importar el dataset del Mundial 2026

En una base recién creada, Flyway crea la estructura pero todavía no existen los 104 partidos.

Con el backend arrancado, abre Swagger y ejecuta:

POST /api/v1/internal/dataset/world-cup-2026/import

No lleva body.

El importador usa el JSON local incluido dentro del backend; no necesita internet en runtime.

En una base vacía, el resultado esperado es aproximadamente:

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

El importador es idempotente: ejecutarlo otra vez no debe duplicar la información.

10. Probar autenticación Google

Para una prueba completa, el frontend debe estar ejecutándose en:

http://localhost:3000

El flujo es:

Frontend
→ Google Identity Services
→ Google ID Token
→ POST /api/v1/auth/google
→ backend crea/reutiliza Participant
→ backend emite JWT propio
→ GET /api/v1/me

En el primer login de una cuenta Google válida se crea automáticamente un Participant.

En los siguientes logins se reutiliza el mismo participante y el mismo UUID interno.

Puedes verificarlo en DBeaver:

SELECT
    id,
    email,
    display_name,
    is_active,
    created_at,
    last_login_at
FROM participants
ORDER BY created_at DESC;

11. Probar el reloj histórico

El sistema puede simular el Mundial en fechas pasadas.

Consultar reloj:

GET /api/v1/internal/clock

Cambiar a modo histórico:

POST /api/v1/internal/clock/historical

Body ejemplo:

{
  "instant": "2026-06-11T18:44:00Z"
}

Volver a tiempo real:

POST /api/v1/internal/clock/real

El reloj histórico es una herramienta de desarrollo/demo. Al reiniciar el backend vuelve a REAL.

11.1 Scripts para simular el Mundial

En lugar de escribir esas peticiones a mano, el repositorio incluye dos utilidades
de desarrollo en `scripts/`. Solo llaman a los endpoints anteriores: no modifican
código, ni base de datos, ni las fechas de los partidos.

### Desarrollo normal

El backend utiliza el reloj REAL con la hora actual. Ese es el comportamiento por
defecto y no hay que hacer nada para obtenerlo: cada vez que arrancas el backend
empieza en REAL, porque el modo del reloj vive solo en memoria.

Con el reloj en REAL, `GET /api/v1/matches/upcoming` devuelve `[]` y el frontend
muestra "No hay partidos próximos". **Eso es correcto**, no es un error: el dataset
del Mundial 2026 va del 11 de junio al 19 de julio de 2026, fechas que ya pasaron
respecto a la fecha actual, así que no queda ningún partido "próximo".

### Probar el Mundial

Con el backend ya arrancado, desde la raíz del backend:

.\scripts\simular-mundial.ps1

El script comprueba que el backend responda, activa HISTORICAL_REPLAY en
`2026-06-10T12:00:00Z` (un día antes del primer partido) y muestra el modo del
reloj, la fecha simulada, el torneo, la cantidad de partidos próximos y el primero
de ellos con su ventana de pronóstico. Si algo no cuadra, imprime un diagnóstico
y no cambia nada más.

Admite otro instante si quieres situarte en mitad del torneo:

.\scripts\simular-mundial.ps1 -Instant "2026-06-25T12:00:00Z"

Recuerda que al reiniciar el backend el reloj vuelve a REAL y habrá que volver a
ejecutar el script. Es intencional: HISTORICAL_REPLAY es únicamente una
herramienta de simulación para poder probar el dataset del Mundial 2026, nunca el
modo de trabajo por defecto.

### Volver al tiempo real

.\scripts\restaurar-tiempo-real.ps1

Confirma que el modo vuelve a REAL y muestra la hora actual. Reiniciar el backend
consigue exactamente lo mismo.

12. Scoring en desarrollo

Con AUTO_SCORING_ENABLED=false, puedes disparar manualmente un ciclo de scoring:

POST /api/v1/internal/scoring/run

O procesar un partido específico:

POST /api/v1/internal/scoring/matches/{matchId}

No actives el scheduler automático en una base histórica sin saber qué instante está usando ApplicationClock.

13. Verificación rápida de entorno

Ejecuta:

git --version
java -version
javac -version
psql --version

Luego:

.\mvnw.cmd --version

Debes tener:

Git operativo.

Java/Javac 21.

PostgreSQL 17.x recomendado.

Maven Wrapper operativo.

Base polla_mundialista creada.

Variables de entorno cargadas.

14. Flujo recomendado cada día

git pull

.\mvnw.cmd spring-boot:run

Eso es todo: la configuración vive en `.env` y no hay que exportar nada.

Después abre Swagger o levanta el frontend.

15. Problemas comunes

java sigue mostrando Java 8/17/otra versión

Revisa:

java -version
where.exe java
$env:JAVA_HOME

Java 21 debe aparecer primero en PATH.

DB_PASSWORD no existe

Revisa que tu archivo `.env` exista en la raíz del backend y contenga
`DB_PASSWORD` con tu contraseña de PostgreSQL. Si aún no lo has creado:

Copy-Item .env.example .env

Error de conexión PostgreSQL

Comprueba:

servicio PostgreSQL iniciado;

puerto 5432;

base polla_mundialista existente;

contraseña correcta;

usuario postgres.

Puerto 8080 ocupado

Comprueba:

netstat -ano | findstr :8080

Detén el proceso que esté ocupando el puerto antes de arrancar el backend.

Google Login devuelve 401

Comprueba que:

GOOGLE_CLIENT_ID sea el mismo que usa el frontend;

el frontend esté en un origen autorizado de Google;

no se esté usando Client Secret en lugar de Client ID.

Al arrancar se puntúan muchos partidos

Detén el backend y asegúrate de que tu `.env` contenga:

AUTO_SCORING_ENABLED=false

antes de volver a iniciar.

No aparecen partidos / "No hay partidos próximos"

Es lo esperado con el reloj en REAL: el dataset del Mundial 2026 va del
11 de junio al 19 de julio de 2026, fechas ya pasadas, así que no queda
ningún partido "próximo". Para verlos, ejecuta el simulador (sección 11.1):

.\scripts\simular-mundial.ps1

16. Archivos que nunca deben subirse al repositorio

No publiques:

contraseñas PostgreSQL;

JWT_SECRET;

Google ID Tokens;

JWT emitidos por el backend;

Client Secrets;

dumps de base con datos personales;

logs con credenciales.

Usa variables de entorno para secretos.

17. Herramientas opcionales útiles

DBeaver Community — inspección de PostgreSQL.

Postman — opcional; Swagger ya cubre la mayoría de pruebas REST.

Windows Terminal — terminal más cómoda.

VS Code + Extension Pack for Java + Spring Boot Extension Pack.

18. Checklist de instalación completa

Git instalado.

Java 21 instalado.

java -version devuelve 21.

javac -version devuelve 21.

PostgreSQL 17 instalado y en ejecución.

Base polla_mundialista creada.

Repositorio clonado.

DB_PASSWORD configurado.

GOOGLE_CLIENT_ID configurado.

JWT_SECRET generado/configurado.

AUTO_SCORING_ENABLED=false durante desarrollo histórico.

.\mvnw.cmd clean test termina en BUILD SUCCESS.

Backend inicia en puerto 8080.

Swagger abre correctamente.

Dataset importado.

Frontend puede realizar Google Login.
Variables de entorno: cuáles son compartidas y cuáles son locales

Para ejecutar el Backend, cada desarrollador debe configurar las siguientes variables de entorno.

No todas las variables deben compartirse entre integrantes del equipo.

Variables locales de cada desarrollador

Estas variables dependen del computador de cada persona y no deben compartirse.

DB_URL=jdbc:postgresql://localhost:5432/polla_mundialista
DB_USERNAME=postgres
DB_PASSWORD=TU_PASSWORD_LOCAL_DE_POSTGRES

JWT_SECRET=TU_SECRET_BASE64_LOCAL

DB_URL

Por defecto, el proyecto trabaja con:

polla_mundialista

La conexión local esperada es:

DB_URL=jdbc:postgresql://localhost:5432/polla_mundialista

Cada desarrollador debe crear esa base de datos en su propio PostgreSQL local.

La base:

polla_mundialista_test

se reserva para escenarios de prueba aislados y solo se utiliza si DB_URL se cambia explícitamente para apuntar a ella.

DB_USERNAME

En desarrollo local se utiliza normalmente:

DB_USERNAME=postgres

Si algún integrante configuró PostgreSQL con otro usuario, debe reemplazar este valor por el suyo.

DB_PASSWORD

Cada integrante debe usar la contraseña que configuró al instalar PostgreSQL en su computador.

Ejemplo:

DB_PASSWORD=TU_PASSWORD_LOCAL

No pedir ni reutilizar la contraseña de PostgreSQL de otro integrante.

JWT_SECRET

Cada desarrollador genera su propio secreto local en su `.env`.

No es necesario que todos tengan el mismo valor durante desarrollo local.

Es el secreto INTERNO con el que el backend firma sus propios JWT. No tiene
relación con Google: no es el Client ID ni el Client Secret.

Requisito: Base64 estándar de al menos 32 bytes (ver sección 4.4, que incluye
el comando de generación para Windows y para macOS/Linux).

No compartir este valor públicamente ni subirlo a GitHub.

Variable compartida por el proyecto

La siguiente variable sí debe ser proporcionada por el responsable del proyecto:

GOOGLE_CLIENT_ID=CLIENT_ID_DEL_PROYECTO

Todos los desarrolladores deben utilizar el mismo GOOGLE_CLIENT_ID configurado para Polla Mundialista 2026.

El GOOGLE_CLIENT_ID no es una contraseña, pero debe mantenerse centralizado para evitar que cada integrante configure un cliente OAuth diferente.

Configuración recomendada para desarrollo

Durante desarrollo y pruebas históricas utilizar:

AUTO_SCORING_ENABLED=false

Esto evita que el scheduler procese automáticamente partidos históricos al iniciar el Backend.

El scoring puede ejecutarse manualmente cuando sea necesario mediante:

POST /api/v1/internal/scoring/run

CORS local

Para trabajar con el Frontend local:

CORS_ALLOWED_ORIGINS=http://localhost:3000

Configuración final esperada

Cada desarrollador debería tener una configuración equivalente a:

DB_URL=jdbc:postgresql://localhost:5432/polla_mundialista
DB_USERNAME=postgres
DB_PASSWORD=TU_PASSWORD_LOCAL

GOOGLE_CLIENT_ID=CLIENT_ID_COMPARTIDO_DEL_PROYECTO

JWT_SECRET=TU_SECRET_BASE64_LOCAL

AUTO_SCORING_ENABLED=false

CORS_ALLOWED_ORIGINS=http://localhost:3000

Resumen

Variable

¿Se comparte?

Responsable

DB_URL

No necesariamente

Cada desarrollador

DB_USERNAME

No necesariamente

Cada desarrollador

DB_PASSWORD

No

Cada desarrollador

JWT_SECRET

No

Cada desarrollador

GOOGLE_CLIENT_ID

Sí

Responsable del proyecto

AUTO_SCORING_ENABLED

Valor recomendado común

Equipo

CORS_ALLOWED_ORIGINS

Valor recomendado común

Equipo

Nunca subir a GitHub:

DB_PASSWORD
JWT_SECRET
Google Client Secret
JWT emitidos
Google ID Tokens

Fuentes oficiales de instalación

Eclipse Temurin / Java: https://adoptium.net/temurin/releases/?version=21

PostgreSQL Windows: https://www.postgresql.org/download/windows/

Git para Windows: https://git-scm.com/install/windows

DBeaver Community: https://dbeaver.io/download/

Visual Studio Code: https://code.visualstudio.com/docs/setup/windows

Java Extension Pack: https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack

Spring Boot Extension Pack: https://marketplace.visualstudio.com/items?itemName=vmware.vscode-boot-dev-pack
