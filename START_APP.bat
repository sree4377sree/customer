@echo off
title Customer Support Ticket Management System
echo =====================================================================
echo  CUSTOMER SUPPORT TICKET MANAGEMENT SYSTEM
echo  Spring Boot 3 + Java 17 + MySQL + Thymeleaf + Bootstrap
echo =====================================================================
echo.

set JAVA_HOME=C:\Users\prasu\.tools\jdk-17
set PATH=C:\Users\prasu\.tools\jdk-17\bin;C:\Users\prasu\.tools\maven\bin;%PATH%

echo Using Java:
"%JAVA_HOME%\bin\java.exe" -version
echo.

echo Starting Customer Support Application on port 8082...
echo Access in browser: http://localhost:8082
echo.

call "C:\Users\prasu\.tools\maven\bin\mvn.cmd" spring-boot:run

pause
