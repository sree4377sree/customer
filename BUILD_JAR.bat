@echo off
title Building Customer Support Ticket Management System
echo Building standalone executable JAR...
set JAVA_HOME=C:\Users\prasu\.tools\jdk-17
set PATH=C:\Users\prasu\.tools\jdk-17\bin;C:\Users\prasu\.tools\maven\bin;%PATH%
call "C:\Users\prasu\.tools\maven\bin\mvn.cmd" clean package -DskipTests
pause
