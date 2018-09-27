<?xml version="1.0" encoding="utf-8"?>
<project name="wurlitzer-runner" default="execute" basedir="${baseDir}">
    <import file="scripts/xml/wurlitzer.systest.ant.includes.xml" />
    <target name="execute">
        <wurlitzer-em-appmap-stress-test
                scenarioFile="${wurlitzer.scenarioFile}"
                durationHours="${wurlitzer.durationHours?c}"
                debug="${wurlitzer.debug?c}"
                initialEdgeSetDelayMinutes="${wurlitzer.initialEdgeSetDelayMinutes?c}"
                hostname="${wurlitzer.em.host}"
                port="${wurlitzer.em.port?c}" />
    </target>
</project>
