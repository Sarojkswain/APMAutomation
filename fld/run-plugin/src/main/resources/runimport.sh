#!/bin/sh
#
# Shell script for configuring postgresql and restoring database
#
# Author: Pavel Tavoda
# Date: 24. Mar 2015
#
################################################################################
# DESCRIPTION:
#
# ${install.dir} refers to the directory where postgres is installed.
#
# 1) After installing, edit ${install.dir}/database/data/postgresql.conf
#    - Uncomment and change the value to “effective_cache_size = 2048MB”
#    - Set pg_log=/pg_log (instead of the default "pg_log")
# 2) Create the directory /pg_log if it doesn't exist
# 3) Restart the postgres server (service postgresql-9.2 restart) and verify
#    that it is running (listening on port 5432)
# 4) Check if the previous version is available at /automation/domainconfig/latest.
#    This path should be settable as a start form parameter
# 5) If the previous version exists, import it using the script in
#    ${install.dir}/install
################################################################################

################################################################################
# Pretty print
################################################################################
# Output to terminal?
if [ -t 1 ]; then
	reset="\033[m"
	headS="\033[33;1m********************************************************************************$reset\n\033[33m"
	headE="$reset\n"
	footS=""
	footE=""
	errS="\033[31;1m"
	errE="$reset\n"
	msgS="\033[34m"
	msgE="$reset\n"
else
	headS="********************************************************************************\n"
	headE="\n"
	footS=""
	footE="\n"
	errS=""
	errE="\n"
	msgS=""
	msgE="\n"
fi

printHead() { echo -n "${headS}$1${headE}"; }
printFoot() { echo -n "${footS}$1${footE}"; }
printErr() { echo -n "${errS}$1${errE}"; }
printMsg() { echo -n "${msgS}$1${msgE}"; }

################################################################################
# Print Usage
################################################################################
printUsage() {
	cat <<-EOF

	USAGE:
	    `basename $0` [options]

	OPTIONS:
	    -installdir DIRECTORY
	        Installation directory usually /data/APM
	    [-lastversion DIRECTORY]
	        Location of previous version (default: /automation/domainconfig/latest)
	    -dbhost HOSTNAME
	         Hostname where database is running
	    -dbname DBSID
	         Name of database
	    [-dbport PORT]
	         Database port where DB server is listening (default: 5432)
	    -dbuser USERNAME
	         User which can be used to connect to DB instance
	    -dbpassword PASSWORD
	         Password for DB user
	    -importfile FILE
	         File to use for importing database
	    [-targetrelease VERSION]
	         Boolean if you run at 64 bit version (default: 99.99.0.0)
	    -dbserviceuser USERNAME
	         Username under which database is running, usually postgres
	    -dbservicepwd PASSWORD
	         Password for user specified in -dbserviceuser
	    [-databasetype DBTYPE]
	         Boolean if you run at 64 bit version (default: postgres)
	    [-is64Bit BOOLEAN]
	         Boolean if you run at 64 bit version (default: true)
	EOF
}

