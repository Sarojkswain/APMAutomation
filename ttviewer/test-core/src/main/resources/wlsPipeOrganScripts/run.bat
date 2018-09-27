echo off
set libpath=%1
set scenario=%2

IF %1.==. GOTO default
IF %2.==. GOTO default
GOTO input

:input
java -cp "%libpath%" -Dpipeorgan.logging=verbose com.wily.tools.pipeorgan.Main %scenario%

:default
Echo "Invalid Script for PipeOrgan"