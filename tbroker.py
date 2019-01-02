#!/usr/bin/python
import os
import os.path
from os.path import expanduser
import glob
import sys
from shutil import copyfile

# console_port: tcp port of the debug console
def tbroker_start(console_port):
	drivers=glob.glob("*driver*.jar")
	cp=""
	for d in drivers:
		cp+=d+os.pathsep
	cmd ="java -cp " + cp + "./build/libs/tbroker-1.0-all.jar tbroker.Shell "
	cmd+="\"sconsole " + str(console_port) + "\" "
	cmd+="?DMYTIE"
	os.system(cmd)

if __name__ == '__main__':
    tbroker_start(5690)
