@echo off
set JAVA_HOME=C:\Users\RPC\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.6.7-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
call mvnw.cmd javafx:run
