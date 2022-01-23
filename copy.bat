@echo off
set SHR_HOME=D:\Kingdee
set BIN_HOME=G:\eclipse_workSpace\bj\shr_custom_project\web


set CUSTOMER_HOME=%SHR_HOME%\eas\server\deploy\easweb.ear\shr_web.war\addon\customer\web

echo %CUSTOMER_HOME%

del /f /s /q %CUSTOMER_HOME%

mkdir %CUSTOMER_HOME%

xcopy  %BIN_HOME% /s %CUSTOMER_HOME%  /Y