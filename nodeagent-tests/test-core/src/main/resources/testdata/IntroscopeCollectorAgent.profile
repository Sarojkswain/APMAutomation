########################################################################
#                                                                      
# Introscope AutoProbe and Agent Configuration                         
#                                                                      
# CA Wily Introscope(R) Version 99.99.leo_node Release 99.99.0.leo_node
# Copyright (c) 2015 CA. All Rights Reserved.
# Introscope(R) is a registered trademark of CA.
########################################################################

########################
# AutoProbe Properties #
########################

#######################
# On/Off Switch
#
# ================
# This boolean property gives you the ability to disable
# Introscope AutoProbe by settings the property value
# to false.
# You must restart the managed application before changes to this property take effect.

introscope.autoprobe.enable=true

#######################
# Custom Log File Location
#
# ================
# Introscope AutoProbe will always attempt to log the changes
# it makes.  Set this property to move the location of the
# log file to something other than the default.  Non-absolute
# names are resolved relative to the location of this
# properties file.
# You must restart the managed application before changes to this property take effect.

introscope.autoprobe.logfile=../../logs/AutoProbe.log


#######################
# Directives Files
#
# ================
# This property specifies all the directives files that determine
# how Introscope AutoProbe performs the instrumentation.  Specify
# a single entry, or a comma-delimited list of entries. The list 
# may include any combination of:
#    - directives (.pbd) files
#    - directives list (.pbl) files 
#    - directories that will be scanned about once per minute for  
#      .pbd files. Directives files placed in a listed directory
#      will be loaded automatically, without any need to edit this 
#      Agent profile. If dynamic instrumentation is enabled, the 
#      directives will take effect immediately without an app reboot.    
# Non-absolute names will be resolved relative to the location of 
# this properties file.
# IMPORTANT NOTE: This is a required parameter and it MUST be set
# to a valid value.  
#    - If the property is not specified or the values are invalid, 
#      the Introscope Agent will not run!  
#    - If the property is set to include a directory, and invalid 
#      directives files are placed in the directory, AutoProbe  
#      metrics will no longer be reported!
#    - If the property is set to include a directory, and loaded 
#      directives files are removed from the directory, AutoProbe  
#      metrics will no longer be reported!
# You must restart the managed application before changes to this property 
# take effect. However, if the property includes one or more directories, 
# and dynamic instrumentation is enabled, the Introscope Agent will load 
# directives files from the specified directories without an app restart, 
# as noted above.

introscope.autoprobe.directivesFile=php-typical.pbl,nodejs-typical.pbl,hotdeploy


#######################
# Agent Properties    #
#######################

#######################
# Remote Probe Collector Port
#
# ================
# Introscope will listen to connections from remote PHP, Ruby, .NET etc probes on this port to
# collect events.
# You must restart agent before the changes to this property take effect.

introscope.remoteagent.collector.tcp.port = 5005
introscope.remoteagent.collector.tcp.local.only=true

# 
# This flag enables optimization for remote probe to filter out unnecessary events before sending them
# to Introscope. This is only beneficial, if a probe supports filtering, otherwise it can result in
# extra overhead. PHP probe is currently only probe that supprts this feature.
#
# This property will only take effect for new probe connections.
#
introscope.remoteagent.collector.probeid.filtering.enabled = true

# Each probe instance connection to collector is mapped to the virtual agent at EM
# Following two properties will govern how this agent will be named.
#
# There are three replacmentvariables that allowed in the configured name:
#
#	{type} - Probe type. Currently only supprted type is php
#	{program} - Name of the program that probe is attached to (e.g. /usr/bin/httpd)
#	{collector} - Name of collector agent. See agent naming proerties below.
#
# These properties are not hot and require collector re-start to take effect.
# Default value for probe process name is {type}
introscope.remoteagent.probe.process.name = {type}-probes
# Default value for probe agent name is {program}
introscope.remoteagent.probe.agent.name = {collector}({program})


#################################
# Logging Configuration
#
# ================
# Changes to this property take effect immediately and do not require the managed application to be restarted.
# This property controls both the logging level and the output location.
# To increase the logging level, set the property to:
# log4j.logger.IntroscopeAgent=VERBOSE#com.wily.util.feedback.Log4JSeverityLevel, console, logfile
# To send output to the console only, set the property to:
# log4j.logger.IntroscopeAgent=INFO, console
# To send output to the logfile only, set the property to:
# log4j.logger.IntroscopeAgent=INFO, logfile

log4j.logger.IntroscopeAgent=INFO, console, logfile

#log4j.logger.IntroscopeAgent.ProbeCollector=TRACE#com.wily.util.feedback.Log4JSeverityLevel, console, logfile

