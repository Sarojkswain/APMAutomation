#!/usr/bin/env python
# the line above is for the UNIX environment
#
# generate btstats files for processing by a Tess

import os
import sys
import time
import fcntl
import random
import socket
import struct
#import pyodbc
import psycopg2
import os
import random


def usage():
  print 'usage: %s [-d|-delay <delay>] [-dsn <ODBC DSN>] [-o|-outdir <path>] [-pwd|-password <password>] [-tfd|-template <template-dir>] [-uid|-user <user-id>] [-w|-wait <wait>] [-x|-exclude] cycles max-btstats [min-btstatts]' % sys.argv[0]
  print '  -d|-delay        > 0: start after waiting <delay> secs; < 0: start at <delay> secs before next hour'
  print '  -dsn             ODBC DSN'
  print '  -o|-outdir       output directory (default: /etc/wily/cem/tim/data/out/btstats)'
  print '  -pwd|-password   database password'
  print '  -tfd|-template   btstats template file directory'
  print '  -uid|-user       database user id'
  print '  -w|-wait         seconds between cycles (default: 15)'
  print '  -x|-exclude      exclude transets not created by create_transactions.py'
  print '  cycles           number of iterations [files generated] (0 = forever)'
  print '  max-btstats      max number of btstats per file'
  print '  min-btstats      min number of btstats per file [btstats per file randomly chosen between min & max]'

def xml_encode(str):
  return str.replace('<','&lt;').replace('>','&gt;').replace('&','&amp;')


i = 0
wait = 7
delay = 0
cycles = 0
outdir = None

#dsn='10.131.21.114:5432'
#dsn='10.131.21.114'

#aq-emgeo01
dsn='130.119.68.227'

#uid='admin'
uid='postgres'

#pwd='quality'
pwd='99Ball00ns'

#dbip='10.131.21.114'
dbip='130.119.68.227'

template_dir = ''
exclude = False
args = sys.argv[1:]
random.seed(os.getpid())
min_btstats_per_file = 10
max_btstats_per_file = 10

# parse the command line
while (i < len(args)):
  arg = args[i]
  if (arg in ('-h', '-help', '--help')):
    usage()
    sys.exit(0)
  elif (arg in ('-d', '-delay')):
    delay = int(args[i+1])
    i += 2
  elif (arg in ('-dsn')):
    dsn = args[i+1]
    i += 2
  elif (arg in ('-o', '-outdir')):
    outdir = args[i+1]
    i += 2
  elif (arg in ('-pwd', '-password')):
    pwd = args[i+1]
    i += 2
  elif (arg in ('-tfd', '-template')):
    template_dir = args[i+1]
    i += 2
  elif (arg in ('-uid', '-user')):
    uid = args[i+1]
    i += 2
  elif (arg in ('-w', '-wait')):
    wait = float(args[i+1])
    i += 2
  elif (arg in ('-x', '-exclude')):
    exclude = True
    i += 1
  elif (cycles is None):
    cycles = int(arg)
    i += 1
  elif (max_btstats_per_file is None):
    max_btstats_per_file = int(arg)
    i += 1
  else:
    min_btstats_per_file = int(arg)
    i += 1
    break

# supply the defaults
if (cycles == 0): cycles = 0x7FFFFFFF
if (outdir is None):
  outdir = "/opt/CA/APM/tim/data/out/btstats/"
elif (outdir[len(outdir)-1] != '/'):
  outdir += '/'

# validate the args
#if (i < len(args) or cycles is None or max_btstats_per_file is None or (min_btstats_per_file is not None and min_btstats_per_file >= max_btstats_per_file)):
# usage()
# sys.exit(1)


# print list of variable values - for debug purpose
print '========================================'
print 'Values used by this script:'
print "delay = '%s'" % (delay)
print "dsn = '%s'" % (dsn)
print "outdir = '%s'" % (outdir)
print "pwd = '%s'" % (pwd)
print "template_dir = '%s'" % (template_dir)
print "uid = '%s'" % (uid)
print "wait = '%s'" % (wait)
print "exclude = '%s'" % (exclude)
print "cycles = '%s'" % (cycles)
print "max_btstats_per_file = '%s'" % (max_btstats_per_file)
print "min_btstats_per_file = '%s'" % (min_btstats_per_file)
print '========================================'
#

# get this machines external hostname
s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
s.connect(("mail.ca.com",25))
socket.gethostbyaddr(s.getsockname()[0])
host = socket.gethostbyaddr(s.getsockname()[0])[0]

print "host = '%s'" % (host)

# read the btstats template file
tpf = open(template_dir + "btstats-template.xml")
template = tpf.read()
tpf.close()

