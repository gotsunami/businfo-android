#!/usr/bin/env python
# -*- coding: latin-1 -*-

"""
Database structure for MySQL engine
"""

DBSTRUCT = """
SET autocommit=0;
DROP TABLE IF EXISTS city;
CREATE TABLE city (
    id INTEGER PRIMARY KEY AUTO_INCREMENT, 
    name VARCHAR(255),
    latitude INTEGER,
    longitude INTEGER,
    UNIQUE(name)
--    UNIQUE(latitude, longitude)
) ENGINE=INNODB;

DROP TABLE IF EXISTS line;
CREATE TABLE line (
    id INTEGER NOT NULL, 
    name VARCHAR(255), 
    color VARCHAR(255), 
    dflt_circpat VARCHAR(255),
    from_city_id INTEGER NOT NULL, 
    to_city_id INTEGER NOT NULL,
    from_date DATE,
    to_date DATE,
    UNIQUE(name),
    PRIMARY KEY(id),
    FOREIGN KEY (from_city_id) REFERENCES city(id), 
    FOREIGN KEY (to_city_id) REFERENCES city(id)
) ENGINE=INNODB;

DROP TABLE IF EXISTS station;
CREATE TABLE station (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255),
    latitude REAL,
    longitude REAL,
    city_id INTEGER NOT NULL,
    UNIQUE(name, city_id),
    FOREIGN KEY (city_id) REFERENCES city(id)
--    UNIQUE(latitude, longitude)
) ENGINE=INNODB;

DROP TABLE IF EXISTS stop;
CREATE TABLE stop (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    time DATE,
    circpat VARCHAR(255),           -- circulation pattern
    station_id INTEGER NOT NULL,
    line_id INTEGER NOT NULL,
    direction_id INTEGER NOT NULL,    -- city
    city_id INTEGER NOT NULL,          -- location of station
    FOREIGN KEY (station_id) REFERENCES station(id), 
    FOREIGN KEY (line_id) REFERENCES line(id), 
    FOREIGN KEY (direction_id) REFERENCES city(id),
    FOREIGN KEY (city_id) REFERENCES city(id)
-- TODO: UNIQUE (time, circpat, station_id, line_id, direction_id, city_id)
) ENGINE=INNODB;

DROP TABLE IF EXISTS line_station;
CREATE TABLE line_station (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    line_id INTEGER NOT NULL,
    station_id INTEGER NOT NULL,
    rank INTEGER,			                -- station's rank order on line
    direction_id INTEGER NOT NULL,			        -- city id for direction
    UNIQUE(line_id, station_id, rank, direction_id),
    FOREIGN KEY (line_id) REFERENCES line(id), 
    FOREIGN KEY (station_id) REFERENCES station(id), 
    FOREIGN KEY (direction_id) REFERENCES city(id)
) ENGINE=INNODB;
"""
