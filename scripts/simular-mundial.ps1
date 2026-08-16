<#
.SYNOPSIS
    Activa el modo HISTORICAL_REPLAY para simular el Mundial 2026.

.DESCRIPTION
    Utilidad de DESARROLLO. Solo llama a endpoints que ya existen en el
    backend; no modifica codigo, ni base de datos, ni fechas de partidos.

    El dataset del Mundial 2026 va del 11 de junio al 19 de julio de 2026.
    Como esas fechas ya pasaron respecto a la hora real, con el reloj en
    REAL el endpoint /matches/upcoming devuelve [] correctamente. Este
    script mueve el reloj de la aplicacion a un instante anterior al primer
    partido para poder probar el torneo completo.

    El cambio vive SOLO en memoria del backend: al reiniciarlo vuelve a
    REAL por si solo. Eso es intencional y este script no lo altera.

.PARAMETER BaseUrl
    URL del backend. Por defecto http://localhost:8080

.PARAMETER Instant
    Instante simulado, en UTC ISO-8601. Por defecto 2026-06-10T12:00:00Z,
    un dia antes del primer partido.

.EXAMPLE
    .\scripts\simular-mundial.ps1

.EXAMPLE
    .\scripts\simular-mundial.ps1 -Instant "2026-06-25T12:00:00Z"
#>

[CmdletBinding()]
param(
    [string] $BaseUrl = "http://localhost:8080",
    [string] $Instant = "2026-06-10T12:00:00Z"
)

$ErrorActionPreference = "Stop"
$TimeoutSec = 20

function Write-Section {
    param([string] $Text)
    Write-Host ""
    Write-Host $Text -ForegroundColor Cyan
}

function Write-Field {
    param([string] $Label, $Value)
    Write-Host ("  {0,-22} {1}" -f "$Label :", $Value)
}

<#
    Devuelve un array real a partir de un endpoint que responde JSON array.

    No usar Invoke-RestMethod aqui: ante un body "[]" devuelve un valor que
    @() envuelve como si tuviera un elemento, y el recuento saldria 1 en vez
    de 0. Invoke-WebRequest + ConvertFrom-Json cuenta correctamente.
#>
function Get-JsonArray {
    param([string] $Uri, [int] $Timeout = 20)

    $response = Invoke-WebRequest -Uri $Uri -UseBasicParsing -TimeoutSec $Timeout
    if ([string]::IsNullOrWhiteSpace($response.Content)) {
        return @()
    }

    return @($response.Content | ConvertFrom-Json)
}

Write-Host ""
Write-Host "Simulador del Mundial 2026 (modo historico)" -ForegroundColor White
Write-Host "Backend: $BaseUrl"

# 1. Comprobar que el backend este disponible.
Write-Section "1/5  Comprobando backend..."
try {
    $health = Invoke-RestMethod -Uri "$BaseUrl/actuator/health" -TimeoutSec 10
} catch {
    Write-Host ""
    Write-Host "El backend no esta ejecutandose. Inicia Spring Boot primero." -ForegroundColor Red
    Write-Host ""
    Write-Host "Desde la raiz del backend:" -ForegroundColor DarkGray
    Write-Host "    .\mvnw.cmd spring-boot:run" -ForegroundColor DarkGray
    Write-Host ""
    exit 1
}

if ($health.status -ne "UP") {
    Write-Host ""
    Write-Host "El backend responde pero no esta sano (status: $($health.status))." -ForegroundColor Red
    Write-Host "Revisa la conexion con PostgreSQL antes de simular el Mundial."
    exit 1
}
Write-Host "  Backend disponible (status: UP)" -ForegroundColor Green

