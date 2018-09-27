@echo off

set WORKING_DIR=[WORKING_DIR]

rem: Parameters for executing tests for FLD to replay recorded metrics and transactions
set MODE=replay

rem Hostname of the EM to replay to
set EM_HOST=[EM_HOST]

rem Port of the EM to replay to
set EM_PORT=[EM_PORT]

rem Credentials of EM to replay to 
set USER_NAME_EM=[USER_NAME_EM]
set PASSWORD_EM="[PASSWORD_EM]"

rem the root name and password of the metric and traces extract files
set LOADFILE_HVR="extract"

rem The number of cloned agent sets. Each set gets its own unique hostname
set CLONE_CONNECTION_COUNT=8

rem The number of clones of each agent in the extract set
set CLONE_AGENT_COUNT=25

rem Number of seconds to wait between sending a random TT (mimics TT sampling)
set WAIT_BW_TRACE_SECS=1

rem The host to map Agents to
set AGENT_HOST_NAME=[AGENT_HOST_NAME]

@echo on

%WORKING_DIR%\HVRAgent.9.8.bat %MODE% -host %EM_HOST% -port %EM_PORT% -username %USER_NAME_EM% -password %PASSWORD_EM% -loadfile %LOADFILE_HVR% -cloneconnections %CLONE_CONNECTION_COUNT% -cloneagents %CLONE_AGENT_COUNT% -secondspertrace %WAIT_BW_TRACE_SECS% -agenthost %AGENT_HOST_NAME%

