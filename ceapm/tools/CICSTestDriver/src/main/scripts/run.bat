@echo off
setlocal EnableDelayedExpansion

REM ### Configuration
REM # AgentDir should be set to the absolute path of an Agent installation.
set AgentDir=wily

REM # AgentName is the name under which the test application will appear in EM.
set AgentName=CICSTestDriver

REM # CTGHost
set CTGHost=usilca31

REM # CTGPort
set CTGPort=2008

REM # CICS IPIC
set CICSIPIC=C660IPIC

REM # Do not modify anything below this line unless you know what you're doing.
REM ############################################################################

if "%AgentDir%" == "" (
    echo AgentDir has not been configured ^(see top of this script^).
    exit /b 1;
)

set AGENT=%AgentDir%\Agent.jar
set CONFIG=%AgentDir%\core\config\IntroscopeAgent.profile

if not exist %AGENT% (
    echo The configured AgentDir is not a proper Agent installation directory: %AgentDir%
    exit /b 1;
)

set MAIN=com.ca.apm.powerpack.sysview.tools.cicstestdriver.CICSTestDriver

REM # Dynamically build a classpath based on the contents of the lib directory

set CLASSPATH=
for /f "tokens=*" %%G in ('dir /b lib\*.jar') do (
    set CLASSPATH=lib\%%G;!CLASSPATH!
)

set verified=0
if not "%1"=="" if exist xml\%1 set verified=1
if %verified% equ 0 (
    REM # List available test definitions if no valid one was passed in
    echo Missing or invalid test definition specified. Available options:
    for /f %%G in ('dir /b xml\*.xml ^| findstr /v /c:"mapping.xml"') do (
        echo     %%G
    )
    exit /b 1;
)

set XML=xml\%1
set PARMS=%2 JGATE tcp://%CTGHost% jgateport=%CTGPort% server=%CICSIPIC% conduit commarea=D180 EBCDIC NOCOMMAREA

java -javaagent:"%AGENT%" -Dcom.wily.introscope.agentProfile="%CONFIG%" -Dcom.wily.introscope.agent.agentName="%AgentName%" -cp "%CLASSPATH%" %MAIN% -mapfile xml\mapping.xml -xmlfile %XML% %PARMS%

REM vim: set tw=0:
