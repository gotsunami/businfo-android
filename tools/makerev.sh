#!/bin/sh

OFILE=res/values/revision.xml

echo "<?xml version=\"1.0\" encoding=\"utf-8\"?>" > $OFILE
echo "<resources>" >> $OFILE
echo "<string name=\"app_revision\">`git rev-parse HEAD| cut -b 1-7`@`hostname -s`</string>" >> $OFILE
echo "</resources>" >> $OFILE
