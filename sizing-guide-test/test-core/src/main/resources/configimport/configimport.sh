#!/bin/bash
# runs the config import program

if [ "$JAVA_HOME" == "" ]; then
  echo "JAVA_HOME is not set.  Please set the environment variable JAVA_HOME to point to your JRE (1.6 or higher) root folder."
  exit
fi

javaExe=$JAVA_HOME/bin/java

#update PYTHONPATH so that egenix modules installed under python2.3 path are guaranteed to be picked up
export PYTHONPATH="$PYTHONPATH:/usr/lib/python2.3/site-packages"
lib_dir=../lib


class_dir=../:$lib_dir/com.wily.apm.dbtools_9.7.1.jar:$lib_dir/org.apache.jakarta_log4j_1.2.15.jar:$lib_dir/postgresql-9.2-1003.jdbc4.jar:$lib_dir/commons-lang-2.1.jar:$lib_dir/commons-configuration-1.1.jar:$lib_dir/commons-logging-1.0.4.jar:$lib_dir/commons-cli-1.2.0.jar:$lib_dir/ojdbc6.jar

java_vm_args="-Dlog4j.configuration=../../common/config/log4j-dbtools.properties"

# run the import program
if [ -x "$javaExe" ]; then
	echo y | "$javaExe" -Xms256M -Xmx1024M -cp $class_dir $java_vm_args com.wily.apm.dbtools.importexport.ConfigImport $*
	exit
fi

echo "$javaExe does not exist.  Please check that JAVA_HOME is pointing to the correct directory."
