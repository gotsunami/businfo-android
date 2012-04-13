
SQLDB=/tmp/htdb.sql
DBOPTS=--gps --gps-cache=gps.csv --pre-filter=filter.map
MAKERES=./makeres.py
SQLITEDB=~/ht.sqlite
SQLITE=sqlite3
CHKSUM=/tmp/.checksum

.PHONY: makedb clean test sqlite

all: sqlite

makedb:
	@echo "Building SQL database..."
	@${MAKERES} ${DBOPTS} sqlite raw/

test:
	@echo "Running sanity checks..."

sqlite: makedb
	@echo "Making SQLite database..."
	@rm -f ${SQLITEDB} && ${SQLITE} ${SQLITEDB} < ${SQLDB}
	@echo "Wrote in ${SQLITEDB}"

clean:
	rm -f ${SQLDB} ${SQLITEDB} ${CHKSUM}

