@echo off
rem 
rem This is a sample batch file for running the WebViewSampleTest as a standalone java application.  It requires JDK 1.7 or higher
rem to be on the system path.  The test itself expects that Chrome will already be installed.
rem 

set /a ArgCount = 0
for %%a in (%*) do set /a ArgCount += 1
if not %ArgCount% == 3 (
	echo Usage:
	echo     run-webview-sample.bat WEBVIEW_URL USERNAME PASSWORD
	goto :eof
)

set CLASSPATH=../lib/*

@echo on
java -Dwebdriver.chrome.driver=%cd%\chromedriver.exe com.ca.apm.systemtest.fld.scripts.WebViewSampleTest %1 %2 %3

