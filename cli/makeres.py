#!/usr/bin/env python
# -*- coding: latin-1 -*-

"""
Generic plain text parser.

Make Android SQL resources for bus lines from row text.
Raw text is a copy of the PDF content using Acrobat Reader
(evince has some issues with copy/paste schedules).
"""

import sys, re, types, os.path, glob, tempfile
import hashlib, shutil
import json, subprocess
from optparse import OptionParser
#
# Local import of DBSTRUCT
import sqlitedb, mysqldb

DBSTRUCT = None
#
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
g_prefilter = None
#
RAW_DB_FILE = 'htdb.sql'
CHKSUM_FILE = '.checksum'
CHKSUM_DB_FILE = 'dbversion.xml'
DB_STATS_FILE = 'dbstats.xml'
CHUNK_DB_FILE = 'htdb-chunks.xml'
CHUNK_PREFIX = 'htdb_chunk'
CHUNK_SIZE = 64 * 1024
# Networks definition file
NETWORKS_FILE = 'src/networks.json'

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

    Every city is searched only in Hérault, France.
    """
    import urllib, urllib2, json
    lat = lng = 0
    s = urllib2.urlopen(FETCH_GPS_URL % urllib.quote(city + ', Hérault, France'))
    r = json.loads(s.read())
    if r['status'] != 'OK':
        print "Bad status %s, could not get data from city: %s" % (r['status'], city.encode('utf-8'))
    else:
        gps = r['results'][0]['geometry']['location']
        lat, lng = gps['lat'], gps['lng']

    return lat, lng

def makeXML(busline, directions, outfile, linecolor):
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
    f.write("""<line id="%s" color="%s">\n""" % (busline, linecolor))
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

def makeSQL(sources, out):
    global dfltCirculationPolicy
    global db_city_count, db_line_count, db_station_count
    global g_cities
    db_city_count = db_line_count = db_station_count = 0

    # FIXME: only Herault Transport supported (and hardcoded!) for now. Must add 
    # a way to support any other network.
    networks = (u'Hérault Transport',)
    for n in range(len(networks)):
        out.write("INSERT INTO network VALUES(%d, \"%s\");\n" % (n+1, networks[n].encode('latin-1')))

    cities = set()
    stations = set()
    lines = set()
    lines_stations = set()
    for src in sources:
        try:
            busline, directions, linecolor, dfltCirculationPolicy, from_date, to_date = parse(src)
            lines.add((busline, directions[0][-1]['city'], directions[1][-1]['city'], 
                linecolor, dfltCirculationPolicy, from_date, to_date))
            k = 0
            for direct in directions:
                rank = 1
                for data in direct:
                    cities.add(data['city'])
                    stations.add((data['station'], data['city']))
                    lines_stations.add((busline, data['station'], rank, directions[k][-1]['city'], data['city']))
                    rank += 1
                k += 1
        except Exception, e:
            print
            print "ERROR: processing line %s" % busline
            raise

    # Build cities IDs
    pk = 1
    cs = []
    for city in cities:
        cs.append((pk, unicode(city).encode('utf-8')))
        pk += 1

    for city in cs:
        lat, lng = get_gps_coords_from_cache(city[1], GPS_CACHE_FILE)
        out.write("INSERT INTO city VALUES(%d, \"%s\", %d, %d);\n" % (city[0], city[1], lat*10**6, lng*10**6))
        db_city_count += 1
        g_cities.append(city[1])

    pk = 1
    pk_city = 0
    pk_stations = {}
    for st in stations:
        for city in cs:
            if city[1] == st[1].encode('utf-8'):
                pk_city = city[0]
                break
        if pk_city == 0:
            print "Error: city id not found!"
            sys.exit(1)
        out.write("INSERT INTO station VALUES(%d, \"%s\", 0, 0, %d);\n" % (pk, st[0].encode('utf-8'), pk_city))
        pk_stations[(st[0].encode('utf-8'), pk_city)] = pk
        db_station_count += 1
        pk += 1

    pk_from = pk_to = 0
    pk = 1
    for line in lines:
        for city in cs:
            if city[1] == line[1].encode('utf-8'):
                pk_from = city[0]
                break
        for city in cs:
            if city[1] == line[2].encode('utf-8'):
                pk_to = city[0]
                break
        if pk_from == 0 or pk_to == 0:
            print "Error: pk_from(%d) or pk_to(%d) id not found!" % (pk_from, pk_to)
            print "Line: " + str(line)
            sys.exit(1)
        # FIXME: network_id is hardcoded (as 1) and only refers to the Herault Transport
        # network for the moment.
        out.write("INSERT INTO line VALUES(%d, %d, \"%s\", \"%s\", \"%s\", %d, %d, \"%s\", \"%s\");\n" % (
            pk, 1, line[0], line[3], line[4], pk_from, pk_to, line[5], line[6]))
        db_line_count += 1
        pk += 1

    pk_line = pk_station = pk_direction = pk_city = 0
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
            if city[1] == ls[3].encode('utf-8'):
                pk_direction = city[0]
                break
        if pk_direction == 0:
            print "Error: pk_direction is 0!"
            sys.exit(1)
        for city in cs:
            if city[1] == ls[4].encode('utf-8'):
                pk_city = city[0]
                break
        if pk_city == 0:
            print "Error: pk_city is 0!"
            sys.exit(1)
        out.write("INSERT INTO line_station VALUES(%d, %d, %d, %d, %d);\n" % (
            pk, pk_line, pk_stations[(ls[1].encode('utf-8'), pk_city)], ls[2], pk_direction))
        pk += 1

    # Handle stops
    k = 1
    for src in sources:
        busline, directions, linecolor, dfltCirculationPolicy, from_date, to_date = parse(src)
        for direct in directions:
            for data in direct:
                for stop in data['stops']:
                    # City id
                    city_id = 0
                    for c in cs:
                        if data['city'].encode('utf-8') == c[1]:
                            city_id = c[0]
                            break
                    if city_id == 0:
                        print "Error: city_id is 0!"
                    # Station id
                    s_id = pk_stations[data['station'].encode('utf-8'), city_id]
                    if type(stop) == types.TupleType:
                        st, pat = stop[0], stop[1]
                    else:
                        st, pat = stop, ''
                    # Direction id
                    direction_id = 0
                    for c in cs:
                        if direct[-1]['city'].encode('utf-8') == c[1]:
                            direction_id = c[0]
                            break
                    if direction_id == 0:
                        print "Error: direction_id is 0!"
                    # Line id
                    line_id = 0
                    j = 1
                    for line in lines:
                        if busline == line[0]:
                            line_id = j
                            break
                        j += 1
                    if line_id == 0:
                        print "Error: line_id is 0!"

                    out.write("INSERT INTO stop VALUES(%d, \"%s\", \"%s\", %d, %d, %d, %d);\n" % 
                        (k, st, pat, s_id, line_id, direction_id, city_id))
                    k += 1

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
    COLOR_PAT = 'color='
    UPDATED_PAT = 'updated=' # Date of last line update

    k = -1
    curCity = None
    curLines = None
    linecolor = ""
    from_date = to_date = ""
    for line in data:
        if line.startswith(DIRECTION_PAT):
            directions.append([])
            k += 1
        elif line.startswith(CIRCULATION_PAT):
            dfltCirculationPolicy = re.sub(CIRCULATION_PAT, '', line).encode('utf-8')
        elif line.startswith(CITY_PAT):
            curCity = re.sub(CITY_PAT, '', line)
        elif line.startswith(BUSLINE_PAT):
            busline = re.sub(BUSLINE_PAT, '', line).encode('utf-8')
        elif line.startswith(FROM_PAT):
            from_date = re.sub(FROM_PAT, '', line).encode('utf-8')
        elif line.startswith(TO_PAT):
            to_date = re.sub(TO_PAT, '', line).encode('utf-8')
        elif line.startswith(COLOR_PAT):
            linecolor = re.sub(COLOR_PAT, '', line).encode('utf-8')
        elif line.startswith(UPDATED_PAT):
            # FIXME
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
                        # Forces to have a HH:MM time format, not H:MM as defined in 
                        # most raw files because it can break SQL queries in the 
                        # Android app...
                        if len(stop) == 4:
                            stop = '0' + stop
                        if nextPolicy == dfltCirculationPolicy:
                            allstops.append(stop)
                        else:
                            allstops.append((stop, nextPolicy))
                    elif re.match(STOP_CIRC_PAT, stop):
                        m = re.match(STOP_CIRC_PAT, stop)
                        allstops.append((m.group(1), m.group(2)))

            # Split all station names with one or more '/' as a unique station name
            for stname in map(lambda x: x.strip(), sts[0].split('/')):
                directions[k].append({
                    'city': smart_capitalize(curCity),
                    'station': smart_capitalize(stname),
                    'stops': allstops,
                })

    return (busline, directions, linecolor, dfltCirculationPolicy, from_date, to_date)

def smart_capitalize(name):
    """
    Try to apply a simple smart capitilazitation algorithm.
    """
    specs = ("L'", "D'",)
    # str.title() adds uppercase caracters on words
    cname = name.strip().title()
    # Check for all '-' in name to put some capitals where needed
    # i.e: saint jean-de-védas -> Saint-Jean-de-Védas
    starts = [m.start() for m in re.finditer('-', cname)]
    clname = list(cname)
    idx = 0
    if len(starts) > 1:
        if len(starts) == 2: # 2 '-' in name
            if len(clname[starts[0]+1:starts[1]-1]) < 3:
                idx = starts[0]+1
                clname[idx] = clname[idx].lower()
        elif len(starts) == 3:
            idx = starts[1]+1
            clname[idx] = clname[idx].lower()
        elif len(starts) == 4:
            idx = starts[1]+1
            clname[idx] = clname[idx].lower()
            idx = starts[2]+1
            clname[idx] = clname[idx].lower()

    # Always use lowercase for l' and d'
    for k in starts:
        if ''.join(map(lambda x: x.upper(), clname[k+1:k+3])) in specs:
            clname[k+1] = clname[k+1].lower()

    return ''.join(clname)

def get_md5(filename):
    ck = open(filename)
    m = hashlib.md5()
    while True:
        data = ck.read(128)
        if not data:
            break
        m.update(data)
    ck.close()
    return m.hexdigest()

def compute_db_checksum(srcdir):
    """
    Checksum of the database is performed using a global checksum of 
    all the raw/*.txt file, the DBSTRUCT content and the filter.map 
    entries. 
    
    It's supposed to be a portable solution between different Python 
    versions. Indeed, different Python versions could handle ordering 
    of elements in Set() in different ways, thus making issues when 
    creating cheksums against the generated .sql or .xml file. It appears 
    to be better to operate directly on source files.

    Furthermore, the computed checksum is used to check if database 
    rebuilding is needed by the building system.
    """
    sources = glob.glob(os.path.join(srcdir, '*.txt'))
    sources.sort()
    final = hashlib.md5()
    final.update(DBSTRUCT)
    final.update(get_md5(os.path.join(sys.path[0], 'filter.map')))
    for src in sources:
        final.update(get_md5(src))
    return final.hexdigest()

def make_chunks(rawname, chunksize=0):
    """
    Only one chunk if chunksize is null.
    Returns the number of chunks created.
    """
    size = os.path.getsize(rawname)
    chunk = 1

    outname = os.path.join(TMP_DIR, "%s_%d.xml" % (CHUNK_PREFIX, chunk))
    print "[%-18s] new chunk file %s..." % ("chunk %02d" % chunk, outname),
    out = open(outname, 'w')
    out.write(XML_HEADER)
    seek = 0
    out.write("""
<string name="ht_createdb">
""")
    for line in open(rawname): 
        if line.startswith('BEGIN TRANSACTION;') or line.startswith('END TRANSACTION;') or line.startswith('END;'):
            continue
        # Order matters
        for pat, sub in (   (r'--.*$', ''), (r'$', ' '), 
                            (r'IS NULL;', 'IS NULL## END;'), (r'^[ \t]*', ''), 
                            (r'\n', ''), (r';', '\n'), (r'##', ';') ):
            line = re.sub(pat, sub, line)
        out.write(line)
        seek += len(line)
        if chunksize > 0 and seek > chunksize:
            seek = 0
            out.write("""
</string>
""")
            out.close()
            print "done."

            # New chunk
            chunk += 1
            outname = os.path.join(TMP_DIR, "%s_%d.xml" % (CHUNK_PREFIX, chunk))
            print "[%-18s] new chunk file %s..." % ("chunk %02d" % chunk, outname),
            out = open(outname, 'w')
            out.write(XML_HEADER)
            out.write("""
<string name="ht_createdb">
""")

    # Closing latest chunk
    out.write("""
</string>
""")
    out.close()
    print "done."
    return chunk

def check_up_to_date(chksum):
    """
    Are the source files in sync with the current SQL and DB?
    Exits gracefully if nothing to do at all.
    """
    # Write checksum for later comparison
    if not os.path.exists(os.path.join(TMP_DIR, CHKSUM_FILE)):
        f = open(os.path.join(TMP_DIR, CHKSUM_FILE), 'w')
        f.write(chksum)
        f.close()
    else:
        # Nothing to do?
        f = open(os.path.join(TMP_DIR, CHKSUM_FILE))
        if f.read() == chksum:
           print "Nothing to do. Exiting."
           f.close()
           sys.exit(0)
        else:
            p = open(os.path.join(TMP_DIR, CHKSUM_FILE), 'w')
            p.write(chksum)
            p.close()
        f.close()

def init_networks(srcdir):
    """
    Find available bus networks. networks.json is a static file edited
    by hand.
    Returns a dictionnary of available networks.
    """
    if not os.path.isfile(NETWORKS_FILE):
        raise ValueError, "networks.json not found in src directory."

    with open(NETWORKS_FILE) as f:
        nets = '\n'.join(f.readlines())
        nets = json.loads(nets)
        f.close()

    for net in nets.keys():
        nets[net]["lines"] = [] 

    for k, v in nets.iteritems():
        lines = glob.glob(os.path.join(srcdir, v['path'], '*.in'))
        if not lines:
            print "[%s] Warning: missing .in line definitions" % k
        else:
            nets[k]["lines"].extend(map(lambda x: os.path.basename(x), lines))
            print "[%s] Found %d lines" % (k, len(lines))

    return nets

def htc_compile(srcdir):
    """
    Runs the htc compiler on *.in bus lines definitions.
    """
    # Find available networks
    networks = init_networks(srcdir)
    # Compile every lines
    for net, data in networks.iteritems():
        print "[%s] Compiling" % net,
        for line in data["lines"]:
            print line,
            sys.stdout.flush()
            cmd = "./htc %s > raw/%s" % (os.path.join(srcdir, data["path"], line), 
                re.sub('\.in$', '.txt', line))
            subprocess.call(cmd, shell=True)
        print

def main():
    global DEBUG
    global g_prefilter, GPS_CACHE_FILE, DBSTRUCT

    parser = OptionParser(usage="""
%prog [--android|-d|-g|--gps|--gps-cache file] action (raw_line.txt|dir)

where action is one of:
  xml     generates XML content from sources
  psql    generates SQL content for PostgreSQL
  sqlite  generates SQL content for SQLite
  mysql   generates SQL content for MySQL
  """)
    parser.add_option("", '--android', action="store_true", dest="android", default=False, help='SQL resource formatting for Android [action: sql]')
    parser.add_option("", '--use-chunks', action="store_true", dest="chunks", default=False, help='Split data in several chunks [action: sql]')
    parser.add_option("", '--db-compare-with', action="store", dest="dbcompare", default=False, help="compares current database checksum with an external XML file [action: sql]")
    parser.add_option("", '--pre-filter', action="store", dest="prefilter", default=None, help="applies a filter mapping on all raw input (useful to substitute content)")
    parser.add_option("", '--chunk-size', type="int", action="store", dest="chunksize", default=CHUNK_SIZE, help="set chunk size in kB [default: %d, action: sql]" % CHUNK_SIZE)
    parser.add_option("-d", action="store_true", dest="debug", default=False, help='more debugging')
    parser.add_option("-v", '--verbose', action="store_true", dest="verbose", default=False, help='verbose output')
    parser.add_option("-g", action="store_true", dest="globalxml", default=False, help='generates global lines.xml [action: sql]')
    parser.add_option("", '--gps', action="store_true", dest="getgps", default=False, help='retreives cities GPS coordinates')
    parser.add_option("", '--gps-cache', action="store", dest="gpscache", default=GPS_CACHE_FILE, 
        help="use gps cache file [default: %s]" % GPS_CACHE_FILE)
    options, args = parser.parse_args()

    if len(args) != 2:
        parser.print_usage()
        sys.exit(2)

    DEBUG = options.debug
    action, infile = args
    action = action.lower()
    if action not in ('xml', 'sqlite', 'mysql'):
        parser.error("Unsupported action '%s'." % action)

    if options.globalxml and action == 'sqlite':
        parser.error("-g and sql action are mutually exclusive!")

    if options.android and action == 'xml':
        parser.error("--android and xml action are mutually exclusive!")

    if options.chunks and action == 'xml':
        parser.error("--use-chunks and xml action are mutually exclusive!")

    if options.prefilter and action == 'xml':
        parser.error("--city-post-map and xml action are mutually exclusive!")

    if options.chunks and not options.android:
        parser.error("--use-chunks requires the --android option!")

    if options.dbcompare and not options.android:
        parser.error("--db-compare-with requires the --android option!")

    g_prefilter = options.prefilter
    GPS_CACHE_FILE = options.gpscache

    if action == 'sqlite':
        DBSTRUCT = sqlitedb.DBSTRUCT
    elif action == 'mysql':
        DBSTRUCT = mysqldb.DBSTRUCT

    if os.path.isdir(infile):
        # Applies pre-filter before parsing any raw content
        prefilter_data = {}
        prefilter_matches = 0
        chksum = compute_db_checksum(infile)
        check_up_to_date(chksum)
        # Run the compiler to convert .in to .txt files
        # FIXME: replace 'src' with infile (move raw/ away)
        htc_compile('src')
        sys.exit(2)

        if options.prefilter:
            if not os.path.exists(g_prefilter):
                raise ValueError, "pre filter not a file"
            filter_dir = os.path.join(TMP_DIR, 'pre-filter')
            # Clean up target
            if not os.path.exists(filter_dir):
                os.mkdir(filter_dir)
            shutil.rmtree(filter_dir)
            shutil.copytree(infile, filter_dir)
            infile = filter_dir
            # TODO: check map format (old_name=new_name)
            import string
            subs = 0
            print "[%-18s] applying filter %s..." % ('pre-filter', g_prefilter),
            sys.stdout.flush()
            for pmap in open(g_prefilter):
                if pmap.strip().startswith('#') or len(pmap.strip()) == 0:
                    continue
                # Old entry, new entry
                oe, ne = pmap.split(';')
                ne = ne.replace('\n', '')
                cmd = "sed -i \"s,%s,%s,gI\" %s" % (oe, ne, os.path.join(filter_dir, '*.txt'))
                subprocess.call(cmd, shell=True)
                subs += 1
            print " %d entries" % subs

        sources = glob.glob(os.path.join(infile, '*.txt'))
        sources.sort()
        if action == 'sqlite':
            # Grouping all INSERTs in a single transaction really 
            # speeds up the whole thing
            outname = os.path.join(TMP_DIR, RAW_DB_FILE)
            print "[%-18s] raw SQL content (for SQLite)..." % outname,
            sys.stdout.flush()
            out = open(outname, 'w')
            out.write("BEGIN TRANSACTION;\n")
            out.write(DBSTRUCT)
            makeSQL(sources, out)
            out.write("END TRANSACTION;\n")
            out.close()
            print "done."

            if options.android:
                rawname = os.path.join(TMP_DIR, RAW_DB_FILE)
                print "[%-18s] XML DB resource for Android..." % 'chunks'
                sys.stdout.flush()
                # Only one chunk
                num_chunks = make_chunks(rawname, options.chunksize)
                print "[%-18s] done, wrote %d chunk(s)" % ('chunks', num_chunks)

                # Writing DB stats file resource
                statsname = os.path.join(TMP_DIR, DB_STATS_FILE)
                print "[%-18s] making DB stats file..." % statsname,
                sys.stdout.flush()
                out = open(statsname, 'w')
                out.write(XML_HEADER)
                out.write("""
<resources>
  <string name="num_lines">%d</string>
  <string name="num_cities">%d</string>
  <string name="num_stations">%d</string>
</resources>
""" % (db_line_count, db_city_count, db_station_count))
                out.close()
                print "done."

                # Writing checksum and version file
                chkname = os.path.join(TMP_DIR, CHKSUM_DB_FILE)
                out = open(chkname, 'w')
                out.write(XML_HEADER)
                print "[%-18s] making checksum file..." % chkname,
                sys.stdout.flush()
                if options.prefilter:
                    infile = filter_dir
                out.write("""
<resources>
  <string name="dbchecksum">%s</string>
  <string name="numchunks">%d</string>
</resources>
""" % (chksum, num_chunks))
                out.close()
                print "done."

                # Check database version against an external XML file?
                if options.dbcompare:
                    if not os.path.exists(options.dbcompare):
                        print "[%-18s] external XML file not found, copying current checksum file..." % 'dbcompare',
                        sys.stdout.flush()
                        out = open(options.dbcompare, 'w')
                        out.write(XML_HEADER)
                        out.write("""
<resources>
  <string name="numchunks">%d</string>
  <string name="dbchecksum">%s</string>
  <string name="dbversion">1</string>
</resources>
""" % (num_chunks, chksum))
                        out.close()
                        print "done."
                    else:
                        print "[%-18s] found external XML file, checking DB version..." % 'dbcompare'
                        old_chksum = old_version = None
                        for line in open(options.dbcompare):
                            m = re.search(r'"dbchecksum">(.*?)</string>', line)
                            if m:
                                old_chksum = m.group(1)
                            m = re.search(r'"dbversion">(.*?)</string>', line)
                            if m:
                                old_version = m.group(1)
                        if old_version == None:
                            print "Error: dbversion is None"
                            sys.exit(1)
                        if old_chksum == None:
                            print "Error: dbchecksum is None"
                            sys.exit(1)

                        if chksum != old_chksum:
                            print "[%-18s] database changed, incrementing version..." % 'UPGRADE',
                            new_version = int(old_version) + 1
                            sys.stdout.flush()
                            out = open(options.dbcompare, 'w')
                            out.write(XML_HEADER)
                            out.write("""
<resources>
  <string name="numchunks">%d</string>
  <string name="dbchecksum">%s</string>
  <string name="dbversion">%d</string>
</resources>
""" % (num_chunks, chksum, new_version))
                            out.close()
                            print "to v%d. Done." % new_version
                        else:
                            print "[%-18s] database NOT updated" % 'IDEM'
                        print "[%-18s] done." % 'dbcompare'
        elif action == 'mysql':
            outname = os.path.join(TMP_DIR, RAW_DB_FILE)
            print "[%-18s] raw SQL content (for MySQL)..." % outname,
            sys.stdout.flush()
            out = open(outname, 'w')
            out.write("SET autocommit=0;\nBEGIN;\n")
            out.write(DBSTRUCT)
            makeSQL(sources, out)
            out.write("COMMIT;\n")
            out.write("SET autocommit=1;\n")
            out.close()
            print "done."
        elif action == 'xml':
            # Action is 'xml'
            for src in sources:
                busline, directions, linecolor, dfltCirculationPolicy, from_date, to_date = parse(src)
                ext = '.xml'
                outfile = os.path.join(TMP_DIR, os.path.basename(src[:src.rfind('.')] + ext))
                makeXML(busline, directions, outfile, linecolor)

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
                    # FIXME
                    if src.find('db.xml') > 0 or src.find('gps.xml') > 0:
                        continue
                    s = open(src)
                    f.write(''.join(s.readlines()[2:]) + '\n')
                    f.flush()
                    s.close()
                f.flush()
                f.write('</lines>\n')
                f.close()
                print "Generated global %s" % os.path.join(TMP_DIR, 'lines.xml')
    else:
        # File target
        if action == 'sqlite':
            print "Error: does not support one file, only full parent directory"
            sys.exit(2)
        else:
            # xml
            outfile = infile[:infile.rfind('.')] + '.xml'
            busline, directions, linecolor, dfltCirculationPolicy, from_date, to_date = parse(infile)
            makeXML(busline, directions, outfile, linecolor)

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
                # City already in the cache
                lat, lng = get_gps_coords_from_cache(city, options.gpscache)
                if lat == 0 and lng == 0:
                    print "Warning: city %s has (0, 0) GPS coordinates!" % city
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

