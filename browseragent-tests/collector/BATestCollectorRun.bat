::
:: This bat file is a developers helper to start a collector
::
:: Prerequisites:
::    1. Configure working directory
::    2. Port specified below is available
::    3. java is in the path
::
:: To run from command prompt:
::    1. from browseragent-tests\collector run: mvn clean install
::    2. then from the same directory run: BATestCollectorRun.bat


:: Constants
set MAIN_CLASS=com.ca.apm.browseragent.testsupport.collector.BATestCollector
set PORT=5000
set TENANT=defaulttenant
set APP=defaultapp
set WORKINGDIR=C:\MyCollectorWorkingDir
set COLLECTOR_NAME=defaultInstance

:: The directory where test app is hosted, this will be used to create paths
:: from FILES_TO_INSERT_SNIPPET below
set TEST_APP_DIR=C:\apm\BrowserAgent\apache-tomcat-8.0.33\webapps\brtmtestapp

:: The files where the snippet will be inserted, comma seperated listed. Wild carding not supported at this time.
set FILES_TO_INSERT_SNIPPET=index.html,GETCORS.jsp,GETLocalDomain.jsp,GETLocalDomain2.jsp,jserrors\error_MultipleErrors.jsp,GETLocalDomainQueryParams.jsp,ajaxclamp\AjaxClamp.jsp,GeoLocation.html,JSFunctionRetry.jsp,spa\index.html,jquery\ajaxTest.html,jquery\ajaxTest2.html,jquery\ajaxTest3.html

:: wild card matches the version, move the file to remove/rename
move /y "target\browseragent-tests-collector-*.jar" "target\BATestCollector.jar"

:: Debugging log4j stuff
:: java -Dlog4j.debug -cp "target\BATestCollector.jar" %MAIN_CLASS% %PORT% %TENANT% %APP% %WORKINGDIR% %COLLECTOR_NAME%

:: Run
java -cp "target\BATestCollector.jar" %MAIN_CLASS% %PORT% %TENANT% %APP% "%WORKINGDIR%" %COLLECTOR_NAME% "%TEST_APP_DIR%" "%FILES_TO_INSERT_SNIPPET%"