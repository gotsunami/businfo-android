#!/usr/bin/env python
# -*- coding: latin-1 -*-

"""
Generic plain text parser.

Make Android XML resources for bus lines from row text.
Raw text is a copy of the PDF content using evince
"""

import sys, re

TIME_PAT = r'\d{2}:\d{2}'
INDENT = 2

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

    f.write("""<?xml version="1.0" encoding="utf-8"?>\n""")
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
                nbCities += 1
                curCity = city
            f.write(' ' *3*INDENT + """<station id="%s">\n""" % station['station'].encode('latin-1'))
            for stop in station['stops']:
                if re.match(TIME_PAT, stop):
                    f.write(' ' *4*INDENT + """<stop t="%s" />\n""" % stop)
                    nbStops += 1
            f.write(' ' *3*INDENT + "</station>\n")
            nbStations += 1
        f.write(' ' * INDENT + "</direction>\n")
        nbDirections += 1
    f.write("</line>")
    f.close()
    print "[%s] %-30s (Dir: %d, Cit: %2d, Stations: %2d, Stops: %2d)" % (busline, "Generated %s" % outfile, nbDirections, nbCities, nbStations, nbStops)

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
            if m:
                busline = m.group(0)
                directions.append({'stations': []})
                curDirection += 1
        elif re.search(NUM_LINE, line):
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
    if len(sys.argv) != 2:
        print "Usage: %s line.txt" % sys.argv[0]

    infile = sys.argv[1]
    outfile = infile[:infile.rfind('.')] + '.xml'

    busline, directions = parse(infile)
    makeXML(busline, directions, outfile)

if __name__ == '__main__':
    main()
