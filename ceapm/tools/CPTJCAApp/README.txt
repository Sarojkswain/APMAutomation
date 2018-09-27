1. Prerequisites:
=================
 * WebSphere MQ Queue Manager (distributed or mainframe) with configured request and reply queues.
 * Some kind of application processing messages from the configured queues (e.g. CICS/IMS transactions).

2. Setup:
=========
 * Install the war file in a Java Servlet server (WAS, Tomcat, Jetty, ...).
 * Modify the configuration of the APM Agent used for the Servlet server to include the included PBD file.

3. Using:
---------
 * Access the servlet context path.
 * Fill out the form based on the requirements of your test and submit.
