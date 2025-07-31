@echo off
echo Baixando e configurando Java 11 temporariamente...

REM Cria pasta temporária para Java 11
mkdir temp_java11 2>nul

REM Baixa o OpenJDK 11
curl -L -o "temp_java11\openjdk-11.zip" "https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_windows-x64_bin.zip"

REM Extrai o Java 11
powershell -Command "Expand-Archive -Path 'temp_java11\openjdk-11.zip' -DestinationPath 'temp_java11' -Force"

REM Encontra o caminho do Java 11 extraído
for /d %%i in (temp_java11\jdk-11*) do set TEMP_JAVA_HOME=%%i

REM Executa o Gradle com Java 11
set JAVA_HOME=%TEMP_JAVA_HOME%
set PATH=%TEMP_JAVA_HOME%\bin;%PATH%
gradlew.bat %*

REM Cleanup
rmdir /s /q temp_java11