# extract/replace the <BT> element
i = template.index('<BTlist>\n') + len('<BTlist>\n')
j = template.index('</BTlist>')
bt_template = template[i:j]
template = template[0:i] + '[BTS]' + template[j:]
template = template.replace('[HOST]', host)

try:
  # connect to the database
  #dbcnx = pyodbc.connect('DRIVER={PostgreSQL};SERVER='+dbip+';DATABASE=cemdb;UID=admin;PWD='+pwd)
  #dbcnx = psycopg2.connect("host=%s dbname=cemdbsrini user=%s password=%s" % (dsn, uid, pwd))
  dbcnx = psycopg2.connect("host=%s dbname=cemdb user=%s password=%s" % (dsn, uid, pwd))
  dbcur = dbcnx.cursor()

  # get the slow time defect defs
  dbcur.execute("select cast(ts_transet_id as numeric), cast(ts_id as numeric) from ts_defect_defs where ts_type=1 or ts_type=2 or ts_type=3 or ts_type=4 or ts_type=5 or ts_type=6")
  ids = dbcur.fetchall()
  if (len(ids) == 0):
    print "can't continue: no slow time defect defs in db"
    sys.exit(3)

  # free up the connection
  dbcnx.close()

  # delay generation if requested
  if (delay > 0):
    print "sleeping for '%s' seconds.." % (delay)
    time.sleep(delay)
  elif (delay < 0):
    now = time.time()
    nextHour = (int(now) + 3600) / 3600 * 3600
    print "sleeping for '%s' seconds.." % (nextHour-now+delay)
    time.sleep(nextHour-now+delay)

  
  # loop for all cycles
  for cycle in xrange(cycles):
    bts = []
    btstats = 0
    if (cycle > 0): time.sleep(wait)

    if (min_btstats_per_file is None):
      btstats_per_file = max_btstats_per_file
    else:
      btstats_per_file = random.randrange(min_btstats_per_file, max_btstats_per_file+1)
  
    # generate a file with the specified number of stats
    print btstats_per_file
    hostIp=1
    netIp=1
    defId=100001
    #defId=700000000000000000
    defectId1=700000000000000000
    defectId2=700000000000000001
    defectId3=700000000000000002
    defectId4=700000000000000003
    defectId5=700000000000000004
    defectId6=700000000000000005

    
    firstSubnet=1
    secondSubnet=1
    #print ids
    while (btstats < btstats_per_file):
      for id in ids:
        firstSubnet=random.randint(0, 255)
        secondSubnet=random.randint(0, 255)
        hostIp += 1
        defId += 1
        defectId1 += 1
        defectId2 += 1
        defectId3 += 1
        defectId4 += 1
        defectId5 += 1
        defectId6 += 1
        
	if hostIp == 254:
	  netIp +=1
	  hostIp = 1
		
        #print id
        #bts += [bt_template.replace('[BTID]', '100642').replace('[CLIENTIP]', str(id[1]).replace('[DDID]', str(id[1]))]
        bts += [bt_template.replace('[BTID]', str(defId)).replace('[DDID1]', str(defectId1)).replace('[DDID2]', str(defectId2)).replace('[DDID3]',str(defectId3)).replace('[DDID4]', str(defectId4)).replace('[DDID5]', str(defectId5)).replace('[DDID6]', str(defectId6)).replace('[FirstSubnet]',str(firstSubnet)).replace('[SecondSubnet]',str(secondSubnet))]
      	 #bts +=  [bt_template.replace('[BTID]',str(defId)).replace('[NetIp]',str(netIp)).replace('[HostIp]',str(hostIp)).replace('[FirstSubnet]',str(firstSubnet)).replace('[SecondSubnet]',str(secondSubnet)).replace('[DDID]', str(id[1]))]
	 #bts +=  [bt_template.replace('[BTID]',str(defId)).replace('[NetIp]',str(netIp)).replace('[HostIp]',str(hostIp)).replace('[DDID]', str(id[1]))]
	#bts +=  [bt_template.replace('[BTID]',str(defId)).replace('[FirstSubnet]',str(firstSubnet)).replace('[SecondSubnet]',str(secondSubnet)).replace('[DDID]', str(id[1]))]
        btstats += 1
        if (btstats >= btstats_per_file): break
  
    # write out the btstats file
    now = int(time.time())
    fn = 'btstats-' + time.strftime("%Y-%m-%d-%H:%M:%S", time.gmtime(now)) + '.xml'
    print "generating file '%s'..." % (fn),
    sys.stdout.flush()
    dff = open('/tmp/' + fn, 'w')
    file_data = template.replace('[TIME]', str(now)).replace('[BTS]', ''.join(bts))
    dff.write(file_data)
    dff.close()
    os.rename("/tmp/"+fn, outdir+fn);
    print "done"
except StandardError, ex:
  print ex
  sys.exit(2)
