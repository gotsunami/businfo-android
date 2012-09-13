#!/usr/bin/env python
# -*- coding: latin-1 -*-

import re
import sys
import types

SEP = ' '
DAYS = ('L', 'Ma', 'Me', 'J', 'V', 'S', 'Sa', 'Di', 'F')
DAYS_S = ('à', '/')
DUPS = {'S': 'Sa', 'Sa': 'S', 'D': 'Di', 'Di': 'D'}
HTMAP = {
    'L' : '1',
    'Ma': '2',
    'Me': '3',
    'J' : '4',
    'V' : '5',
    'S' : '6',
    'Sa': '6',
    'Di': '7',
    'F' : 'r', # rest days
    'à' : '-',
    '/' : ',',
}
FEATMAP = {'0': '', 'NSCO': 'S', 'SCO': 's'}
#
cities = []
cur_city = None

def debug(msg):
    print "=>", msg

def error(msg):
    print "Error:", msg
    sys.exit(1)

def get_days(d):
    """
    d in the list of days
    """
    days = []
    k = 0

    while k < len(d):
        if k < len(d)-1:
            if d[k+1] in DAYS_S:
                days.append(d[k:k+3])
                k += 3
                continue
            elif d not in DAYS_S:
                days.append(d[k])
        else:
            days.append(d[k])
        k += 1

    k = 0
    for d in days:
        if type(d) == types.StringType:
            for sep in DAYS_S:
                if d.find(sep) > 0:
                    z = d.split(sep)
                    z.insert(1, sep)
                    days[k] = z
        k += 1

    # Remaining strings without special chars
    k = 0
    for d in days:
        if type(d) == types.StringType:
            tmp = []
            for c in DAYS:
                if d.find(c) != -1:
                    # Special case: check for any day synonyms
                    if DUPS.has_key(c):
                        if DUPS[c] not in tmp:
                            tmp.append(c)
                            tmp.append('/')
                    else:
                        tmp.append(c)
                        tmp.append('/')
            days[k] = tmp[:-1] # remove trailing /
        k += 1

    i = 0
    for d in days:
        tmp = ''
        for k in d:
            tmp += HTMAP[k]
        days[i] = tmp
        i += 1
    return days

def get_features(p):
    return [FEATMAP[re.sub(r'\[.\]', '', k)] for k in p]

def print_scheds(scheds):
    print '\n'.join(scheds).replace(' ', ';')

def parse_cities_stations(line):
    """
    Find city name and related stations by reading 
    lines of text
    """
    global cur_city, cities

    line = line.strip().split(' ')
    city_s = [] # will hold the full city name
    for ent in line:
        tmp = re.sub(r"[-*'.’]", '', ent)
        if all([n.isupper() for n in tmp]):
            city_s.append(ent)
            if len(line) == 1:
                # only city name on line
                cur_city = ' '.join(city_s)
                cur_city = re.sub(r'[*]', '', cur_city)
                cities.append([cur_city, []])
        else:
            if len(city_s) > 0:
                # just got a city name on the line
                cur_city = ' '.join(city_s)
                cur_city = re.sub(r'[*]', '', cur_city)
                cities.append([cur_city, []])
                break

    if len(line) > 1 and ' '.join(city_s) == ' '.join(line):
        # Special case, only city name on line (may be several words)
        cur_city = ' '.join(city_s)
        cur_city = re.sub(r'[*]', '', cur_city)
        cities.append([cur_city, []])
        return

    if len(city_s) > 0:
        if len(line) > 1:
            cities[-1][1].append(' '.join(line[len(city_s):]))
    else:
        # just a station name on the line
        cities[-1][1].append(' '.join(line))

def handle_direction(data):
    """
    Handle one direction at a time
    """
    scheds = []
    max_sched_width = 0
    city_block = False

    for line in data:
        if len(line) == 0:
            continue
        # schedules
        m = re.match(r'.*\d{2}:\d{2}.*', line)
        if m:
            # Should be a schedule line
            city_block = False
            # Clean up the line
            bkts = re.findall(r'\[.\]', line)
            for b in bkts:
                line = line.replace(b, '')
            scheds.append(line)
            line_size = len(line.split(SEP))
            if line_size > max_sched_width:
                max_sched_width = line_size
        elif line.find('days=') == 0:
            # Parsing days line
            city_block = False
            d = line[5:].split(' ')
            days = get_days(d)
        elif line.find('p=') == 0:
            # Parsing 
            city_block = False
            features = []
            if len(line) != 2:
                features = get_features(line[2:].split(' '))
        elif line.find('c=') == 0:
            # Parsing cities and stations
            city_block = True
            parse_cities_stations(line[2:]) # removes c=

        elif city_block:
            parse_cities_stations(line)


    j = 0
    for scline in scheds:
        scline = scline.split(' ')
        k = 0
        if len(days) < len(scline):
            print "Error: less days than schedules!"
            print "days=", len(days), days
            print "scheds (line %d)=" % (j+1), len(scline), scline
            error("%d < %d" % (len(days), len(scline)))
        for sc in scline:
            if sc != '-':
                scline[k] = sc + '*' + days[k]
                if k < len(features):
                    scline[k] += features[k]
                scline[k] += '*'
            k += 1
            scheds[j] = ' '.join(scline)
        j += 1

    print "\ndirection="
    st = 0
    for d in cities:
        print "\ncity=%s" % d[0]
        for station in d[1]:
            print "%s;%s" % (station, scheds[st].replace(' ', ';'))
            st += 1

    if max_sched_width > len(days):
        error("more schedules entries (%d) than days definition (%d)!" % (max_sched_width, len(days)))
            

def main():
    global cities, cur_city
    if len(sys.argv) != 2:
        print "Missing bus line argument"
        print "Usage: %s line.in" % sys.argv[0]
        sys.exit(2)

    f = open(sys.argv[1])
    data = f.readlines()
    f.close()

    # break content into 2 directions
    directions = []
    dir1 = dir2 = False
    for line in data:
        line = line[:-1].strip()
        if len(line) == 0 or line.find("#") == 0:
            continue
        if line.find("direction=") == 0:
            if dir1:
                dir1 = False
                dir2 = True
            else:
                dir1 = True
            directions.append(list())
        else:
            if dir1 or dir2:
                directions[-1].append(line)

    if len(directions) != 2:
        error("Missing direction: only have %d (should have 2)!" % len(directions))

    # Parsing header
    header = []
    kws = ['name', 'circulation', 'color', 'from', 'to']
    kws = map(lambda x: x+'=', kws)
    for line in data:
        line = line[:-1].strip()
        if line.find("direction=") == 0:
            break
        for kw in kws:
            if line.find(kw) == 0:
                header.append(line)
                break

    header.insert(0, "# Generated by the htc compiler. DO NOT EDIT!")
    header.insert(1, "# Compile: htc %s\n" % sys.argv[1])
    print '\n'.join(header)

    k = 1
    need_manual = False
    for d in directions:
        # Data direction can be handled separately
        cities = []
        curc_city = None
        handle_direction(d)
        # Looks for city duplicates
        dup = {}
        for cit in cities:
            if not dup.has_key(cit[0]):
                dup[cit[0]] = 1
            else:
                dup[cit[0]] += 1
                print >> sys.stderr, "Warning: find duplicates in direction %d for city %s" % (k, cit[0])
                need_manual = True
        k += 1

    if need_manual:
        print >> sys.stderr, "Warning: manual tuning of the result is required!"

if __name__ == '__main__':
    main()

