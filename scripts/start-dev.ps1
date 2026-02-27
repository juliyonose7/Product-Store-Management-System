$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

Write-Host "Starting backend and frontend in separate terminals..."

Start-Process powershell -ArgumentList '-NoExit', '-Command', "Set-Location '$root'; .\\backend\\mvnw.cmd -f .\\backend\\pom.xml spring-boot:run"
Start-Sleep -Seconds 2
Start-Process powershell -ArgumentList '-NoExit', '-Command', "Set-Location '$root'; npm --prefix .\\frontend start"

Write-Host "Backend: http://localhost:8080"
Write-Host "Frontend: http://localhost:4200"
