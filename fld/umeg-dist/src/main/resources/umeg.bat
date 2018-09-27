REM @echo off
REM ****************************************************************************
REM * Start UMeG for Windows.
REM * parameter 1 -- required - rate
REM ****************************************************************************

setlocal
pushd "%~dp0"

if "%~1" == "" (
    echo "Missing rate argument."
    exit /B 1
)

set "JAVA_XX_FLAGS=-XX:MaxHeapFreeRatio=30 -XX:MinHeapFreeRatio=10"
set "APM_AGENT="-javaagent:%~dp0\wily\Agent.jar" -Dcom.wily.introscope.agent.agentName=UMeGAgent -Dintroscope.agent.customProcessName=UMeG -Dcom.wily.introscope.agentProfile="%~dp0\wily\core\config\IntroscopeAgent.profile""

if not "%EM_URL%" == "" set "APM_AGENT=%APM_AGENT% "-DagentManager.url.1=%EM_URL%""

java^
 %JAVA_XX_FLAGS%^
 %APM_AGENT%^
 -cp "%~dp0\classes"^
 com.ca.apm.systemtest.fld.umeg.Main^
 -r "%~1"

popd
endlocal
