@echo off

title %%TITLE%%

set LOG_FILE=%%LOG_FILE%%

set JAVA_HOME=%%JAVA_HOME%%
set PATH=%JAVA_HOME%\bin;%PATH%
set CP=%%CP%%

set WEBVIEW_SERVER_HOST=%%WEBVIEW_SERVER_HOST%%
set WEBVIEW_SERVER_PORT=%%WEBVIEW_SERVER_PORT%%
set WEBVIEW_SERVER_CREDENTIALS=%%WEBVIEW_SERVER_CREDENTIALS%%
set DATA=%%DATA%%

call java -cp %CP% com.ca.apm.tests.util.selenium.WebViewLoadSeleniumRunner^
 -webviewServerHost %WEBVIEW_SERVER_HOST% -webviewServerPort %WEBVIEW_SERVER_PORT% %WEBVIEW_SERVER_CREDENTIALS%^
 -webviewServerHostPlaceholders "WEBVIEW_HOST_NAME"^
 %DATA% >> %LOG_FILE% 2>>&1