# If "logfile" is specified in "log4j.logger.IntroscopeAgent",
# the location of the log file is configured using the
# "log4j.appender.logfile.File" property.
# System properties (Java command line -D options)
# are expanded as part of the file name.  For example,
# if Java is started with "-Dmy.property=Server1", then
# "log4j.appender.logfile.File=../../logs/Introscope-${my.property}.log"
# is expanded to:
# "log4j.appender.logfile.File=../../logs/Introscope-Server1.log".

log4j.appender.logfile.File=../../logs/IntroscopeAgent.log
 
########## See Warning below ##########
# Warning: The following properties should not be modified for normal use.
# You must restart the managed application before changes to this property take effect.
log4j.additivity.IntroscopeAgent=false
log4j.appender.console=com.wily.org.apache.log4j.ConsoleAppender
log4j.appender.console.layout=com.wily.org.apache.log4j.PatternLayout
log4j.appender.console.layout.ConversionPattern=%d{M/dd/yy hh:mm:ss,SSS a z} [%-3p] [%c] %m%n
log4j.appender.console.target=System.err
log4j.appender.logfile=com.wily.introscope.agent.AutoNamingRollingFileAppender
log4j.appender.logfile.layout=com.wily.org.apache.log4j.PatternLayout
log4j.appender.logfile.layout.ConversionPattern=%d{M/dd/yy hh:mm:ss,SSS a z} [%-3p] [%c] %m%n
log4j.appender.logfile.MaxBackupIndex=4
log4j.appender.logfile.MaxFileSize=2MB
#########################################

#################################
# DNS lookup configuration
# 
# Agent has following DNS lookup implementations: direct and separateThread.  Implementation to use is specified 
# by value of introscope.agent.dns.lookup.type property.
# direct performs DNS lookups in application thread. Application thread will be delayed by length of time the 
# underlying DNS mechanism takes to perform a specific lookup.
# separateThread performs DNS lookups in a separate thread. The application thread is delayed at most by 
# introscope.agent.dns.lookup.max.wait.in.milliseconds milliseconds.
# When using separateThread implementation, if lookup of host name by IP address times out, IP address will be returned
# in place of name and if lookup of IP address by host name times out, empty IP address will be returned.  
# Default DNS lookup implementation is separateThread
#
# You must restart the managed application before change to this property takes effect.
#introscope.agent.dns.lookup.type=direct
introscope.agent.dns.lookup.type=separateThread
#
# Maximum time in milliseconds separateThread implementation waits to lookup a host name or IP address. 
# It is ignored by direct implementation.  Default value is 200.
# Change to this property takes effect immediately and does not require the managed application to be restarted.
introscope.agent.dns.lookup.max.wait.in.milliseconds=200


#################################
# Enterprise Manager Connection Order
#
# ================
# The Enterprise Manager connection order list the Agent uses if it 
# is disconnected from its Enterprise Manager.
# You must restart the managed application before changes to this property take effect.

introscope.agent.enterprisemanager.connectionorder=DEFAULT


#################################
# Enterprise Manager Locations and Names 
# (names defined in this section are only used in the 
# introscope.agent.enterprisemanager.connectionorder property)
#
# ================
# Settings the Introscope Agent uses to find the Enterprise Manager 
# and names given to host and port combinations.
# You must restart the managed application before changes to this property take effect.

#introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT=localhost
introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT=w2k8sys058.ca.com
introscope.agent.enterprisemanager.transport.tcp.port.DEFAULT=5001
introscope.agent.enterprisemanager.transport.tcp.socketfactory.DEFAULT=com.wily.isengard.postofficehub.link.net.DefaultSocketFactory

# The following connection properties enable the Agent to tunnel communication 
# to the Enterprise Manager over HTTP.
#
# WARNING: This type of connection will impact Agent and Enterprise Manager 
# performance so it should only be used if a direct socket connection to the 
# the Enterprise Manager is not feasible. This may be the case if the Agent 
# is isolated from the Enterprise Manager with a firewall blocking all but 
# HTTP traffic.
# 
# When enabling the HTTP tunneling Agent, uncomment the following host, port, 
# and socket factory properties, setting the host name and port for the 
# Enterprise Manager Web Server. Comment out any other connection properties 
# assigned to the "DEFAULT" channel and confirm that the "DEFAULT" channel is 
# assigned as a value for the "introscope.agent.enterprisemanager.connectionorder" 
# property.
# You must restart the managed application before changes to this property take effect.
#introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT=localhost
#introscope.agent.enterprisemanager.transport.tcp.port.DEFAULT=8081
#introscope.agent.enterprisemanager.transport.tcp.socketfactory.DEFAULT=com.wily.isengard.postofficehub.link.net.HttpTunnelingSocketFactory

# The following properties are used only when the Agent is tunneling over HTTP 
# and the Agent must connect to the Enterprise Manager through a proxy server 
# (forward proxy). Uncomment and set the appropriate proxy host and port values. 
# If the proxy server cannot be reached at the specified host and port, the 
# Agent will try a direct HTTP tunneled connection to the Enterprise Manager 
# before failing the connection attempt.
# Whereas if the proxy server can be reached at the host and port provided, but the authentication fails,
# the agent will repeatedly keep trying to make a connection to the Enterprise Manager through the proxy. 
# You must restart the managed application before changes to this property take effect.
#introscope.agent.enterprisemanager.transport.http.proxy.host=
#introscope.agent.enterprisemanager.transport.http.proxy.port=

