#!/bin/sh

LOG=/tmp/fw.log

# Default values
DEFHOST="usilca31.ca.com"
DEFPORT=15030
DEFGOPORT=11111
DEFLOCALPORT=8888

################################################################################

function LogAndOut {
    echo -e `date` "$1" | tee -a ${LOG}
}

# If the first parameter is &, relaunch self in background with remaining
# parameters
if [ $# -ge 1 ] && [ $1 = '&' ]; then
    shift
    SCRIPT=`readlink -f $0`
    LogAndOut "Scheduling ${SCRIPT} $*"
    nohup sh ${SCRIPT} $* &>/dev/null 2>&1 &
    exit 0;
fi

if [ -z $* ]; then
    LogAndOut "Started"
else
    LogAndOut "Started with parameters: $*"
fi

if [ $# -ge 1 ] && [ $1 = '--help' ]; then
    echo "$0 [<host>|<ip> [<target-port>[ <go-port> [<local-port>]]]]"
    exit 0;
fi

if [ $# -ge 1 ] && [ ! -z $1 ]; then
    HOST=$1
else
    HOST=$DEFHOST
fi

if [ $# -ge 2 ] && [ $2 -ge 1 ] && [ $2 -le 65535 ]; then
    PORT=$2
else
    PORT=$DEFPORT
fi

if [ $# -ge 3 ] && [ $3 -ge 1 ] && [ $3 -le 65535 ]; then
    GOPORT=$3
else
    GOPORT=$DEFGOPORT
fi

if [ $# -ge 4 ] && [ $4 -ge 1 ] && [ $4 -le 65535 ]; then
    LOCALPORT=$4
else
    LOCALPORT=$DEFLOCALPORT
fi

HOSTIP=`nslookup $HOST | grep "Address: " | tail -1 | cut -f 2 -d':' | tr -d ' '`
if [ -z $HOSTIP ]; then
    HOSTIP=$HOST
fi

NDEV=`ip addr | grep -o "eth[[:digit:]]\+:.*UP" | cut -f 1 -d':'`
if [ -z $NDEV ]; then
    LogAndOut "Failed to identify any available network device, will abort"
    exit 1
fi

LogAndOut "Will forward localhost:${LOCALPORT} to ${HOSTIP}:${PORT} using device ${NDEV}"

# Wait until we receive the go signal from the Forwarder role.
LogAndOut "Waiting for go signal on port ${GOPORT} ..."
perl - ${GOPORT} << '__HERE__' | tee -a ${LOG}
use IO::Socket;
my $port = $ARGV[0];
my $listen =
    IO::Socket::INET->new(LocalPort => $port, Listen => 2, Proto => "tcp", ReuseAddr => 1)
    or die "Failed to listen on port $port: $!";
$listen->accept if (defined $listen);
__HERE__
LogAndOut "Received go signal, proceeding"

# Enable ip forwarding
sysctl -q net.ipv4.ip_forward=1

# Flush any existing rules
iptables -F
iptables -t nat -F

# Setup fowarding
iptables -t nat -A PREROUTING  -p tcp --dport ${LOCALPORT} -j DNAT --to-destination ${HOSTIP}:${PORT}
iptables -t nat -A POSTROUTING -p tcp -d ${HOSTIP} --dport ${PORT} -o ${NDEV} -j MASQUERADE

# Log the nat table status
LogAndOut "NAT table status after modifications:\n$(iptables -t nat -L -n)"

LogAndOut "Done"
