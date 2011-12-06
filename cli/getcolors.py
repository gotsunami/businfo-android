#!/usr/bin/env python

import urllib2, re
PAGE = 'http://www.herault-transport.fr/horaires_tarifs_hiver.html'

def make_resource(lines):
    """
    Build an Android resource files for line colors
    """
    print """<?xml version="1.0" encoding="utf-8"?>
<!-- GENERATED AUTOMATICALLY BY THE getcolors.py SCRIPT. DO NOT MODIFY! -->
<linecolors>"""
    for line, color in lines.iteritems():
        print """  <line name="%s" color="%s" />""" % (line, color)
    print "</linecolors>"

def main():
    f = urllib2.urlopen(PAGE)
    data = f.read()
    m = re.findall(r'<tr>.*?<td.*?bgcolor=.*?>.*?<strong>.*?</strong>', data, re.I|re.S)
    lines = {}
    for res in m:
        t = re.findall(r'<td.*?bgcolor=.*?>', res)
        color = re.search(r'#[0-9a-fA-F]{6}', t[0]).group(0)
        line = re.search(r'<strong>(.*?)</strong>', res).group(1)
        line = re.sub('<a.*>', '', line)
        line = re.sub('</a>', '', line)
        line = line.strip()
        color = color.strip()
        lines[line] = color
    del lines['']

    make_resource(lines)

if __name__ == '__main__':
    main()
