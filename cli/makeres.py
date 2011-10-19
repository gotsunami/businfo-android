#!/usr/bin/env python
# -*- coding: latin-1 -*-

"""
Generic plain text parser.

Make Android XML resources for bus lines from row text.
Raw text is a copy of the PDF content using evince
"""

import sys, re, types, os.path, glob, tempfile
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
TMP_DIR = tempfile.gettempdir()
#
FETCH_GPS_URL = """http://maps.googleapis.com/maps/api/geocode/json?address=%s&sensor=false"""
GPS_CACHE_FILE = 'gps.csv'
GPS_RSRC_FILE = 'gps.xml'
g_cities = []

def get_cities_in_cache(cache_file):
    ccities = []
    try:
        f = open(cache_file)
        data = f.readlines()
        for line in data:
            ccities.append(line.split(';')[0])
        f.close()
    except IOError:
        print 'No cache found'

    return ccities

def get_gps_coords_from_cache(city, cache_file):
    try:
        f = open(cache_file)
        data = f.readlines()
        for line in data:
            k = line[:-1].split(';')
            c = k[0]
            if city == c:
                return map(lambda x: float(x), k[1:])
        # Not found in cache
        return float(0), float(0)
        f.close()
    except IOError:
        print 'No cache found'
        sys.exit(1)

def fetch_gps_coords(city):
    """
    Uses Google Geocoding API.
    See http://code.google.com/intl/fr/apis/maps/documentation/geocoding/
    """
    import urllib, urllib2, json
    lat = lng = 0
    #print FETCH_GPS_URL % urllib.quote(city + u', France')
    s = urllib2.urlopen(FETCH_GPS_URL % urllib.quote(city + u', France'))
    r = json.loads(s.read())
    if r['status'] != 'OK':
        print "Bad status %s, could not get data from city: %s" % (city, r['status'])
    else:
        gps = r['results'][0]['geometry']['location']
        lat, lng = gps['lat'], gps['lng']

    return lat, lng

def makeXML(busline, directions, outfile):
    global dfltCirculationPolicy
    global g_cities

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
                if city not in g_cities:
                    g_cities.append(city)
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

    g_cities.sort()
    print "[%-15s] %-30s (Dir: %d, Cit: %2d, Stations: %2d, Stops: %2d)" % (busline, "Generated %s" % outfile, nbDirections, nbCities, nbStations, nbStops)
    if DEBUG: print directions

