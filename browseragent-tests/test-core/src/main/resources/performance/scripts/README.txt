README for running Selenium script

***** PREREQUISITES*****
JAR's that are needed are chromedriver.exe(2.25 version or later version) and selenium-server-standalone-2.53.0
One can find these JARs at these location in the artifactory

Chrome Drivers
https://sites.google.com/a/chromium.org/chromedriver/downloads


Selenium standalone jar
http://isl-dsdc.ca.com/artifactory/webapp/#/artifacts/browse/simple/General/apm-third-party-local/org/seleniumhq/selenium/selenium-server-standalone/2.53.0


1)All the jars, exe's should be placed in a libs folder. if 'libs' folder is not present then add it and place the required jars/drivers in there.
Name for the chrome driver has to be chromedriver.exe.
2)Various arguments inside the runchromeselenium.bat file. 
	1st argument - IP address of the machine where ACC is installed.
	2nd argument - Number of iterations the to run
	3rd argument - sleep time in seconds between the iterations in seconds.
3)ACC server/controller should be running.
4)The username and passowrd for the login page pf ACC must be edited inside the script file(TASChrome.java,TASChromeP.java and TASChromeC.java)
5) There are 3 different files
	a)TASChrome.java -- calculates only the resource timings
	b)TASChromeP.java -- calculates only the page load timings.
	c)TASChromeC.java -- calculates both, page and resource timings.

Recommened: please use TASChromeP.java or TASChromeC.java 
Note: TASChrome.java is deprecated and takes in total time to run the test for as argument no 1 instead of total number of iterations

*****CUSTOMIZATION*****
1)The browser does not exit after the TASChromeX script end, this too can be changed in the file.
2)Follow the comments in the TASChromeX file to customize it for Firefox browser.
3)There is an option to delete browser cache, this function is not called by default.
Please uncomment the required function(noted inline in the code) to delete browser cache.Also need to update browser cache path for the machine in question in the code.


******RUNNING******
1)Run using the runchromeselenium.bat file