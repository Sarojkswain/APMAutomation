1.	Ensure that tomcat is shut down
2.	Delete the database at C:\Users\USERNAME\fld  (e.g. SINAL01)
3.	Start tomcat
4.	Pull and update the testing repository
5.	Build and deploy to tomcat – probably a “mvn clean install” from the testing root, and then “mvn tomcat7:deploy” or “mvn tomcat7:redeploy” from the testing/fld/load-orchestrator-webapp directory
6.	Load http://localhost:8080/LoadOrchestrator/upload.html
7.	Upload the attached test process

At this point you can use the API to create a dashboard.  Use process key “fldTest.1”

POST /LoadOrchestrator/api/dashboards HTTP/1.0
Content-Type: application/json
Content-Length: 142

{"processKey":"fldTest.1","name":"My Dashboard #8","monitors":[{"key":"Monitor 1","name":"monitor.1"},{"key":"Monitor 2","name":"monitor.2"}]}

Then update the dashboard – param1 is a string, param2 is a long representing the number of milliseconds it will wait before completing, param3 is a Boolean – you’ll see that in the dashboard that is created.

PUT /LoadOrchestrator/api/dashboards/1 HTTP/1.0
Content-Type: application/json
Content-Length: 387

{"id":1,"processKey":"fldTest.1","name":"My Dashboard #8","config":[{"formId":"param1","type":"string","name":"testForm.param1","value":"param1"},{"formId":"param2","type":"long","name":"testForm.param2","value":15000},{"formId":"param3","type":"boolean","name":"testForm.param3","value":true}],"monitors":[{"key":"Monitor 1","name":"monitor.1"},{"key":"Monitor 2","name":"monitor.2"}]}]

After updating the dashboard, you can run it using launch.  You should see a little activity in the tomcat console window

POST /LoadOrchestrator/api/dashboards/1/launch HTTP/1.0


Tomcat console:
About to wait 10 sec before starting
Now starting
Param1 == param1
From execution: param1
Param3 == true
Will now wait 15000 ms at Mon Dec 22 16:15:06 CET 2014
Done waiting
