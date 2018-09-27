title %TITLE%

set JAVA_HOME=%%JAVA_HOME%%
set PATH=%JAVA_HOME%\bin;%PATH%
set GROOVY_HOME=%%GROOVY_HOME%%
set KILL_FILE=%KILL_FILE%

:Run
call ant -f soa-load.xml -Dsystem.config.groovy=soa.groovy jax_1

if exist %KILL_FILE% goto GO_AWAY_NOW

call ant -f soa-load.xml -Dsystem.config.groovy=soa.groovy jax_2

if exist %KILL_FILE% goto GO_AWAY_NOW

call ant -f soa-load.xml -Dsystem.config.groovy=soa.groovy jax_3

if exist %KILL_FILE% goto GO_AWAY_NOW

ping 127.0.0.1 -n 10
cls
GOTO Run

:GO_AWAY_NOW