# The following properties are used only when the proxy server requires 
# authentication. Uncomment and set the user name and password properties.
# You must restart the managed application before changes to this property take effect.
# For NTLM credentials you must separate domain name from user name by escaped backslash
# e.g. mydomain.com\\jack01
#introscope.agent.enterprisemanager.transport.http.proxy.username=
#introscope.agent.enterprisemanager.transport.http.proxy.password=

# To connect to the Enterprise Manager using HTTPS (HTTP over SSL),
# uncomment these properties and set the host and port to the EM's secure https listener host and port.
#introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT=localhost
#introscope.agent.enterprisemanager.transport.tcp.port.DEFAULT=8444
#introscope.agent.enterprisemanager.transport.tcp.socketfactory.DEFAULT=com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory

# To connect to the Enterprise Manager using SSL,
# uncomment these properties and set the host and port to the EM's SSL server socket host and port.
#introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT=localhost
#introscope.agent.enterprisemanager.transport.tcp.port.DEFAULT=5443
#introscope.agent.enterprisemanager.transport.tcp.socketfactory.DEFAULT=com.wily.isengard.postofficehub.link.net.SSLSocketFactory


# Additional properties for connecting to the Enterprise Manager using SSL.
#
# Location of a truststore containing trusted EM certificates.
# If no truststore is specified, the agent trusts all certificates.
# Either an absolute path or a path relative to the agent's working directory.
# On Windows, backslashes must be escaped.  For example: C:\\keystore
#introscope.agent.enterprisemanager.transport.tcp.truststore.DEFAULT=
# The password for the truststore
#introscope.agent.enterprisemanager.transport.tcp.trustpassword.DEFAULT=
# Location of a keystore containing the agent's certificate.
# A keystore is needed if the EM requires client authentication.
# Either an absolute path or a path relative to the agent's working directory.
# On Windows, backslashes must be escaped.  For example: C:\\keystore
#introscope.agent.enterprisemanager.transport.tcp.keystore.DEFAULT=
# The password for the keystore
#introscope.agent.enterprisemanager.transport.tcp.keypassword.DEFAULT=
# Set the enabled cipher suites.
# A comma-separated list of cipher suites.
# If not specified, use the default enabled cipher suites.
#introscope.agent.enterprisemanager.transport.tcp.ciphersuites.DEFAULT=


#################################
# Enterprise Manager Failback Retry Interval
#
# ================
# When the Agent is configured to have multiple Enterprise Managers
# in its connection order and this property is enabled, the Introscope 
# Agent will automatically attempt to connect to the Enterprise Manager
# in its connection order to which it can connect in allowed mode.
# In case no such Enterprise Manager is found, the reconnection attempt 
# will occur on a regular interval as specified.
# Agent will not connect to any Enterprise Manager in disallowed mode,  
# when this property is enabled.
# You must restart the managed application before changes to this property take effect.

#introscope.agent.enterprisemanager.failbackRetryIntervalInSeconds=120


#######################
# Custom Process Name
#
# ================
# Specify the process name as it should appear in the
# Introscope Enterprise Manager and Workstation.
# You must restart the managed application before changes to this property take effect.

#introscope.agent.customProcessName=CustomProcessName


#######################
# Default Process Name
#
# ================
# If no custom process name is provided and the
# agent is unable to determine the name of the
# main application class, this value will be
# used for the process name.
# You must restart the managed application before changes to this property take effect.

introscope.agent.defaultProcessName=Collector


#######################
# Agent Name
#
# ================
# Specify the name of this agent as it appears in the
# Introscope Enterprise Manager and Workstation.

# Use this property if you want to specify the Agent
# Name using the value of a Java System Property.
# You must restart the managed application before changes to this property take effect.
introscope.agent.agentNameSystemPropertyKey=

# This enables/disables auto naming of the agent using
# an Application Server custom service.
# You must restart the managed application before changes to this property take effect.
introscope.agent.agentAutoNamingEnabled=false

# The amount of time to delay connecting to the Introscope Enterprise
# Manager while Agent Auto Naming is attempted.
# You must restart the managed application before changes to this property take effect.
introscope.agent.agentAutoNamingMaximumConnectionDelayInSeconds=120

# When Agent Auto Naming is enabled, the Agent will check for 
# a new Application Server determined name on the specified interval.
# You must restart the managed application before changes to this property take effect.
introscope.agent.agentAutoRenamingIntervalInMinutes=10

