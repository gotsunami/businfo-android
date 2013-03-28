#!/usr/bin/env python
# -*- coding: latin-1 -*-

"""
Database structure for SQLite engine
"""

DBSTRUCT = """
DROP TABLE IF EXISTS line;
CREATE TABLE line (
    id INTEGER PRIMARY KEY AUTOINCREMENT, 
    network_id INTEGER,
    name TEXT, 
    color TEXT, 
    dflt_circpat TEXT,
    from_city_id INTEGER, 
    to_city_id INTEGER,
    from_date DATETIME,
    to_date DATETIME,
    UNIQUE(name, network_id)
);

DROP TABLE IF EXISTS network;
CREATE TABLE network (
    id INTEGER PRIMARY KEY AUTOINCREMENT, 
    name TEXT, 
    color TEXT, 
    UNIQUE(name)
);

DROP TABLE IF EXISTS city;
CREATE TABLE city (
    id INTEGER PRIMARY KEY AUTOINCREMENT, 
    name TEXT,
    latitude INTEGER,
    longitude INTEGER,
    UNIQUE(name)
--    UNIQUE(latitude, longitude)
);

DROP TABLE IF EXISTS station;
CREATE TABLE station (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    latitude REAL,
    longitude REAL,
    city_id INTEGER,
    UNIQUE(name, city_id)
--    UNIQUE(latitude, longitude)
);

DROP TABLE IF EXISTS stop;
CREATE TABLE stop (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    time DATETIME,
    circpat TEXT,           -- circulation pattern
    station_id INTEGER,
    line_id INTEGER,
    direction_id INTEGER,    -- city
    city_id INTEGER          -- location of station
-- TODO: UNIQUE (time, circpat, station_id, line_id, direction_id, city_id)
);

DROP TABLE IF EXISTS line_station;
CREATE TABLE line_station (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    line_id INTEGER,
    station_id INTEGER,
    rank INTEGER,			                -- station's rank order on line
    direction_id INTEGER,			        -- city id for direction
    UNIQUE(line_id, station_id, rank, direction_id)
);

CREATE TRIGGER fki_line_network_id
BEFORE INSERT ON line
BEGIN
    SELECT RAISE(ROLLBACK, "insert on table 'line' violates foreign key constraint 'fk_network_id'")
    WHERE (SELECT id FROM network WHERE id = NEW.network_id) IS NULL;
END;

CREATE TRIGGER fki_line_from_city_id
BEFORE INSERT ON line
BEGIN
    SELECT RAISE(ROLLBACK, "insert on table 'line' violates foreign key constraint 'fk_from_city_id'")
    WHERE (SELECT id FROM city WHERE id = NEW.from_city_id) IS NULL;
END;

CREATE TRIGGER fki_line_to_city_id
BEFORE INSERT ON line
BEGIN
    SELECT RAISE(ROLLBACK, "insert on table 'line' violates foreign key constraint 'fk_to_city_id'")
    WHERE (SELECT id FROM city WHERE id = NEW.to_city_id) IS NULL;
END;

CREATE TRIGGER fki_station_city_id
BEFORE INSERT ON station
BEGIN
    SELECT RAISE(ROLLBACK, "insert on table 'station' violates foreign key constraint 'fk_city_id'")
    WHERE (SELECT id FROM city WHERE id = NEW.city_id) IS NULL;
END;

CREATE TRIGGER fki_line_station_line_id
BEFORE INSERT ON line_station
BEGIN
    SELECT RAISE(ROLLBACK, "insert on table 'line_station' violates foreign key constraint 'fk_line_id'")
    WHERE (SELECT id FROM line WHERE id = NEW.line_id) IS NULL;
END;

CREATE TRIGGER fki_line_station_station_id
BEFORE INSERT ON line_station
BEGIN
    SELECT RAISE(ROLLBACK, "insert on table 'line_station' violates foreign key constraint 'fk_station_id'")
    WHERE (SELECT id FROM station WHERE id = NEW.station_id) IS NULL;
END;

-- Must be an existing city
CREATE TRIGGER fki_line_station_direction_id
BEFORE INSERT ON line_station
BEGIN
    SELECT RAISE(ROLLBACK, "insert on table 'line_station' violates foreign key constraint 'fk_direction_id'")
    WHERE (SELECT id FROM city WHERE id = NEW.direction_id) IS NULL;
END;

--
-- Constraints on stop table
--
CREATE TRIGGER fki_stop_station_id
BEFORE INSERT ON stop
BEGIN
    SELECT RAISE(ROLLBACK, "insert on table 'stop' violates foreign key constraint 'fk_station_id'")
    WHERE (SELECT id FROM station WHERE id = NEW.station_id) IS NULL;
END;

CREATE TRIGGER fki_stop_line_id
BEFORE INSERT ON stop
BEGIN
    SELECT RAISE(ROLLBACK, "insert on table 'stop' violates foreign key constraint 'fk_line_id'")
    WHERE (SELECT id FROM line WHERE id = NEW.line_id) IS NULL;
END;

CREATE TRIGGER fki_stop_direction_id
BEFORE INSERT ON stop
BEGIN
    SELECT RAISE(ROLLBACK, "insert on table 'stop' violates foreign key constraint 'fk_direction_id'")
    WHERE (SELECT id FROM city WHERE id = NEW.direction_id) IS NULL;
END;

CREATE TRIGGER fki_stop_city_id
BEFORE INSERT ON stop
BEGIN
    SELECT RAISE(ROLLBACK, "insert on table 'stop' violates foreign key constraint 'fk_city_id'")
    WHERE (SELECT id FROM city WHERE id = NEW.city_id) IS NULL;
END;
"""
