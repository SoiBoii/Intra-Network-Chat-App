@echo off
echo ========================================
echo Finding Your IP Address
echo ========================================
echo.
echo Your IP addresses on this network:
echo.
ipconfig | findstr "IPv4"
echo.
echo Use one of these IP addresses (usually the first one) 
echo as the server address for other devices to connect.
echo.
echo Press any key to exit...
pause > nul 