# Auto name of log files (Agent, AutoProbe and LeakHunter) with
# the Agent name or a timestamp can be disabled by setting the 
# value of this property to 'true'.  Log file auto naming only 
# takes effect when the Agent name can be determined using a 
# Java System Property or an Application Server custom service.
# You must restart the managed application before changes to this property take effect.
introscope.agent.disableLogFileAutoNaming=false

# Uncomment this property to provide a default Agent Name 
# if the other methods fail.
# You must restart the managed application before changes to this property take effect.
introscope.agent.agentName=Agent

# Fully Qualified Domain Name (FQDN) can be enabled by setting this property  
# value to 'true'. By Default (false) it will display HostName.
# Set to 'true' when integrating with Catalyst.
# You must restart the managed application before changes to this property take effect.
introscope.agent.display.hostName.as.fqdn=false


#######################
# Agent Extensions Directory
#
# ================
# This property specifies the location of all extensions to be loaded
# by the Introscope Agent.  Non-absolute names are resolved relative 
# to the location of this properties file.
# You must restart the managed application before changes to this property take effect.

introscope.agent.extensions.directory=../../core/ext


#######################
# Agent Common Directory
#
# ================
# This property specifies the location of common directory to be loaded
# by the Introscope Agent.  Non-absolute names are resolved relative 
# to the location of this properties file.
# You must restart the managed application before changes to this property take effect.

introscope.agent.common.directory=../../common


#######################
# Platform Monitor Configuration
#
# ================
# Use this property to override the Agent's default Platform Monitor
# detection. To override the default, uncomment the relevant
# definition of introscope.agent.platform.monitor.system from those
# shown below.
# 32-Bit platform monitor binaries can work only on 32-bit JVMs and
# 64-Bit platform monitor binaries can work only on 64-bit JVMs.
# You must restart the managed application before changes to this property take effect.

#introscope.agent.platform.monitor.system=SolarisAmd32
#introscope.agent.platform.monitor.system=SolarisAmd64
#introscope.agent.platform.monitor.system=SolarisSparc32
#introscope.agent.platform.monitor.system=SolarisSparc64
#introscope.agent.platform.monitor.system=AIXPSeries32
#introscope.agent.platform.monitor.system=AIXPSeries64
#introscope.agent.platform.monitor.system=HP-UXItanium32
#introscope.agent.platform.monitor.system=HP-UXItanium64   
#introscope.agent.platform.monitor.system=HP-UXParisc32
#introscope.agent.platform.monitor.system=HP-UXParisc64
#introscope.agent.platform.monitor.system=WindowsIntelAmd32
#introscope.agent.platform.monitor.system=WindowsIntelAmd64
#introscope.agent.platform.monitor.system=LinuxIntelAmd32
#introscope.agent.platform.monitor.system=LinuxIntelAmd64 

#######################
# SQL Agent Configuration
#
# You must restart the managed application before changes to these properties take effect.
# Configuration settings for Introscope SQL Agent
# ================

# Turns off metrics for individual SQL statements. The default value is false.
#introscope.agent.sqlagent.sql.turnoffmetrics=false

# Report only Average Response Time metric for individual SQL statements. The default value is false.
#introscope.agent.sqlagent.sql.artonly=false

# Turn off transaction tracing for individual sql statements. The default value is false.
#introscope.agent.sqlagent.sql.turnofftrace=false

# Unnormalized sql will appear as parameter for Sql components in Transaction Trace 
# Caution: enabling this property may result in passwords and sensitive information to be presented in Transaction Trace
# The default value is false.
#introscope.agent.sqlagent.sql.rawsql=false

######################################
# SQL Agent Normalizer extension
#
# ================
# Configuration settings for SQL Agent normalizer extension


# Specifies the name of the sql normalizer extension that will be used 
# to override the preconfigured normalization scheme. To make custom 
# normalization extension work, the value of its manifest attribute 
# com-wily-Extension-Plugin-{pluginName}-Name should match with the 
# value given to this property. If you specify a comma separated list 
# of names, only the first name will be used. Example, 
# introscope.agent.sqlagent.normalizer.extension=ext1, ext2
# Only ext1 will be used for normalization. By default we now ship the  
# RegexSqlNormalizer extension
# Changes to this property take effect immediately and do not 
# require the managed application to be restarted.

#introscope.agent.sqlagent.normalizer.extension=RegexSqlNormalizer

##############################
# RegexSqlNormalizer extension
#
# ==================
# The following properties pertain to RegexSqlNormalizer which 
# uses regex patterns and replace formats to normalize the sql in 
# a user defined way. 


# This property if set to true will make sql strings to be
# evaluated against all the regex key groups. The implementation
# is chained. Hence, if the sql matches multiple key groups, the
# normalized sql output from group1 is fed as input to group2 and 
# so on. If the property is set to 'false', as soon as a key group  
# matches, the normalized sql output from that group is returned
# Changes to this property take effect immediately and do not require 
# the managed application to be restarted.
# Default value is 'false'
#introscope.agent.sqlagent.normalizer.regex.matchFallThrough=true

