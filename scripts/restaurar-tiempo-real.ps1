<#
.SYNOPSIS
    Devuelve el reloj de la aplicacion al modo REAL.

.DESCRIPTION
    Utilidad de DESARROLLO. Deshace lo que hace simular-mundial.ps1.
    Solo llama a un endpoint que ya existe; no modifica codigo, ni base
    de datos, ni fechas de partidos.

    Nota: reiniciar el backend tiene el mismo efecto, porque el modo del
    reloj vive solo en memoria y arranca siempre en REAL.

    Con el reloj en REAL y el dataset del Mundial 2026 (junio-julio 2026)
    ya en el pasado, /matches/upcoming devuelve [] correctamente.

.PARAMETER BaseUrl
    URL del backend. Por defecto http://localhost:8080

.EXAMPLE
    .\scripts\restaurar-tiempo-real.ps1
#>

[CmdletBinding()]
param(
    [string] $BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"
$TimeoutSec = 20

Write-Host ""
Write-Host "Restaurando el reloj a tiempo real" -ForegroundColor White
Write-Host "Backend: $BaseUrl"
Write-Host ""

# 1. Comprobar que el backend este disponible.
try {
    $health = Invoke-RestMethod -Uri "$BaseUrl/actuator/health" -TimeoutSec 10
} catch {
    Write-Host "El backend no esta ejecutandose. Inicia Spring Boot primero." -ForegroundColor Red
    Write-Host ""
    Write-Host "Desde la raiz del backend:" -ForegroundColor DarkGray
    Write-Host "    .\mvnw.cmd spring-boot:run" -ForegroundColor DarkGray
    Write-Host ""
    Write-Host "(Si el backend esta apagado no hace falta este script: al arrancar" -ForegroundColor DarkGray
    Write-Host " de nuevo el reloj ya estara en REAL.)" -ForegroundColor DarkGray
    Write-Host ""
    exit 1
}

if ($health.status -ne "UP") {
    Write-Host "El backend responde pero no esta sano (status: $($health.status))." -ForegroundColor Red
    exit 1
}

# 2. Volver a tiempo real.
try {
    $null = Invoke-RestMethod -Uri "$BaseUrl/api/v1/internal/clock/real" `
        -Method Post -TimeoutSec $TimeoutSec
} catch {
    Write-Host "No fue posible restaurar el reloj." -ForegroundColor Red
    Write-Host "  Endpoint : POST $BaseUrl/api/v1/internal/clock/real"
    Write-Host "  Error    : $($_.Exception.Message)"
    exit 1
}

# 3. Confirmar el modo.
$clock = Invoke-RestMethod -Uri "$BaseUrl/api/v1/internal/clock" -TimeoutSec $TimeoutSec

Write-Host ("  {0,-18} {1}" -f "Modo :", $clock.mode)
Write-Host ("  {0,-18} {1}" -f "Hora actual :", $clock.currentTime)
Write-Host ""

if ($clock.mode -ne "REAL") {
    Write-Host "El reloj no quedo en modo REAL (modo actual: $($clock.mode))." -ForegroundColor Red
    exit 1
}

Write-Host "Reloj restaurado a tiempo real." -ForegroundColor Green
Write-Host ""
Write-Host "Con el dataset del Mundial 2026 en el pasado, /matches/upcoming" -ForegroundColor DarkGray
Write-Host "devolvera [] y el frontend mostrara 'No hay partidos proximos'." -ForegroundColor DarkGray
Write-Host "Eso es lo esperado en modo REAL, no un error." -ForegroundColor DarkGray
Write-Host ""
exit 0