def createDB():
    # Create DB structure
    print """
DROP TABLE IF EXISTS line;
CREATE TABLE line (
    id INTEGER PRIMARY KEY AUTOINCREMENT, 
    name TEXT, 
    from_city_id INTEGER, 
    to_city_id INTEGER,
    UNIQUE(name)
);

DROP TABLE IF EXISTS city;
CREATE TABLE city (
    id INTEGER PRIMARY KEY AUTOINCREMENT, 
    name TEXT,
    latitude REAL,
    longitude REAL,
    UNIQUE(name)
--    UNIQUE(latitude, longitude)
);

DROP TABLE IF EXISTS station;
CREATE TABLE station (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    latitude REAL,
    longitude REAL,
    city_id INTEGER,
    UNIQUE(name, city_id)
--    UNIQUE(latitude, longitude)
);

DROP TABLE IF EXISTS schedule;
CREATE TABLE schedule (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    time DATETIME,
    station_id INTEGER
);

DROP TABLE IF EXISTS line_station;
CREATE TABLE line_station (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    line_id INTEGER,
    station_id INTEGER,
    rank INTEGER,			                -- station's rank order on line
    direction_id INTEGER,			        -- city id for direction
    UNIQUE(line_id, station_id, rank, direction_id)
);

CREATE TRIGGER fki_line_from_city_id
BEFORE INSERT ON line
BEGIN
    SELECT RAISE(ROLLBACK, 'insert on table "line" violates foreign key constraint "fk_from_city_id"')
    WHERE (SELECT id FROM city WHERE id = NEW.from_city_id) IS NULL;
END;

CREATE TRIGGER fki_line_to_city_id
BEFORE INSERT ON line
BEGIN
    SELECT RAISE(ROLLBACK, 'insert on table "line" violates foreign key constraint "fk_to_city_id"')
    WHERE (SELECT id FROM city WHERE id = NEW.to_city_id) IS NULL;
END;

CREATE TRIGGER fki_station_city_id
BEFORE INSERT ON station
BEGIN
    SELECT RAISE(ROLLBACK, 'insert on table "station" violates foreign key constraint "fk_city_id"')
    WHERE (SELECT id FROM city WHERE id = NEW.city_id) IS NULL;
END;

CREATE TRIGGER fki_line_station_line_id
BEFORE INSERT ON line_station
BEGIN
    SELECT RAISE(ROLLBACK, 'insert on table "line_station" violates foreign key constraint "fk_line_id"')
    WHERE (SELECT id FROM line WHERE id = NEW.line_id) IS NULL;
END;

CREATE TRIGGER fki_line_station_station_id
BEFORE INSERT ON line_station
BEGIN
    SELECT RAISE(ROLLBACK, 'insert on table "line_station" violates foreign key constraint "fk_station_id"')
    WHERE (SELECT id FROM station WHERE id = NEW.station_id) IS NULL;
END;

-- Must be an existing city
CREATE TRIGGER fki_line_station_direction_id
BEFORE INSERT ON line_station
BEGIN
    SELECT RAISE(ROLLBACK, 'insert on table "line_station" violates foreign key constraint "fk_direction_id"')
    WHERE (SELECT id FROM city WHERE id = NEW.direction_id) IS NULL;
END;

CREATE TRIGGER fki_schedule_station_id
BEFORE INSERT ON schedule
BEGIN
    SELECT RAISE(ROLLBACK, 'insert on table "schedule" violates foreign key constraint "fk_station_id"')
    WHERE (SELECT id FROM station WHERE id = NEW.station_id) IS NULL;
END;
"""

def makeSQL(sources):
    global dfltCirculationPolicy
    global g_cities

    cities = set()
    stations = set()
    lines = set()
    lines_stations = set()
    for src in sources:
        busline, directions = parse(src)