# This property specifies the regex group keys. They are evaluated in order
# Changes to this property take effect immediately and do not 
# require the managed application to be restarted.
#introscope.agent.sqlagent.normalizer.regex.keys=key1

# This property specifies the regex pattern that will be used
# to match against the sql. All valid regex alowed by java.util.Regex
# package can be used here.
# Changes to this property take effect immediately and do not 
# require the managed application to be restarted.
# eg: (\\b[0-9,.]+\\b) will filter all number values, ('.*?') will filter
# anything between single quotes, ((?i)\\bTRUE\\b|\\bFALSE\\b) will filter
# boolean values from the query.
#introscope.agent.sqlagent.normalizer.regex.key1.pattern=(".*?")|('.*?')|(\\b[0-9,.]+\\b)|((?i)\\bTRUE\\b|\\bFALSE\\b)

# This property if set to 'false' will replace the first occurrence of the
# matching pattern in the sql with the replacement string. If set to 'true'
# it will replace all occurrences of the matching pattern in the sql with
# replacement string
# Changes to this property take effect immediately and do not 
# require the managed application to be restarted.
# Default value is 'false'
#introscope.agent.sqlagent.normalizer.regex.key1.replaceAll=true

# This property specifies the replacement string format. All valid 
# regex allowed by java.util.Regex package java.util.regex.Matcher class
# can be used here.
# eg: The default normalizer replaces the values with a question mark (?)
# Changes to this property take effect immediately and do not 
# require the managed application to be restarted.
#introscope.agent.sqlagent.normalizer.regex.key1.replaceFormat=?

# This property specifies whether the pattern match is sensitive to case
# Changes to this property take effect immediately and do not 
# require the managed application to be restarted.
#introscope.agent.sqlagent.normalizer.regex.key1.caseSensitive=false



#######################
# Agent Metric Clamp Configuration
#
# ================
# The following setting configures the Agent to approximately clamp the number of metrics sent to the EM  
# If the number of metrics pass this metric clamp value then no new metrics will be created.  Old metrics will still report values.
# If the property is not set then no metric clamping will occur. 
# Changes to this property take effect immediately and do not require the managed application to be restarted. 
# introscope.agent.metricClamp=5000


#######################
# Transaction Tracer Configuration
#
# ================
# Configuration settings for Introscope Transaction Tracer

# Uncomment the following property to specify the maximum number of components allowed in a Transaction 
# Trace.  By default, the clamp is set at 5000.   
# Note that any Transaction Trace exceeding the clamp will be discarded at the agent, 
# and a warning message will be logged in the Agent log file.
# Warning: If this clamp size is increased, the requirement on the memory will be higher and
# as such, the max heap size for the JVM may need to be adjusted accordingly, or else the 
# managed application may run out of memory.
# Changes to this property take effect immediately and do not require the managed 
# application to be restarted.
#introscope.agent.transactiontrace.componentCountClamp=5000

# Uncomment the following property to specify the maximum depth of components allowed in
# head filtering, which is the process of examining the start of a transaction for
# the purpose of potentially collecting the entire transaction.  Head filtering will
# check until the first blamed component exits, which can be a problem on very deep
# call stacks when no clamping is done.  The clamp value will limit the memory and
# CPU utilization impact of this behavior by forcing the agent to only look up to a
# fixed depth.  By default, the clamp is set at 30.   
# Note that any Transaction Trace whose depth exceeds the clamp will no longer be examined
# for possible collection UNLESS some other mechanism, such as sampling or user-initiated
# transaction tracing, is active to select the transaction for collection.
# Warning: If this clamp size is increased, the requirement on the memory will be higher and
# as such, garbage collection behavior may be affected, which will have an application-wide
# performance impact.
# Changes to this property take effect immediately and do not require the managed application to be restarted.
#introscope.agent.transactiontrace.headFilterClamp=30

# Uncomment the following property to disable Transaction Tracer Sampling
# Changes to this property take effect immediately and do not require the managed application to be restarted.
introscope.agent.transactiontracer.sampling.enabled=true

# The following property limits the number of transactions that are reported by the agent 
# per reporting cycle. The default value if the property is not set is 200.
# You must restart the managed application before changes to this property take effect.
introscope.agent.ttClamp=500


########################
# TT Sampling
# ================
# These are normally configured in the EM. Configuring in the Agent disables configuring
# them in the EM
# You must restart the managed application before changes to this property take effect.
#
introscope.agent.transactiontracer.sampling.perinterval.count=10000
introscope.agent.transactiontracer.sampling.interval.seconds=120

