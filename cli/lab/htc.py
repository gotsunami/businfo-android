#!/usr/bin/env python
# -*- coding: latin-1 -*-

import re
import sys
import types

SEP = ' '
DAYS = ('L', 'Ma', 'Me', 'J', 'V', 'Sa', 'Di')
DAYS_S = ('à', '/')
HTMAP = {
    'L' : '1',
    'Ma': '2',
    'Me': '3',
    'J' : '4',
    'V' : '5',
    'S' : '6',
    'Sa': '6',
    'Di': '7',
    'à' : '-',
    '/' : ',',
}
#
cities = {}
cur_city = None
cur_city_idx = 0

def debug(msg):
    print "=>", msg

def error(msg):
    print "Error:", msg
    sys.exit(1)

def get_days(d):
    days = []
    k = 0

    while k < len(d)-1:
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

def print_scheds(scheds):
    print '\n'.join(scheds).replace(' ', ';')

def parse_cities_stations(line):
    """
    Find city name and related stations by reading 
    lines of text
    """
    global cur_city, cities, cur_city_idx

    line = line.strip().split(' ')
    city_s = [] # will hold the full city name
    for ent in line:
        tmp = re.sub(r'[-*]', '', ent)
        if all([n.isupper() for n in tmp]):
            city_s.append(ent)
            if len(line) == 1:
                # only city name on line
                cur_city = ' '.join(city_s)
                cur_city = re.sub(r'[*]', '', cur_city)
                if not cities.has_key(cur_city):
                    cities[cur_city] = [cur_city_idx]
                    cur_city_idx += 1
        else:
            if len(city_s) > 0:
                # just got a city name on the line
                cur_city = ' '.join(city_s)
                cur_city = re.sub(r'[*]', '', cur_city)
                if not cities.has_key(cur_city):
                    cities[cur_city] = [cur_city_idx]
                    cur_city_idx += 1
                break

    if len(city_s) > 0:
        if len(line) > 1:
            cities[cur_city].append(' '.join(line[len(city_s):]))
    else:
        # just a station name on the line
        cities[cur_city].append(' '.join(line))
            

def main():
    f = open(sys.argv[1])
    data = f.readlines()
    f.close()

    scheds = []
    #cities = {}
    max_sched_width = 0
    city_block = False
    #cur_city = None

    for line in data:
        line = line[:-1].strip()
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
            if len(line.split(SEP)) > max_sched_width:
                max_sched_width = len(scheds)
        elif line.find('days=') == 0:
            # Parsing days line
            city_block = False
            d = line[5:].split(' ')
            days = get_days(d)
        elif line.find('p=') == 0:
            # Parsing 
            city_block = False
            pass
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
        for sc in scline:
            if sc != '-':
                scline[k] = sc + '*' + days[k] + '*'
            k += 1
            scheds[j] = ' '.join(scline)
        j += 1

#    print_scheds(scheds)

    import pprint
    pp = pprint.PrettyPrinter()

    # convert to a flat-ordererd list
    lcities = []
    for x in range(len(cities.values())):
        for k, v in cities.iteritems():
            if cities[k][0] == x:
                lcities.append([k, cities[k][1:]])

#    pp.pprint(lcities)

    print "\ndirection="
    st = 0
    for d in lcities:
        print "\ncity=%s" % d[0]
        for station in d[1]:
            print "%s;%s" % (station, scheds[st].replace(' ', ';'))
            st += 1


    if max_sched_width > len(days):
        error("more schedules entries (%d) than days definition (%d)!" % (max_sched_width, len(days)))

if __name__ == '__main__':
    main()

