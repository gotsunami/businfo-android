#!/usr/bin/env python
# -*- coding: latin-1 -*-

"""
Make DB queries
"""

import os, sys, re, os.path, glob
from optparse import OptionParser
from makeres import TMP_DIR
import sqlite3

SQLITEDB = 'ht.sqlite'
# Global SQLite DB connection
CONN = None
DEBUG = False

def graph_line(num_line, c, render=True):
    c.execute('select id, name from line order by name')
    k = 1
    line = None
    for li in c:
        if k == num_line:
            line = li
            break
        k += 1
    if not line:
       print "Line with id %d not found." % num_line
       sys.exit(2)
    if render:
        print "Graphing line %s" % line[1].encode('utf-8')
    if DEBUG:
        print "Line id is %d" % line[0]

    # Fetch all stations from line (2 directions)
    c.execute("select station_id, direction_id from line_station where line_id=? order by rank", (line[0],))
    stations = {}
    for st in c:
        if not stations.has_key(st[1]):
            stations[st[1]] = []
        stations[st[1]].append(st[0])
    if DEBUG:
        print stations

    # Get station's names
    prepare = {stations.keys()[0]: [], stations.keys()[1]: []}
    for direction, sts in stations.iteritems():
        for stid in sts:
            c.execute("select s.name, c.name from station as s, city as c where s.id=? and s.city_id=c.id", (stid,))
            data = c.fetchone()
            stname = "%s, %s" % (data[0].encode('utf-8'), data[1].encode('utf-8'))
            if DEBUG:
                print "%d: %s" % (direction, stname)
            prepare[direction].append(stname)
        if DEBUG:
            print

    # Draw graph
    if render:
        g_name = os.path.join(TMP_DIR, "line_%d.dot" % num_line)
        f = open(g_name, 'w')
        f.write("digraph G {\n")
        f.write('  node[fontsize=8];\n');
        f.write('  edge[color=red];\n');
        for direction, sts in prepare.iteritems():
            f.write('    ' + ' -> '.join(["\"" + st + "\"" for st in sts]) + ';\n')
            f.write('  edge[color=blue];\n');
        f.write('}\n')
        f.close()
        print "Done. Wrote %s." % g_name

    if render:
        p_name = os.path.join(TMP_DIR, "line_%d.png" % num_line)
        print "Creating PNG graph ...",
        sys.stdout.flush()
        os.system("dot -Tpng %s > %s" % (g_name, p_name))
        print p_name

    return prepare

def get_random_color():
    from random import randint
    rgb = []
    for k in range(3):
        c = hex(randint(1, 255))[2:]
        if len(c) == 1:
            c = '0' + c
        rgb.append(c)
    return ''.join(rgb)

def graph_network(c):
    c.execute('select id, name from line order by name')
    lines = c.fetchall()
    g_name = os.path.join(TMP_DIR, "network.dot")
    k = 1
    print "Generating DOT graph ..."
    f = open(g_name, 'w')
    f.write("digraph G {\n")
    f.write('  node[fontsize=8];\n');
    print "Analyzing lines:",
    for li in lines:
        print li[1].encode('utf-8') + ',',
        sys.stdout.flush()
        data = graph_line(k, c, render=False)
        f.write("  edge[color=\"#%s\"];\n" % get_random_color());
        for direction, sts in data.iteritems():
            f.write('    ' + ' -> '.join(["\"" + st + "\"" for st in sts]) + ';\n')
        k += 1
    print
    f.write('}\n')
    print "Done. Wrote %s." % g_name
    f.close()

    p_name = os.path.join(TMP_DIR, "network.png")
    print "Creating PNG graph ...",
    sys.stdout.flush()
    os.system("dot -Tpng %s > %s" % (g_name, p_name))
    print p_name

def main():
    global CONN, DEBUG

    parser = OptionParser(usage="""%prog [--path|--stations|--cities] [dbfile]""")
    parser.add_option("-d", '', action="store_true", dest="debug", default=False, help='Debug output')
    parser.add_option("-p", '--path', action="store", dest="path", default=False, help='Compute path')
    parser.add_option("-c", '--cities', action="store_true", dest="cities", default=False, help='Show list of cities')
    parser.add_option("-s", '--stations', action="store_true", dest="stations", default=False, help='Show list of stations')
    parser.add_option("-l", '--lines', action="store_true", dest="lines", default=False, help='Show list of lines')
    parser.add_option("", '--graph-line', action="store", metavar="LINE_NUM", type="int", dest="graphline", default=None, help='Graph a line with all stations (dot graphviz)')
    parser.add_option("-n", '--network', action="store_true", dest="network", default=False, help='Graph full HT network')
    parser.add_option("-f", '--find', action="store", dest="find", metavar="KEYWORD", default=None, help='Search the DB for a match in city, line or station name')

    options, args = parser.parse_args()
    if len(args) > 1:
        parser.print_usage()
        sys.exit(2)

    # Default path to DB
    db_path = os.path.join(os.getenv('HOME'), SQLITEDB)
    if len(args) == 1:
        db_path = args[0]

    if not os.path.exists(db_path):
        print "DB '%s' not found. Exiting." % db_path
        sys.exit(2)

    DEBUG = options.debug
    CONN = sqlite3.connect(db_path)
    c = CONN.cursor()
    c.execute('select count(*) from line')
    num_lines = c.fetchone()[0]
    c.execute('select count(*) from city')
    num_cities = c.fetchone()[0]
    c.execute('select count(*) from station')
    num_stations = c.fetchone()[0]

    print "Using database: %s" % db_path
    print "%d lines, %d cities, %d stations" % (num_lines, num_cities, num_stations)

    if options.cities:
        c.execute('select name from city order by name')
        k = 1
        for city in c:
            print "%3d. %s" % (k, city[0].encode('utf-8'))
            k += 1

    if options.stations:
        c.execute('select s.name, c.name from station as s, city as c where s.city_id=c.id order by c.name')
        k = 1
        for st in c:
            print "%3d. %s, %s" % (k, st[0].encode('utf-8'), st[1].encode('utf-8'))
            k += 1

    if options.lines:
        c.execute('select name from line order by name')
        k = 1
        for li in c:
            print "%3d. %s" % (k, li[0].encode('utf-8'))
            k += 1

    # Graph line ID
    if options.graphline:
        graph_line(options.graphline, c)
    elif options.network:
        graph_network(c)

    c.close()

if __name__ == '__main__':
    main()

