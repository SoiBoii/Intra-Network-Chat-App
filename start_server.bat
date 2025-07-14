@echo off
echo ========================================
echo WhatsApp Clone Server Setup
echo ========================================
echo.
echo Finding your IP address...
ipconfig | findstr "IPv4"
echo.
echo Your IP address is shown above. Other devices on the same network
echo should use this IP address to connect to your server.
echo.
echo Starting server on port 12345...
echo.
echo To connect from other devices, use:
echo java -cp .;sqlite-jdbc-3.50.2.0.jar Client YOUR_IP_ADDRESS
echo.
echo Press any key to start the server...
pause > nul
java -cp .;sqlite-jdbc-3.50.2.0.jar Server 