Polla Mundialista 2026 — Backend

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

El repositorio no contiene contraseñas ni secretos reales. Cada desarrollador debe configurar sus variables localmente.

Abre PowerShell en la carpeta del backend.

4.1 PostgreSQL

$env:DB_PASSWORD = "TU_PASSWORD_LOCAL_DE_POSTGRES"

Opcionalmente, el proyecto admite:

$env:DB_USERNAME = "postgres"
$env:DB_URL = "jdbc:postgresql://localhost:5432/polla_mundialista"

Si no los defines, se usan los valores locales por defecto del proyecto.

4.2 Google Client ID

Solicita al responsable del proyecto el Google OAuth Web Client ID usado por la Polla Mundialista.

Configúralo:

$env:GOOGLE_CLIENT_ID = "TU_CLIENT_ID.apps.googleusercontent.com"

El Client ID no es el Client Secret. Nunca necesitas un Google Client Secret para ejecutar este flujo local.

El mismo Client ID deberá utilizarse en el frontend.

4.3 JWT Secret local

Cada desarrollador puede generar su propio secreto JWT local. No es necesario compartirlo entre computadores.

Ejecuta:

$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RNGCryptoServiceProvider]::Create()
$rng.GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$rng.Dispose()

Verifica únicamente la longitud, sin imprimir ni compartir el secreto:

$env:JWT_SECRET.Length

Para un secreto de 32 bytes en Base64 normalmente debe mostrar:

44

No publiques JWT_SECRET en GitHub, chats, capturas ni documentación.

4.4 Scoring automático durante desarrollo

Para desarrollo y reproducción histórica se recomienda desactivar el scheduler automático:

$env:AUTO_SCORING_ENABLED = "false"

Esto evita que, al arrancar el backend en tiempo real, se procesen inmediatamente todos los partidos históricos.

Cuando queramos probar el scheduler de forma explícita podremos activarlo temporalmente.

4.5 CORS

El backend permite por defecto el frontend local:

http://localhost:3000

Si necesitas otro origen:

$env:CORS_ALLOWED_ORIGINS = "http://localhost:3000"

Puede recibir varios orígenes separados por coma.

5. Resumen de variables para desarrollo

En una nueva terminal PowerShell, antes de arrancar el backend, configura al menos:

$env:DB_PASSWORD = "TU_PASSWORD_LOCAL_DE_POSTGRES"
$env:GOOGLE_CLIENT_ID = "TU_CLIENT_ID.apps.googleusercontent.com"
$env:JWT_SECRET = "TU_JWT_SECRET_BASE64"
$env:AUTO_SCORING_ENABLED = "false"

Estas variables existen solo en esa sesión de PowerShell. Si cierras la terminal, tendrás que configurarlas nuevamente.

6. Primera instalación / descarga de dependencias

No necesitas instalar Maven manualmente.

Desde la raíz del backend:

.\mvnw.cmd clean test

La primera ejecución descargará las dependencias Maven necesarias.

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

$env:DB_PASSWORD = "TU_PASSWORD_LOCAL"
$env:GOOGLE_CLIENT_ID = "TU_CLIENT_ID.apps.googleusercontent.com"
$env:JWT_SECRET = "TU_JWT_SECRET_BASE64"
$env:AUTO_SCORING_ENABLED = "false"

.\mvnw.cmd spring-boot:run

Después abre Swagger o levanta el frontend.

15. Problemas comunes

java sigue mostrando Java 8/17/otra versión

Revisa:

java -version
where.exe java
$env:JAVA_HOME

Java 21 debe aparecer primero en PATH.

DB_PASSWORD no existe

Configúrala en la misma terminal antes de ejecutar Maven:

$env:DB_PASSWORD = "..."

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

Detén el backend y asegúrate de definir:

$env:AUTO_SCORING_ENABLED = "false"

antes de volver a iniciar.

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

Fuentes oficiales de instalación

Eclipse Temurin / Java: https://adoptium.net/temurin/releases/?version=21

PostgreSQL Windows: https://www.postgresql.org/download/windows/

Git para Windows: https://git-scm.com/install/windows

DBeaver Community: https://dbeaver.io/download/

Visual Studio Code: https://code.visualstudio.com/docs/setup/windows

Java Extension Pack: https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack

Spring Boot Extension Pack: https://marketplace.visualstudio.com/items?itemName=vmware.vscode-boot-dev-pack