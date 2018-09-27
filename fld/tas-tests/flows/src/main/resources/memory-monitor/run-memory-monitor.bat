@echo off

title %%TITLE%%

set LOG_FILE=%%LOG_FILE%%

set JAVA_HOME=%%JAVA_HOME%%
set PATH=%JAVA_HOME%\bin;%PATH%
set CP=%%CP%%

set GC_LOG_FILE=%%GC_LOG_FILE%%
set GROUP=%%GROUP%%
set ROLE_NAME=%%ROLE_NAME%%
set MEMORY_MONITOR_WEBAPP_HOST=%%MEMORY_MONITOR_WEBAPP_HOST%%
set MEMORY_MONITOR_WEBAPP_PORT=%%MEMORY_MONITOR_WEBAPP_PORT%%
set MEMORY_MONITOR_WEBAPP_CONTEXT_ROOT=%%MEMORY_MONITOR_WEBAPP_CONTEXT_ROOT%%
set CHART_WIDTH=%%CHART_WIDTH%%
set CHART_HEIGHT=%%CHART_HEIGHT%%
set WAIT_INTERVAL=%%WAIT_INTERVAL%%
set WORK_DIR=%%WORK_DIR%%
set ITERATION_COUNT=%%ITERATION_COUNT%%

set OPTS=-gcLogFile %GC_LOG_FILE% -group %GROUP% -roleName %ROLE_NAME% -memoryMonitorWebappHost %MEMORY_MONITOR_WEBAPP_HOST%

if defined MEMORY_MONITOR_WEBAPP_PORT         set OPTS=%OPTS% -memoryMonitorWebappPort %MEMORY_MONITOR_WEBAPP_PORT%
if defined MEMORY_MONITOR_WEBAPP_CONTEXT_ROOT set OPTS=%OPTS% -memoryMonitorWebappContextRoot %MEMORY_MONITOR_WEBAPP_CONTEXT_ROOT%
if defined CHART_WIDTH                        set OPTS=%OPTS% -chartWidth %CHART_WIDTH%
if defined CHART_HEIGHT                       set OPTS=%OPTS% -chartHeight %CHART_HEIGHT%
if defined WAIT_INTERVAL                      set OPTS=%OPTS% -waitInterval %WAIT_INTERVAL%
if defined WORK_DIR                           set OPTS=%OPTS% -workDir %WORK_DIR%
if defined ITERATION_COUNT                    set OPTS=%OPTS% -iterationCount %ITERATION_COUNT%

rem call java -cp %CP% com.ca.apm.systemtest.fld.util.memorymonitor.MemoryMonitorRunner %OPTS% >> %LOG_FILE% 2>>&1
call powershell "java -cp %CP% com.ca.apm.systemtest.fld.util.memorymonitor.MemoryMonitorRunner %OPTS% | tee %LOG_FILE%"
