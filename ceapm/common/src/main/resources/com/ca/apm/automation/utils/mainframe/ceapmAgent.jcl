//WILYZTXX JOB 123300000
/*JOBPARM SYSAFF=(*)
//        EXPORT SYMLIST=*
//*=============================================================
//*  CONTACT INFO:  JAN PODROUZEK      X25940
//*  ALTERNATE:     VOJTECH BISEK      X25919
//*  DESCRIPTION:   CE-APM AGENT
//*  CO-REQ:        NONE
//*  ESTIMATED CPU: VARIES
//*  EST. ELAPSED:  VARIES
//*  STARTUP:       MVS S <JCLNAME>
//*  NORMAL STOP:   MVS P <JOBNAME>
//*  CANCEL:        NO DUMP REQUIRED
//*  SPECIAL INFO:  USED FOR CE-APM AUTOMATED TESTING
//*
//*  REGION:        Prague
//*  LAST UPDATED:  20 May 2016
//*=============================================================
//*
//* Documentation: http://goo.gl/EGdcMt
//*
//WILYZOS PROC ACTION='START',
//   VERSION='7',            < JVM version number
//   RELEASE='1',            < JVM release number
//   MINOR='0',              < JVM minor number
//   PGMSUFF='71',           < JVM program number
//   USSPATH='J7.1',         < JVM USS folder name
//   LOGLVL='+I'             < Debug LVL: +I(info) +T(trc) ...
//*
// SET VER=&VERSION
// SET REL=&RELEASE
// SET MIN=&MINOR
// SET USSFOL=&USSPATH
// SET ARGS='2>DD:STDMSG'
// SET JCLPFX='WILY.STDENV.'
//*
// SET SYSVIEW=WILY.SYSV.SYSV141.CNM4BLOD.&SYSNAME
// SET INSTALL='/u/users/wily/test/xx'
// SET OPTS='PATHOPTS=(OWRONLY,OCREAT,OAPPEND)'
// SET MODE='PATHMODE=(SIRUSR,SIWUSR,SIRGRP,SIWGRP)'
// SET LEPARM=''
//*
//JAVAJVM EXEC PGM=JVMLDM&PGMSUFF.,REGION=0M,
//             PARM='&LEPARM/&LOGLVL &ARGS'
//*
//STEPLIB  DD  DISP=SHR,DSN=&SYSVIEW
//*
//STDOUT   DD PATH='&INSTALL./logs/&ACTION..stdout.log',&OPTS,&MODE
//SYSOUT   DD PATH='&INSTALL./logs/&ACTION..sysout.log',&OPTS,&MODE
//STDERR   DD PATH='&INSTALL./logs/&ACTION..stderr.log',&OPTS,&MODE
//STDMSG   DD PATH='&INSTALL./logs/&ACTION..stdmsg.log',&OPTS,&MODE
//*SYSPRINT DD PATH='&INSTALL./logs/&ACTION..sysprint.log',&OPTS,&MODE
//SYSPRINT DD  SYSOUT=*
//CEEDUMP  DD PATH='&INSTALL./logs/&ACTION..ceedump.log',&OPTS,&MODE
//*
//STDENV   DD  *,SYMBOLS=JCLONLY
export SA_INSTALL=&INSTALL
export STEPLIB=&SYSVIEW
export JAVA_HOME=/sys/java31bt/v&VER.r&REL.m&MIN./usr/lpp/java/&USSFOL

SYSVIEWPATH=/u/users/wily/sysview/141/CNM4BJAR
IRRRACFPATH=/usr/include/java_classes

JVM_OPTS="-Xmx512m -Xms256m"
#JVM_OPTS="$JVM_OPTS -ea"
#JVM_OPTS="$JVM_OPTS -Xhealthcenter:port=<p>,transport=jrmp"
#JVM_OPTS="$JVM_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,\
#server=y,address=<p>,suspend=n"
#JVM_OPTS="$JVM_OPTS -verbose:class"

export TZ="EST5EDT"
export PATH=/bin:/usr/lpp/Printsrv/bin:/usr/sbin:/sys/s390util/bin:\
$JAVA_HOME/bin
export LIBPATH=$SYSVIEWPATH:$JAVA_HOME/bin:$JAVA_HOME/bin/classic:/usr/lib

export CLASSPATH=$JAVA_HOME/lib
export JZOS_JVM_OPTIONS="-Djava.ext.dirs=\
$SA_INSTALL/lib:\
$SYSVIEWPATH:\
$IRRRACFPATH:\
$JAVA_HOME/lib:\
$JAVA_HOME/lib/ext"

export IBM_JAVA_OPTIONS="$JVM_OPTS \
-Dcom.wily.IntroscopeCAPI.config.APIconfig=\
$SA_INSTALL/data/IntroscopeCAPIConfig.xml \
-Dcom.wily.introscope.agentProfile=\
$SA_INSTALL/config/Introscope_Cross-Enterprise_APM.profile"
//         DD  DISP=SHR,DSN=&JCLPFX.CNTL(&ACTION)
//        PEND
//*
//WILYZOS EXEC WILYZOS