#######################
# URL Grouping Configuration
#
# ================
# Configuration settings for Frontend naming.  By default, all frontends
# go into the "Default" group.  This is done so that invalid URLs (i.e.
# those that would generate a 404 error) do not create unique, one-time
# metrics -- this can bloat the EM's memory.  To get more meaningful
# metrics out of the Frontends|Apps|URLs tree, set up URL groups that
# are relevant to the deployment
# Changes to this property take effect immediately and do not require the managed application to be restarted.
introscope.agent.urlgroup.keys=products,items,socket,default
introscope.agent.urlgroup.group.products.pathprefix=/api/Products*
introscope.agent.urlgroup.group.products.format=Products
introscope.agent.urlgroup.group.items.pathprefix=/api/Items*
introscope.agent.urlgroup.group.items.format=Items
introscope.agent.urlgroup.group.socket.pathprefix=/socket.io*
introscope.agent.urlgroup.group.socket.format=Socket
introscope.agent.urlgroup.group.default.pathprefix=*
introscope.agent.urlgroup.group.default.format=Default

# Configuration settings for Backend URL Path naming.  By default, all 
# HTTP backends URL path go into "Default" group. This is hot property.
# It is applicable for metric path Backends|WebService at {protocol}_//{host}_{port}|Paths tree.

introscope.agent.backendpathgroup.keys=default
introscope.agent.backendpathgroup.group.default.pathprefix=*
introscope.agent.backendpathgroup.group.default.format=Default


#######################
# Error Detector Configuration
#
# ================
# Configuration settings for Error Detector

# Please include errors.pbd in your pbl (or in introscope.autoprobe.directivesFile)

# The error snapshot feature captures transaction details about serious errors
# and enables recording of error count metrics.
# Changes to this property take effect immediately and do not require the managed application to be restarted.
introscope.agent.errorsnapshots.enable=true

# The following setting configures the maximum number of error snapshots
# that the Agent can send in a 15-second period.
# Changes to this property take effect immediately and do not require the managed application to be restarted.
introscope.agent.errorsnapshots.throttle=10

# The following series of properties lets you specify error messages 
# to ignore.  Error snapshots will not be generated or sent for
# errors with messages matching these filters.  You may specify
# as many as you like (using .0, .1, .2 ...). You may use wildcards (*).  
# The following are examples only.
# Changes to this property take effect immediately and do not require the managed application to be restarted.
#introscope.agent.errorsnapshots.ignore.0=*com.company.HarmlessException*
#introscope.agent.errorsnapshots.ignore.1=*HTTP Error Code: 404*

# Minimum threshold for stall event duration
# Changes to this property take effect immediately and do not require the managed application to be restarted.
introscope.agent.stalls.thresholdseconds=30

# Frequency that the agent checks for stall events
# Changes to this property take effect immediately and do not require the managed application to be restarted.
introscope.agent.stalls.resolutionseconds=10

# Treat PHP script aborting through die() or exit() as error. The default value if the property is not set is false
# Changes to this property take effect immediately and do not require the managed application to be restarted.
introscope.agent.php.error.on.abort=true


#######################
# Dynamic Instrumentation Settings 
# ================================= 
# This feature enables changes to PBDs to take effect without restarting the application server or the agent process.  
# This is a very CPU intensive operation, and it is highly recommended to use configuration to minimize the classes that are 
# being redefined.PBD editing is all that is required to trigger this process. 
  
# Enable/disable the dynamic instrumentation feature. 
# You must restart the managed application before changes to this property take effect.
#introscope.autoprobe.dynamicinstrument.enabled=true 
 
# The polling interval in minutes to poll for PBD changes 
# You must restart the managed application before changes to this property take effect.
#introscope.autoprobe.dynamicinstrument.pollIntervalMinutes=1 
    

################################
# Agent Metric Aging
# ==============================
# Detects metrics that are not being updated consistently with new data and removes these metrics.
# By removing these metrics you can avoid metric explosion.    
# Metrics that are in a group will be removed only if all metrics under this group are considered candidates for removal.
# BlamePointTracer metrics are considered a group.  
#
# Enable/disable the metric agent aging feature. 
# Changes to this property take effect immediately and do not require the managed application to be restarted.
introscope.agent.metricAging.turnOn=true
#
# The time interval in seconds when metrics are checked for removal
# You must restart the managed application before changes to this property take effect.
introscope.agent.metricAging.heartbeatInterval=1800
#
# During each interval, the number of metrics that are checked for metric removal
# Changes to this property take effect immediately and do not require the managed application to be restarted.
introscope.agent.metricAging.dataChunk=500
#
# The metric becomes a candidate for removal when it reaches the number of intervals set (numberTimeslices) and has not invoked any new data points during that period.  
# If the metric does invoke a new data point during that period then the numberTimeslices resets and starts over.  
# Changes to this property take effect immediately and do not require the managed application to be restarted.
#introscope.agent.metricAging.numberTimeslices=3000
#
# You can choose to ignore metrics from removal by adding the metric name or metric filter to the list below.  
# Changes to this property take effect immediately and do not require the managed application to be restarted.
introscope.agent.metricAging.metricExclude.ignore.0=Threads*

