@echo off
echo ========================================
echo WhatsApp Clone Client
echo ========================================
echo.
if "%1"=="" (
    echo Usage: start_client.bat SERVER_IP_ADDRESS
    echo.
    echo Example: start_client.bat 192.168.1.100
    echo.
    pause
    exit /b
)
echo Connecting to server at %1:12345
echo.
java -cp .;sqlite-jdbc-3.50.2.0.jar Client %1 