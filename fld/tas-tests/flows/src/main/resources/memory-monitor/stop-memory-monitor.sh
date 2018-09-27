#!/bin/bash

cd `dirname $0`

exec kill `ps aux | fgrep java | fgrep MemoryMonitorRunner | awk '{print $2;}'` 2>/dev/null
