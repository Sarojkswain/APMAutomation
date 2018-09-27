rem @echo off

set WORKING_DIR=[WORKING_DIR]
set EM_DIR=[EM_DIR]
rem set EM_DIR=S:\sw\Introscope99.99.0.sys
set ARGS=

:loop
if %1x==x goto continue
set ARGS=%ARGS% %1
shift
goto loop
:continue

set PLUGINS_DIR=%EM_DIR%\product\enterprisemanager\plugins
rem set JAVA_HOME=C:\sw\Java\jdk1.6.0_17\bin\java
set JAVA_HOME="C:\Program Files\Java\jdk1.7.0_45"


@echo on

java -Xmx2048m -classpath "%WORKING_DIR%\HVRAgent.jar;%PLUGINS_DIR%\com.wily.core_99.99.sys.jar;%PLUGINS_DIR%\com.wily.introscope.common_99.99.sys.jar;%PLUGINS_DIR%\com.wily.introscope.em.client14_99.99.sys.jar;%PLUGINS_DIR%\com.wily.introscope.em.client_99.99.sys.jar;%PLUGINS_DIR%\com.wily.isengard.client_99.99.sys.jar;%EM_DIR%\lib\IntroscopeJDBC.jar" com.wily.introscope.tools.fakeagent.FakeAgent %ARGS%
