#!/usr/bin/python

import datetime
import sys

def gettime(start, step=10, count=50):
    k = start.find(':')
    if k == -1:
        raise ValueError, "Missing hh:mm format"
    h, m = int(start[:k]), int(start[k+1:])
    now = datetime.datetime.now()
    now = now.replace(hour=h, minute=m, second=0)

    td = datetime.timedelta(minutes=step)
    for i in range(count):
        now += td
        print "%02d:%02d" % (now.hour, now.minute),
    print

if __name__ == '__main__':
    if len(sys.argv) == 1:
        print "usage: %s hh:mm step count" % sys.argv[0]
        sys.exit(2)
    gettime(sys.argv[1], *(map(int, sys.argv[2:])))

