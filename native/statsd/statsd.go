package main

// Simple web RPC service collecting statistics for bus34.
// Stats are written to a MySQL database.

import (
	"fmt"
	_ "net"
	"os"

	"github.com/go-martini/martini"
)

const (
	BUSINFO_STATSD_PORT    = "BUSINFO_STATSD_PORT"
	BUSINFO_MYSQL_HOST     = "BUSINFO_MYSQL_HOST"
	BUSINFO_MYSQL_PORT     = "BUSINFO_MYSQL_PORT"
	BUSINFO_MYSQL_LOGIN    = "BUSINFO_MYSQL_LOGIN"
	BUSINFO_MYSQL_PASSWORD = "BUSINFO_MYSQL_PASSWORD"
)

func exit(msg string) {
	fmt.Fprintf(os.Stderr, "error: %s\n", msg)
	os.Exit(2)
}

var conf map[string]string = map[string]string{
	BUSINFO_STATSD_PORT:    "",
	BUSINFO_MYSQL_HOST:     "",
	BUSINFO_MYSQL_PORT:     "",
	BUSINFO_MYSQL_LOGIN:    "",
	BUSINFO_MYSQL_PASSWORD: "",
}

func main() {
	/*
		for k, _ := range conf {
			conf[k] = os.Getenv(k)
			if conf[k] == "" {
				exit(fmt.Sprintf("missing %s", k))
			}
		}
	*/
	m := martini.Classic()
	m.Post("/v1/usage", func(c martini.Context) string {
		return "OK"
	})
	m.Run()
}
