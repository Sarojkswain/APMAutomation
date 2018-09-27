@echo off

cd /d %~dp0

title %%TITLE%%

rem set LOG_FILE=%%LOG_FILE%%
set LOG4J_CONFIG=%%LOG4J_CONFIG%%

set JAVA_HOME=%%JAVA_HOME%%
set PATH=%JAVA_HOME%\bin;%PATH%
set CP=%%CP%%

set WAIT_INTERVAL=%%WAIT_INTERVAL%%
set WORK_DIR=%%WORK_DIR%%

set OPTS=%%OPTS%%

if defined WAIT_INTERVAL set OPTS=%OPTS% -waitInterval %WAIT_INTERVAL%
if defined WORK_DIR      set OPTS=%OPTS% -workDir %WORK_DIR%

set TO_KILL=wmic process where "CommandLine like '%run-time-synchronization.bat%' and not (CommandLine like '%wmic%')"
echo TO_KILL = %TO_KILL%
wmic process where "CommandLine like '%run-time-synchronization.bat%' and not (CommandLine like '%wmic%')" Call Terminate
echo KILL_ERRORLEVEL = %ERRORLEVEL%

set TO_KILL=wmic process where "CommandLine like '%java%TimeSynchronizationRunner%' and not (CommandLine like '%wmic%')"
echo TO_KILL = %TO_KILL%
wmic process where "CommandLine like '%java%TimeSynchronizationRunner%' and not (CommandLine like '%wmic%')" Call Terminate
echo KILL_ERRORLEVEL = %ERRORLEVEL%

powershell -command "Start-Sleep -s 2"

:restart

call java -cp %CP% -Dlog4j.configuration=%LOG4J_CONFIG% com.ca.apm.systemtest.fld.util.timesync.TimeSynchronizationRunner %OPTS%

if %ERRORLEVEL% EQU 0 (
    call powershell "echo 'Bye..'
) else (
    call powershell "echo 'Time synchronization failed, restarting..'
    powershell -command "Start-Sleep -s 2"
    goto restart
)
