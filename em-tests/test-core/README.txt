How to create and run new Selenium test for ATC
===============================================

1a. Install webdriver manager:
	npm install -g webdriver-manager

1b. Install Selenium Server:
	webdriver-manager update

1c. Start up a server:
	webdriver-manager start

1d. Set testbed.selenium.webdriverURL to localhost and localRun to false

2a. Other way to download driver and put it to some place and point using testbed.chromeDriverPath and set localRun to true

3. Create env.file and put somewhere:
	testbed.localRun=true  # true to use local driver specified at testbed.chromeDriverPath
	testbed.chromeDriverPath=C:/temp/chromedriver.exe  # path to local driver
	testbed.selenium.webdriverURL=http://tas-cz-naf:4444/wd/hub  # url of remote Selenium driver
	testbed.test.applicationBaseURL=http://tas-cz-na0.ca.com:8082/  # application base url

4. Install in eclipse TestNG plugin

5. Create test class and extends it from UITest.
	Package for ATC: com.ca.apm.test.atc
	Under this create appropriate package as you want.

6. Create method with anotation @Test from TestNG

	Note:
		Currently every test anotated with @Test has to have also @Tas anotation:
		@Tas(testBeds = @TestBed(name = AppMapTradingServiceTestBed.class, executeOn = "endUserMachine"), owner = "kovan03", size = SizeType.DEBUG, exclusivity = ExclusivityType.EXCLUSIVE)

		Without @Tas anotation build failed.
	
		This will be changed in future when maven tas plugin will change.

7. Create new Launcher for Test:
	TestNG -> New
	Select the test class
	Fill VM arguments:
		-Denv.properties="path.to.env.file"
		
8. Run launcher


How to run AGC registration tests
=================================
1. Install webdriver as described above in step 1. Install TestNG plugin in eclipse.
2. Use or modify file localdev/register-env-local.properties
3. Create TestNG launcher in Eclipse:
  - run suite localdev/register-tmp-suite.xml
  - add VM arguments:
    -Denv.properties="localdev/register-env-local.properties" -Dselenium.webdriverURL=http://localhost:4444/wd/hub -Dtas.skipLocalRepo=false
    
4. Modify as you need the register-env-local.properties to run only tests that you need
5. Launch tests
6. Manual tests need manually restart followers. Manual step contains JOptionDialog that appears when is needed and say what to do manually.

How to run one test from RegistrationSteps class
================================================
1. Create new TestNG launch in Eclipse
2. Create env.properties files (as described above)
3. Add VM arguments: -Denv.properties="env.properties" -Dselenium.webdriverURL=http://localhost:4444/wd/hub
4. Use prepared testNg suite to run one or more methods.
  