# To ignore ChangeDetector.AgentID  metric from metric aging.
introscope.agent.metricAging.metricExclude.ignore.1=ChangeDetector.AgentID


#########################################
# ChangeDetector configuration properties
# =======================================
# On/Off Switch
#
# ================
# This boolean property gives you the ability to enable
# Introscope ChangeDetector by settings the property value
# to true. It is set to false by default.
# You must restart the managed application before changes to this property take effect.
#introscope.changeDetector.enable=false
#######################
# Root directory 
#
# ================
# The root directory is the folder where ChangeDetector creates its local cache files. 
# Use a backslash to escape the backslash character, as in the example.   
#introscope.changeDetector.rootDir=c:\\sw\\AppServer\\wily\\change_detector
#######################
# Startup wait time 
#
# ================
# Time to wait after agent starts before trying to connect to the Enterprise manager
#introscope.changeDetector.isengardStartupWaitTimeInSec=15
#######################
# Interval between connection attempts
#
# ================
# Specify the number of seconds to wait before retrying connection to the Enterprise manager
#introscope.changeDetector.waitTimeBetweenReconnectInSec=10
#######################
# Agent ID
#
# ================
# A string used by ChangeDetector to identify this agent
#introscope.changeDetector.agentID=SampleApplicationName
#
#######################
# Data source configuration file path 
#
# ================
# The absolute or relative path to the ChangeDetector datasources configuration file.
# Use a backslash to escape the backslash character.   
#introscope.changeDetector.profile=../../common/ChangeDetector-config.xml
#
#######################
# Data source configuration file directory
#
# ================
# The absolute or relative path to the datasource configuration file(s) directory.
# Use a backslash to escape the backslash character.
# All datasource configuration file(s) from this directory will be used in addition
# to any file specified by introscope.changeDetector.profile property.
#introscope.changeDetector.profileDir=changeDetector_profiles
#
#######################
# Data Compression 
#
# ================
# Enabling these properties will allow compression on the 
# Change Detector data buffer. This could be useful 
# if you experience memory consumption at start-up 
# Default property value is "false"
# You must restart the managed application before changes to this property take effect.
#
#introscope.changeDetector.compressEntries.enable=true
#
# The following property defines the batch size for the compression job
# You must restart the managed application before changes to this property take effect.
#introscope.changeDetector.compressEntries.batchSize=1000


#######################
# Garbage collection and Memory Monitoring 
#
# ================
# Enable/disable Garbage Collection monitor
# Changes to following property take effect immediately and do not require the managed application to be restarted.

introscope.agent.gcmonitor.enable=true

######################################################
# Thread Dump Collection
######################################################

# Enable/disable Thread Dump Feature support.
introscope.agent.threaddump.enable=true

# Configure the maximum stack elements the Thread dump can have,
# If the user configures the max stack elements beyond 25000,
# The property is reset to the default value of 12000

introscope.agent.threaddump.MaxStackElements=12000

# Enable/disable DeadLock poller Metric support.
introscope.agent.threaddump.deadlockpoller.enable=false

# The property determines the interval in which the Agent queries for any deadlock in the system.
introscope.agent.threaddump.deadlockpollerinterval=15000

#######################
# Primary network interface selector
#
# Agent reports details of host computer's primary interface (IP address, MAC address, host & domain name). 
# If following property is unset, agent will attempt to determine an appropriate interface as the primary
# interface.  If an alternate interface is required, uncomment following property and specify required 
# interface identity.
# Change to this property takes effect immediately and do not require the managed application to be restarted.
#
#introscope.agent.primary.net.interface.name=eth0.0


#######################
#  Transaction Structure aging properties
#
# ================
# This property is to evaluate the number of elements in the transaction structure at the period interval,
# to determine if "emergency aging" is required.
# Default value is "30000"
# com.wily.introscope.agent.harvesting.transaction.creation.checkperiod=30000       

# This property specifies the period in milliseconds that the aging for the transaction structure is checked, 
# Default value is "30000"
# com.wily.introscope.agent.harvesting.transaction.aging.checkperiod=30000
 
# This property specifies the minimum amount in milliseconds that a tree in the transaction structure must be inactive before it is purged.
# The inactivity does not imply that it will be aged out.
# Default value is "60000"
# com.wily.introscope.agent.harvesting.transaction.aging.period=60000 
             
# This property sets the maximum percentage increment in the size of the structure that is allowed to happen before aging of the transaction structure is forced
# If the change in the number of nodes between the aging periods is more than this percentage value, then checking for aging occurs
# if set to a small value, the transaction structure will be aged more frequently, and the memory utilization of the agent will be therefore 
# kept lower.
# Default value is "5", i.e. 5%
# com.wily.introscope.agent.harvesting.transaction.aging.attentionlevel.percentage=5        
 
