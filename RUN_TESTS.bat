@echo off
title Running Customer Support System Unit Tests
set JAVA_HOME=C:\Users\prasu\.tools\jdk-17
set PATH=C:\Users\prasu\.tools\jdk-17\bin;C:\Users\prasu\.tools\maven\bin;%PATH%
call "C:\Users\prasu\.tools\maven\bin\mvn.cmd" test
pause
