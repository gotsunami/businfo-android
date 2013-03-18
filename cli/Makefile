
SQLDB=/tmp/htdb.sql
DBOPTS=--gps --gps-cache=gps.csv --pre-filter=filter.map
MAKERES=./makeres.py
SQLITEDB=~/ht.sqlite
SQLITE=sqlite3
CHKSUM=/tmp/.checksum
MYSQL=mysql
MYSQLOPTS=-u ht --password=ht
MYSQLDB=ht
LINECOMPILERSRC=htc.go

.PHONY: makedb clean test sqlite mysql mysqldb

all: sqlite

makedb: htc
	@echo "Generating raw SQLite content..."
	@${MAKERES} ${DBOPTS} sqlite raw/

mysqldb: htc
	@echo "Generating raw MySQL content..."
	@${MAKERES} ${DBOPTS} mysql raw/

htc:
	@go build ${LINECOMPILERSRC}

test:
	@echo "Running sanity checks..."

sqlite: makedb
	@echo "Making SQLite database..."
	@rm -f ${SQLITEDB} && ${SQLITE} ${SQLITEDB} < ${SQLDB}
	@echo "Wrote in ${SQLITEDB}"

mysql: mysqldb
	@echo "Making MySQL database..."
	@echo "Deleting old database..."
	@${MYSQL} ${MYSQLOPTS} -e "DROP DATABASE IF EXISTS ${MYSQLDB}"
	@echo "Creating new one..."
	@${MYSQL} ${MYSQLOPTS} -e "CREATE DATABASE ${MYSQLDB}"
	@${MYSQL} ${MYSQLOPTS} ${MYSQLDB} < ${SQLDB}
	@echo "Done"

clean:
	rm -f ${SQLDB} ${SQLITEDB} ${CHKSUM}