# This property sets the maximum absolute increment in the size of the structure that is allowed to happen before aging of the transaction structure is forced
# If the change in the number of nodes between the aging periods is more than this percentage value, then checking for aging occurs
# if set to a small value, the transaction structure will be aged more frequently, and the memory utilization of the agent will be therefore 
# kept lower.
# Default value is "100000"
# com.wily.introscope.agent.harvesting.transaction.attentionlevel.absolute=100000

# This property is used to avoid spikes in memory utilization of the transaction structure.
# If there is an increase of elements at any time bigger than a third of this value,
# then "emergency aging" occurs immediately. Emergency aging will agent parts of the transaction structures that are younger than the 
# value specified in com.wily.introscope.agent.harvesting.transaction.aging.period,  and will likely reduce the amount of data sent by the agent.
# Only modify this value if the memory requirement are very strict. 
# Default value is "100000"
# com.wily.introscope.agent.harvesting.transaction.creation.attentionlevel.absolute=100000

# This property specifies the maximum duration in milliseconds of the aging process. It is used to avoid long aging process when 
# resources available are not sufficient. 
# default value if 30000
# com.wily.introscope.agent.harvesting.transaction.aging.duration.max=30000
 
#######################
#  Transaction Structure properties
#
# ================
# Enable/disable to shut down globally the transaction trace feature.
# Default value is "true"
# com.wily.introscope.agent.blame.transaction.doTransactionTrace=true

# Enable/disable high concurrency mode for all repositories.
# Set to true, it will use more memory but may give more throughput
# Default value is "false"
# com.wily.introscope.agent.blame.highconcurrency.enabled=false

# This property defines the number of stripes in the striped repositories
# It works when the high concurrency mode is on,
# which is "com.wily.introscope.agent.blame.highconcurrency.enabled=true"
# Default value is "16"
# com.wily.introscope.agent.blame.highconcurrency.stripes=16

# Enable using the same accumulator for all Blame point metrics, independently 
# from they position in the transaction structure. This is using less memory but also 
# may affect throughput under extreme concurrency situations.
# Default value is "true". Set to false only if you detect sever throughput degradation.
# You will need to wait for the transaction structure and the metrics to age for this property
# to have effect 
# com.wily.introscope.agent.blame.com.wily.introscope.agent.blame.usesharedaccumulators.enabled=true
 
# Enable/disable to removes stalls from all traces, and remove stall feature altogether.
# Default value is "true"
# com.wily.introscope.agent.blame.stall.trace.enabled=true

# Enable synchronized repositories instead of compare and swap repositories
# The synchronized repository is not used in java5 because of overhead in locking.
# the default value is true in java 6 and above, and false for java 5. In java 5, setting to false will cause overhead
# com.wily.introscope.agent.blame.synchronized.enabled=true

 
#######################
# Properties to activate sustainability metrics
#
# ================
# Sustainability metrics are generated to provide information on the agent health and
# internal status. There is a substantial overhead associated with these metrics, and therefore, their
# usage is not suggested at this time in production environments.
#
# Enable/disable to generate globally sustainability debug metrics.
# Set to true, it will generate globally sustainability debug metrics that can be used to evaluate the Transaction Structure
# Default value is "false"
# com.wily.introscope.agent.blame.transactions.debugmetrics.enabled=false           

# Enable/disable to generate sustainability metrics on the harvesting process.
# Default value is "false"
# com.wily.introscope.agent.harvesting.debugmetrics.enabled=false

# This property is to generate the metrics for the health of the data structures in the agent.
# Default value is "false"
# concurrentMapPolicy.generatemetrics=false   

#com.wily.introscope.agent.sustainabilitymetrics.enabled=true

#com.wily.introscope.agent.sustainabilitymetrics.metrics.enabled=true

#com.wily.introscope.agent.sustainabilitymetrics.report.enabled=true

#com.wily.introscope.agent.sustainabilitymetrics.report.frequency


##############################################
# Intelligent Instrumentation properties
#
##############################################

# This property enables and disables deep component visibility into error snapshots
# Change to this property takes effect immediately and do not require the 
# managed application to be restarted.
introscope.agent.deep.errorsnapshot.enable=true

#This property limits the maximum number of deep trace components in a Transaction Trace
# Change to this property takes effect immediately and do not require the 
# managed application to be restarted.
introscope.agent.deep.trace.max.components=5000

#This property limits the maximum number of consecutive deep trace components in a Transaction Trace
# Change to this property takes effect immediately and do not require the 
# managed application to be restarted.
introscope.agent.deep.trace.max.consecutive.components=1000


##################################################################
# Properties to activate Agent Controller Registration Extension
#
# ================
#
# Enable/disable Agent Controller Registration Extension.
# Set to true, it will allow the running APM Agent
# to register with the local Agent Controller.
# Default value is false.
# It is a hot property so only save the file after changing.
# Agent does not need restarting.
introscope.agent.acc.enable=false
#
# The port used to contact the Agent Controller. Default value is 51914
# introscope.agent.acc.port=51914