# 2. Activar el reloj historico.
Write-Section "2/5  Activando reloj historico..."
$body = @{ instant = $Instant } | ConvertTo-Json -Compress
try {
    $null = Invoke-RestMethod -Uri "$BaseUrl/api/v1/internal/clock/historical" `
        -Method Post -Body $body -ContentType "application/json" -TimeoutSec $TimeoutSec
} catch {
    Write-Host "  No fue posible cambiar el reloj." -ForegroundColor Red
    Write-Host "  Endpoint : POST $BaseUrl/api/v1/internal/clock/historical"
    Write-Host "  Instante : $Instant"
    Write-Host "  Error    : $($_.Exception.Message)"
    exit 1
}

# 3. Verificar el modo resultante.
$clock = Invoke-RestMethod -Uri "$BaseUrl/api/v1/internal/clock" -TimeoutSec $TimeoutSec
if ($clock.mode -ne "HISTORICAL_REPLAY") {
    Write-Host "  El reloj no quedo en modo historico (modo actual: $($clock.mode))." -ForegroundColor Red
    exit 1
}
Write-Host "  Reloj en HISTORICAL_REPLAY" -ForegroundColor Green

# 4. Torneo activo.
Write-Section "3/5  Consultando torneo activo..."
try {
    $tournament = Invoke-RestMethod -Uri "$BaseUrl/api/v1/tournaments/active" -TimeoutSec $TimeoutSec
} catch {
    Write-Host "  No hay torneo activo disponible." -ForegroundColor Red
    Write-Host "  Si la base es nueva, importa el dataset (ver README, seccion 9)."
    exit 1
}
Write-Host "  $($tournament.name)" -ForegroundColor Green

# 5. Partidos proximos con el reloj simulado.
Write-Section "4/5  Consultando partidos proximos..."
$upcoming = Get-JsonArray -Uri "$BaseUrl/api/v1/matches/upcoming?tournamentId=$($tournament.id)" -Timeout $TimeoutSec
$first = $upcoming | Sort-Object startsAt | Select-Object -First 1

Write-Section "5/5  Resultado"
Write-Field "Modo del reloj" $clock.mode
Write-Field "Fecha simulada" $clock.currentTime
Write-Field "Torneo" "$($tournament.name)  [$($tournament.id)]"
Write-Field "Partidos upcoming" $upcoming.Count

if ($first) {
    Write-Field "Primer partido" "$($first.homeTeamName) vs $($first.awayTeamName)"
    Write-Field "Fecha del primero" $first.startsAt
    Write-Field "predictionsOpen" $first.predictionsOpen
    Write-Field "Cierra pronostico" $first.predictionClosesAt
}

Write-Host ""

if ($upcoming.Count -eq 104) {
    Write-Host "Mundial simulado correctamente. 104 partidos disponibles." -ForegroundColor Green
    Write-Host ""
    Write-Host "Recarga el frontend para verlos. Al reiniciar el backend, el reloj" -ForegroundColor DarkGray
    Write-Host "vuelve a REAL y habra que ejecutar este script de nuevo." -ForegroundColor DarkGray
    Write-Host ""
    exit 0
}

# Diagnostico: no tocamos nada mas, solo informamos.
Write-Host "No aparecieron los 104 partidos esperados." -ForegroundColor Yellow
Write-Host ""
Write-Host "Diagnostico:" -ForegroundColor Yellow

$total = Get-JsonArray -Uri "$BaseUrl/api/v1/matches?tournamentId=$($tournament.id)" -Timeout 30
Write-Field "Partidos en la base" $total.Count
Write-Field "Partidos upcoming" $upcoming.Count

if ($total.Count -eq 0) {
    Write-Host ""
    Write-Host "  La base no tiene partidos: falta importar el dataset." -ForegroundColor Yellow
    Write-Host "  Ver README, seccion 9 (POST /api/v1/internal/dataset/world-cup-2026/import)."
} else {
    $ordered = $total | Sort-Object startsAt
    Write-Field "Primer partido" $ordered[0].startsAt
    Write-Field "Ultimo partido" $ordered[-1].startsAt
    Write-Field "Instante simulado" $clock.currentTime
    Write-Host ""
    Write-Host "  'upcoming' solo devuelve partidos con startsAt posterior al instante" -ForegroundColor Yellow
    Write-Host "  simulado. Si el instante es posterior al ultimo partido, la lista queda"
    Write-Host "  vacia. Prueba con una fecha anterior al primer partido:"
    Write-Host ""
    Write-Host "      .\scripts\simular-mundial.ps1 -Instant `"$($ordered[0].startsAt)`"" -ForegroundColor DarkGray
}

Write-Host ""
exit 1
