#!/usr/bin/env python
# -*- coding: latin-1 -*-

"""
Generic plain text parser.

Make Android XML resources for bus lines from row text.
Raw text is a copy of the PDF content using evince
"""

import sys, re, types, os.path, glob
from optparse import OptionParser

DFLT_CIRC_POLICY = '1-6'
TIME_PAT = r'^\d{1,2}:\d{2}$'
CIRC_PAT = r'^\*(.*)\*$'
STOP_CIRC_PAT = r'^(\d{1,2}:\d{2})\*(.*)\*$'
INDENT = 2
DEBUG = False
dfltCirculationPolicy = DFLT_CIRC_POLICY
XML_HEADER = """<?xml version="1.0" encoding="utf-8"?>
<!-- GENERATED AUTOMATICALLY BY THE makeres.py SCRIPT. DO NOT MODIFY! -->
"""

def makeXML(busline, directions, outfile):
    global dfltCirculationPolicy
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

    f.write(XML_HEADER)
    f.write("""<line id="%s">\n""" % busline)
    for data in directions:
        curDirection = data[-1]['city']
        f.write(' ' * INDENT + """<direction id="%s" c="%s">\n""" % (curDirection.encode('utf-8'), dfltCirculationPolicy))
        curCity = None
        for station in data:
            city = station['city']
            if city != curCity:
                if curCity != None:
                    f.write(' ' *2*INDENT + "</city>\n")
                f.write(' ' *2*INDENT + """<city id="%s">\n""" % city.encode('utf-8'))
                if city not in tmpCities:
                    tmpCities.append(city)
                    nbCities += 1
                curCity = city
            f.write(' ' *3*INDENT + """<station id="%s">\n""" % station['station'].encode('utf-8'))
            for stop in station['stops']:
                if type(stop) == types.TupleType:
                    f.write(' ' *4*INDENT + """<s t="%s" c="%s"/>\n""" % (stop[0], stop[1]))
                else:
                    f.write(' ' *4*INDENT + """<s t="%s"/>\n""" % stop)
                
                nbStops += 1
            f.write(' ' *3*INDENT + "</station>\n")
            if station['station'] not in tmpStations:
                tmpStations.append(station['station'])
                nbStations += 1
        f.flush()
        f.write(' ' *2*INDENT + "</city>\n")
        f.write(' ' * INDENT + "</direction>\n")
        nbDirections += 1
    f.write("</line>")
    f.close()

    print "[%-15s] %-30s (Dir: %d, Cit: %2d, Stations: %2d, Stops: %2d)" % (busline, "Generated %s" % outfile, nbDirections, nbCities, nbStations, nbStops)
    if DEBUG: print directions

def parse(infile):
    """
    Simple raw data parser
    """
    global dfltCirculationPolicy

    data = []
    try:
        f = open(infile)
        data = [d.strip() for d in f.readlines()]
        f.close()
    except IOError, e:
        print "Can't open file: %s" % e
        sys.exit(1)

    # Removes empty lines and comments (^#)
    data = [d for d in data if len(d) > 0 and d[0] != '#']

    if not data:
        print "Empty content"
        sys.exit(1)

    data = map(lambda x: unicode(x, 'utf-8'), data)
    directions = []

    BUSLINE_PAT = 'name='
    CIRCULATION_PAT = 'circulation='
    DIRECTION_PAT = 'direction='
    CITY_PAT = 'city='
    FROM_PAT = 'from='
    TO_PAT = 'to='

    k = -1
    curCity = None
    curLines = None
    for line in data:
        if line.startswith(DIRECTION_PAT):
            directions.append([])
            k += 1
        elif line.startswith(CIRCULATION_PAT):
            dfltCirculationPolicy = re.sub(CIRCULATION_PAT, '', line)
        elif line.startswith(CITY_PAT):
            curCity = re.sub(CITY_PAT, '', line)
        elif line.startswith(BUSLINE_PAT):
            busline = re.sub(BUSLINE_PAT, '', line).encode('utf-8')
        elif line.startswith(FROM_PAT):
            pass
        elif line.startswith(TO_PAT):
            pass
        else:
            # This is a station line
            sts = line.split(';')
            if len(sts) == 0:
                # No station line??
                raise ValueError, "Not a station line: %s" % line
            stops = sts[1:]
            allstops = []
            nextPolicy = dfltCirculationPolicy
            for stop in stops:
                m = re.match(CIRC_PAT, stop)
                if m:
                    nextPolicy = m.group(1)
                    continue
                else:
                    if re.match(TIME_PAT, stop):
                        if nextPolicy == dfltCirculationPolicy:
                            allstops.append(stop)
                        else:
                            allstops.append((stop, nextPolicy))
                    elif re.match(STOP_CIRC_PAT, stop):
                        m = re.match(STOP_CIRC_PAT, stop)
                        allstops.append((m.group(1), m.group(2)))

            directions[k].append({
                'city': curCity.strip().capitalize(), 
                'station': sts[0].strip(), 
                'stops': allstops,
            })

    return (busline, directions)

def main():
    global DEBUG

    parser = OptionParser(usage="Usage: %prog [-d] (raw_line.txt|dir)")
    parser.add_option("-d", action="store_true", dest="debug", default=False, help='more debugging')
    parser.add_option("-g", action="store_true", dest="globalxml", default=False, help='generates global lines.xml')
    options, args = parser.parse_args()

    if len(args) != 1:
        parser.print_usage()
        sys.exit(2)

    DEBUG = options.debug
    infile = args[0]
    if os.path.isdir(infile):
        sources = glob.glob(os.path.join(infile, '*.txt'))
        sources.sort()
        for src in sources:
            outfile = src[:src.rfind('.')] + '.xml'
            busline, directions = parse(src)
            makeXML(busline, directions, outfile)

        if options.globalxml:
            dst = os.path.join(infile, 'lines.xml')
            if os.path.exists(dst):
                os.remove(dst)
            xmls = glob.glob(os.path.join(infile, '*.xml'))
            xmls.sort()
            f = open(dst, 'w')
            f.write(XML_HEADER)
            f.write('<lines>\n')
            for src in xmls:
                s = open(src)
                f.write(''.join(s.readlines()[2:]) + '\n')
                f.flush()
                s.close()
            f.flush()
            f.write('</lines>\n')
            f.close()
            print "Generated global %s" % os.path.join(infile, 'lines.xml')
    else:
        outfile = infile[:infile.rfind('.')] + '.xml'
        busline, directions = parse(infile)
        makeXML(busline, directions, outfile)

if __name__ == '__main__':
    main()
