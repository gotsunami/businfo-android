#!/usr/bin/env python
# -*- coding: latin-1 -*-

"""
Generic plain text parser.

Make Android XML resources for bus lines from row text.
Raw text is a copy of the PDF content using evince
"""

import sys, re
from optparse import OptionParser

TIME_PAT = r'\d{2}:\d{2}'
INDENT = 2
DEBUG = False

def makeXML(busline, directions, outfile):
    try:
        f = open(outfile, 'w')
    except IOError, e:
        print "Error: %s" % e
        sys.exit(1)

    nbDirections = 0
    nbCities = 0
    nbStations = 0
    nbStops = 0
    # Used to count distinct entries
    tmpCities = tmpStations = []

    f.write("""<?xml version="1.0" encoding="utf-8"?>\n""")
    f.write("<!-- GENERATED AUTOMATICALLY BY THE makeres.py SCRIPT. DO NOT MODIFY!\n")
    f.write("     LINE %s\n-->\n" % busline)
    f.write("""<line id="%s">\n""" % busline)
    for data in directions:
        curDirection = data['stations'][-1]['city']
        f.write(' ' * INDENT + """<direction id="%s">\n""" % curDirection.encode('latin-1'))
        curCity = None
        for station in data['stations']:
            city = station['city']
            if city != curCity:
                if curCity != None:
                    f.write(' ' *2*INDENT + "</city>\n")
                f.write(' ' *2*INDENT + """<city id="%s">\n""" % city.encode('latin-1'))
                if city not in tmpCities:
                    tmpCities.append(city)
                    nbCities += 1
                curCity = city
            f.write(' ' *3*INDENT + """<station id="%s">\n""" % station['station'].encode('latin-1'))
            k = 0
            for stop in station['stops']:
                if re.match(TIME_PAT, stop):
                    sline = ''
                    if data.has_key('lines'):
                        try:
                            sline = " l=\"%s\"" % data['lines'][k]
                        except IndexError:
                            print "\nError @ k=%d for %s, %s @ %s:" % (k, curDirection.encode('latin-1'), station['station'].encode('latin-1'), stop.encode('latin=-1'))
                            print data['lines']
                            print len(data['lines']), len(station['stops'])
                    f.write(' ' *4*INDENT + """<stop t="%s"%s />\n""" % (stop, sline))
                    nbStops += 1
                k += 1
            f.write(' ' *3*INDENT + "</station>\n")
            if station['station'] not in tmpStations:
                tmpStations.append(station['station'])
                nbStations += 1
        f.write(' ' * INDENT + "</direction>\n")
        nbDirections += 1
    f.write("</line>")
    f.close()
    print "[%s] %-30s (Dir: %d, Cit: %2d, Stations: %2d, Stops: %2d)" % (busline, "Generated %s" % outfile, nbDirections, nbCities, nbStations, nbStops)
    if DEBUG: print directions

def parse(infile):
    data = []
    try:
        f = open(infile)
        data = f.readlines()
        f.close()
    except IOError, e:
        print "Can't open file: %s" % e
        sys.exit(1)

    if not data:
        print "Empty content"
        sys.exit(1)

    data = map(lambda x: unicode(x, 'utf-8'), data)
    directions = []
    busline = None
    curDirection = -1
    curCity = None
    NUM_LINE = unicode('N° de ligne', 'latin-1')

    for line in data:
        line = line[:-1] # Removes trailing \n
        if line.startswith('Ligne') or line.startswith('Horaires ligne'):
            m = re.search(r'\d+(\/\d+)?', line)
            if m: busline = m.group(0)
        elif re.search(NUM_LINE, line):
            directions.append({'stations': []})
            curDirection += 1
            line = [k.strip() for k in line.split('  ') if re.search(r'\d+', k)]
            directions[curDirection]['lines'] = line
        elif re.search(TIME_PAT, line):
            line = [k.strip() for k in line.split('  ') if len(k) > 0]
            j = 0
            if line[0].isupper():
                curCity = line[0].capitalize()
                station = line[1]
                j = 2
            else:
                station = line[0]
                j = 1

            stops = []
            for stop in line[j:]:
                m = re.findall(r'\d{2}:\d{2}', stop)
                if m: 
                    stops.extend(m)
                else:
                    stops.append(stop)
            directions[curDirection]['stations'].append({'city': curCity, 'station': station, 'stops': stops})

    if busline is None:
        print "Could not find bus line information."
        sys.exit(2)
    return (busline, directions)

def main():
    global DEBUG
    parser = OptionParser(usage="Usage: %prog [-d] raw_line.txt")
    parser.add_option("-d", action="store_true", dest="debug", default=False)
    options, args = parser.parse_args()

    if len(args) != 1:
        parser.print_usage()
        sys.exit(2)

    DEBUG = options.debug
    infile = args[0]
    outfile = infile[:infile.rfind('.')] + '.xml'

    busline, directions = parse(infile)
    makeXML(busline, directions, outfile)

if __name__ == '__main__':
    main()
