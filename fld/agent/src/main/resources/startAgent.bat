@echo off
REM ****************************************************************************
REM * Start agent for windows
REM ****************************************************************************

setLocal EnableDelayedExpansion
pushd "%~dp0"

rem Elevated shell detection. See <http://stackoverflow.com/a/11995662/341065>.
echo Administrative permissions required. Detecting permissions...

net session >nul 2>&1
if %errorlevel% == 0 (
    echo Success: Administrative permissions confirmed.
) else (
    echo Failure: Current permissions inadequate.
    exit /B %errorlevel%
)

echo --------------------------------------------------------------------------------
echo Starting Agent on windows

where taskkill >NUL
if errorlevel 1 (
    echo TaskKill is not on PATH. Appending %windir%\System32 to PATH.
    set "PATH=%PATH%;%windir%\System32"
)

where taskkill >NUL
if errorlevel 1 (
    echo WARNING: TaskKill is still not on PATH.
)

:restart

set CLASSPATH=!PRE_CLASSPATH!
if not "%CLASSPATH%" == "" set CLASSPATH=%CLASSPATH%;

if not exist agent-current (
    setlocal
    REM Avoid conflict with Cygwin's mklink.
    set PATH=
    mklink /d agent-current .
    if errorlevel 1 echo mklink failed!
    endlocal
)

set "CLASSPATH=%CLASSPATH%;agent-current\lib\*"
set "CLASSPATH=.;%CLASSPATH%"
echo CLASSPATH=!CLASSPATH!

call :find_apm_agent_jar
if not "%_apm_agent_jar%"=="" (
    echo APM Agent JAR file is %_apm_agent_jar%
)

if not "%_apm_agent_jar%"=="" (
    set "APM_AGENT=-javaagent:%_apm_agent_jar%"
    if exist "%~dp0\IntroscopeAgent.profile" (
        set "APM_AGENT=!APM_AGENT! -Dcom.wily.introscope.agentProfile=%~dp0\IntroscopeAgent.profile"
    ) else (
        if exist "%~dp0\wily\core\config\IntroscopeAgent.profile" (
            set "APM_AGENT=!APM_AGENT! -Dcom.wily.introscope.agentProfile=%~dp0\wily\core\config\IntroscopeAgent.profile"
        ) else (
            set "APM_AGENT=!APM_AGENT! -Dcom.wily.introscope.agent.agentName=FLDAgent"
            set "APM_AGENT=!APM_AGENT! -Dintroscope.agent.enterprisemanager.transport.tcp.host.DEFAULT=aqpp-em01"
        )
    )
) else (
    set APM_AGENT=
)

set JAVA_XX_FLAGS=-XX:MaxHeapFreeRatio=30 -XX:MinHeapFreeRatio=10

java ^
 %JAVA_XX_FLAGS% ^
 "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"^
 %APM_AGENT%^
 "-Dfld.agent.property.file=fld-agent-production.properties"^
 "-Dfld.agent.node.name=%COMPUTERNAME:\=%"^
 "-Dlog4j.configuration=file:/%~dp0log4j.properties"^
 com.ca.apm.systemtest.fld.agent.Agent^
 fldagent-test-context.xml

REM Use ERRORLEVEL 1 when you would like to restart also in case of runtime exception
REM otherwise use ERRORLEVEL 50
IF ERRORLEVEL 1 (
    echo ------------
    echo Agent RELOAD
    call :delay 2
    GOTO restart
)

echo ------------
echo Bye
popd
exit /B 0

:delay
setlocal
set /A N=%1+1
ping -n %N% -4 -w 1000 localhost >NUL
endlocal
exit /B 0

:find_apm_agent_jar
set _apm_agent_jar=
for /R %%F in (agent-apm-agent-*-jar-with-dependencies.jar) do (
    set "_apm_agent_jar=%%~dpfF"
    exit /B 0
)
exit /B 0
