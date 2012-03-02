#!/bin/sh


test -z "$1" && exit 8

OFILE=res/values/gmaps.xml
KEY=$1

cat > $OFILE << EOF
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="gmaps_api_key">$KEY</string>
</resources>
EOF

