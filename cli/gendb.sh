#!/bin/sh

TMP=/tmp/dbinline.xml
OUT=/tmp/db.xml

cat > $TMP << EOF
<?xml version="1.0" encoding="utf-8"?>
<!-- GENERATED AUTOMATICALLY BY THE makeres.py SCRIPT. DO NOT MODIFY! -->
<resources>
  <string name="ht_createdb">"
EOF

cli/makeres.py --sql cli/raw/| sed 's,",\\",g;/^BEGIN TRANSACTION;/d;/^END TRANSACTION;/d;s,$, ,;s,-- .*$,,;/^END;/d;s,IS NULL;,IS NULL## END;,;s/^[ \t]*//' >> $TMP # | tr -d '\n' >> $OUT

cat >> $TMP << EOF
"</string>
</resources>
EOF

cat $TMP | tr -d '\n' > $OUT
cat $OUT | sed 's,;,&\n,g;s,##,;,g' > $TMP
mv $TMP $OUT

echo "Created $OUT"