#        print busline, directions[0]
        lines.add((busline, directions[0][-1]['city'], directions[1][-1]['city']))
        k = 0
        for direct in directions:
            rank = 1
            for data in direct:
                #print data, len(direct)
                #print data
                cities.add(data['city'])
                stations.add((data['station'], data['city']))
                lines_stations.add((busline, data['station'], rank, directions[k][-1]['city']))
                rank += 1
            k += 1

    pk = 1
    cs = []
    for city in cities:
        cs.append((pk, city))
        pk += 1

    for city in cs:
        print("INSERT INTO city VALUES(%d, \"%s\", 0, 0);" % (city[0], city[1]))

    pk = 1
    pk_city = 0
    pk_stations = {}
    for st in stations:
        for city in cs:
            if city[1] == st[1]:
                pk_city = city[0]
                break
        if pk_city == 0:
            print "Error: city id not found!"
            sys.exit(1)
        print("INSERT INTO station VALUES(%d, \"%s\", 0, 0, %d);" % (pk, st[0].encode('utf-8'), pk_city))
        pk_stations[st[0].encode('utf-8')] = pk
        pk += 1

    pk_from = pk_to = 0
    pk = 1
    for line in lines:
        for city in cs:
            if city[1] == line[1]:
                pk_from = city[0]
                break
        for city in cs:
            if city[1] == line[2]:
                pk_to = city[0]
                break
        if pk_from == 0 or pk_to == 0:
            print "Error: pk_from(%d) or pk_to(%d) id not found!" % (pk_from, pk_to)
            print "Line: " + str(line)
            sys.exit(1)
        print("INSERT INTO line VALUES(%d, \"%s\", %d, %d);" % (
            pk, line[0], pk_from, pk_to))
        pk += 1

    pk_line = pk_station = pk_direction = 0
    pk = 1
    for ls in lines_stations:
        j = 1
        h = 1
        for line in lines:
            if ls[0] == line[0]:
                pk_line = j
                break
            j += 1
        if pk_line == 0:
            print "Error: pk_line is 0!"
            sys.exit(1)
        for city in cs:
            if city[1] == ls[3]:
                pk_direction = city[0]
                break
        if pk_direction == 0:
            print "Error: pk_direction is 0!"
            sys.exit(1)
        print("INSERT INTO line_station VALUES(%d, %d, %d, %d, %d);" % (
            pk, pk_line, pk_stations[ls[1].encode('utf-8')], ls[2], pk_direction))
        pk += 1

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

    parser = OptionParser(usage="""
%prog [-d|-g|--gps|--gps-cache file|--sql] (raw_line.txt|dir)

Default behaviour is to output XML content. Use --sql to instead
generate SQL data.""")
    parser.add_option("-d", action="store_true", dest="debug", default=False, help='more debugging')
    parser.add_option("-v", '--verbose', action="store_true", dest="verbose", default=False, help='verbose output')
    parser.add_option("-g", action="store_true", dest="globalxml", default=False, help='generates global lines.xml')
    parser.add_option("", '--gps', action="store_true", dest="getgps", default=False, help='retreives cities GPS coordinates')
    parser.add_option("", '--gps-cache', action="store", dest="gpscache", default=GPS_CACHE_FILE, 
        help='use gps cache file')
    parser.add_option("", '--sql', action="store_true", dest="sql", default=False, help='generates SQL content only')
    options, args = parser.parse_args()

    if len(args) != 1:
        parser.print_usage()
        sys.exit(2)

    DEBUG = options.debug
    infile = args[0]
    if os.path.isdir(infile):
        sources = glob.glob(os.path.join(infile, '*.txt'))
        sources.sort()
        if options.sql:
            # Grouping all INSERTs in a single transaction really 
            # speeds up the whole thing
            print "BEGIN TRANSACTION;"
            createDB()
            makeSQL(sources)
            print "END TRANSACTION;"
            return

        for src in sources:
            busline, directions = parse(src)
            ext = '.xml'
            f = makeXML
            outfile = os.path.join(TMP_DIR, os.path.basename(src[:src.rfind('.')] + ext))
            f(busline, directions, outfile)

        if options.globalxml:
            dst = os.path.join(TMP_DIR, 'lines.xml')
            if os.path.exists(dst):
                os.remove(dst)
            xmls = glob.glob(os.path.join(TMP_DIR, '*.xml'))
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
            print "Generated global %s" % os.path.join(TMP_DIR, 'lines.xml')
    else:
        outfile = infile[:infile.rfind('.')] + '.xml'
        busline, directions = parse(infile)
        makeXML(busline, directions, outfile)

    if options.getgps:
        print "Getting GPS coordinates of cities ..."
        print "Using cache file %s ..." % options.gpscache
        ccities = get_cities_in_cache(options.gpscache) # cities in cache
        ncities = []                    # not in cache yet

        f = open(options.gpscache, 'a')
        for city in g_cities:
            if city not in ccities:
                lat, lng = fetch_gps_coords(city)
                f.write(';'.join([city, str(lat), str(lng)]) + '\n')
                ccities.append(city)
                ncities.append(city)
                print "N %-25s @%f, %f" % (city, lat, lng)
            else:
                lat, lng = get_gps_coords_from_cache(city, options.gpscache)
                if lat == 0 and lng == 0:
                    print "%s not found in cache... ANOMALY" % city
                    sys.exit(1)
                else:
                    if options.verbose:
                        print "C %-25s @%f, %f" % (city, lat, lng)
        f.close()

        print "%d cities in cache" % len(ccities)
        print "%d cities added in cache" % len(ncities)

        ores = os.path.join(TMP_DIR, GPS_RSRC_FILE)
        print "Generating resource file %s ..." % ores
        f = open(options.gpscache)
        data = f.readlines()
        f.close()

        f = open(ores, 'w')
        f.write(XML_HEADER)
        f.write('<gps>\n')
        for line in data:
            k = line[:-1].split(';')
            f.write(' ' * INDENT + '<city name="%s" lat="%s" lng="%s" />\n' % (k[0], k[1], k[2]))
        f.write('</gps>\n')


if __name__ == '__main__':
    main()

