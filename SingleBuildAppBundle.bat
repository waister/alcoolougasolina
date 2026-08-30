@echo off
echo Iniciando geracao de App Bundle (Alcool ou Gasolina)...

echo.
echo ============================================================
echo Gerando Bundle para: Alcool ou Gasolina
echo ============================================================
call gradlew.bat :app:bundleRelease
if errorlevel 1 (
    echo [ERRO] Falha ao gerar bundle para Alcool ou Gasolina
    pause
    exit /b 1
)

echo.
echo ============================================================
echo Concluido! O bundle foi gerado em:
echo app/build/outputs/bundle/release/
echo ============================================================
pause