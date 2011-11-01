#!/bin/sh

OFILE=res/values/revision.xml

echo "<?xml version=\"1.0\" encoding=\"utf-8\"?>" > $OFILE
echo "<resources>" >> $OFILE
echo "<string name=\"app_revision\">`hg tip --template '{rev}:{node|short}'`@`hostname -s`</string>" >> $OFILE
echo "</resources>" >> $OFILE