################################################################################
# Parse command line
################################################################################
parseLine() {
	REQ_PARAMS="-installdir -dbhost -dbname -dbuser -dbpassword -importfile
		-dbserviceuser -dbservicepwd"
	OPT_PARAMS="-lastversion -dbport -targetrelease -databasetype -is64Bit"
	error=false
	while [ $# -gt 0 ]; do
		paramName="$1"
		varName=""
		if `echo $REQ_PARAMS $OPT_PARAMS | grep -q -- ${paramName}`; then
			varName="${paramName#-}"
		fi
		shift

		if [ -z "$varName" ]; then
			printErr "ERROR: Wrong parameter '$paramName'"
			error=true
		else
			paramVal="$1"
			if [ -z $paramVal ]; then
				printErr "ERROR: missing parameter for '$paramName'"
				error=true
			elif [ ! "$paramVal" = "${paramVal#-}" ]; then
				# paramVal start with -
				printErr "ERROR: missing parameter for '$paramName'"
				error=true
			else
				shift
				eval ${varName}=${paramVal}
			fi
		fi
	done

	# Check required params
	for i in $REQ_PARAMS; do
		eval val=\$${i#-}
		if [ -z $val ]; then
			printErr "ERROR: Missing required param $i"
			error=true
		fi
	done

	if [ $error = 'false' ]; then
		printMsg "VALUES:"
		for i in $REQ_PARAMS; do
			eval val=\$${i#-}
			printMsg "    ${i#-}=$val"
		done
		printMsg "    --- optional ---"
		for i in $OPT_PARAMS; do
			eval val=\$${i#-}
			printMsg "    ${i#-}=$val"
		done
	fi
}

updateConf() {
	# Update existing value, uncomment when commented
	sed -i "s%^#\?[ \t]*\($1[ \t]*=[ \t]*[^ \t]*\)%$1 = $2%" $pFile
	# Fallback if option is not available in predefined config
	grep -q "^$1\>" $pFile || echo "#$1 = $2" >> $pFile
	# Print error if still missing
	grep -q "^$1\>" $pFile || printErr "ERROR: Missing option $1 in $pFile"
}

################################################################################
# Main
################################################################################

# Default values for optional parameters
dbport=5432
databasetype=postgres
targetrelease=99.99.0.0
is64Bit=true
lastversion=/automation/domainconfig/latest

################################################################################
# Parse command line
printHead "Parse command line"
error=false
# Can't start with | because this will execute in subshell
parseLine $* > /tmp/qq$$ 2>&1
sed 's/^/    /' /tmp/qq$$
rm -rf /tmp/qq$$
if [ $error = 'true' ]; then
	printUsage
	exit 1
fi
printFoot

################################################################################
# Update postgresql conf file
printHead "Updating postgres conf file $pFile"
{
	pFile=$installdir/database/data/postgresql.conf
	cp $pFile /tmp/postgresql.conf.bak$$
	updateConf effective_cache_size 2048MB
	updateConf log_directory "'/pg_log'"
} 2>&1 | sed 's/^/    /'
printFoot

################################################################################
printHead "Creating /pg_log directory"
{
	if [ ! -d '/pg_log' ]; then
		mkdir /pg_log
		chown $dbserviceuser /pg_log
	else
		printMsg "Directory /pg_log already available"
		ls -ld /pg_log
	fi
} 2>&1 | sed 's/^/    /'
printFoot

################################################################################
printHead "Restart PostgreSQL"
{
	if [ -f /etc/init.d/postgresql ]; then
		# SYSV init
		/etc/init.d/postgresql restart
	else
		service postgresql restart
	fi
} 2>&1 | sed 's/^/    /'
printFoot

################################################################################
printHead "Check if Postgres is listenning"
{
	listen=false
	i=0
	while [ $i -lt 10 ]; do
		echo 'abc\n' | telnet $dbhost $dbport 2>/dev/null | fgrep -q Connected
		if [ $? -eq 0 ]; then
			listen=true
			break
		fi
		sleep 1
		i=$((i+1))
	done

	if [ $listen = 'false' ]; then
		printErr "ERROR: Server is not listening after restart on $dbhost:$dbport"
	else
		printMsg "OK"
	fi
} 2>&1 | sed 's/^/    /'
printFoot

################################################################################
printHead "Check for latest version"
if [ -f "$lastversion" ]; then
{
	importScriptDir=$installdir/install/database-scripts/unix
	printMsg "Run import script $importScriptDir/configimport.sh"
	dbscriptsdir=$installdir/install/database-scripts/

	cd $importScriptDir
	./configimport.sh -dbhost $dbhost -dbname $dbname -dbport $dbport \
		-databasetype $databasetype -dbuser $dbuser -dbpassword $dbpassword \
		-dbscriptsdir $dbscriptsdir -importfile $importfile \
		-targetrelease $targetrelease -dbserviceuser $dbserviceuser \
		-dbservicepwd $dbservicepwd -postgresinstalldir ${installdir}/database \
		-is64Bit $is64Bit
} 2>&1 | sed 's/^/    /'
else
	printMsg "    Can NOT find latest version in $lastversion"
fi
printFoot
