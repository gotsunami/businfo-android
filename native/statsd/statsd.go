package main

// Simple web RPC service collecting statistics for bus34.
// Stats are written to a MySQL database.

import (
	"fmt"
	"log"
	"net/http"
	"os"
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
	http.HandleFunc("/v1/stats", func(w http.ResponseWriter, r *http.Request) {
		if r.Method != "POST" {
			http.NotFound(w, r)
			return
		}
		if err := r.ParseForm(); err != nil {
			http.Error(w, http.StatusText(http.StatusBadRequest), http.StatusBadRequest)
			return
		}
		fmt.Fprintf(w, "yeah!")
	})

	log.Fatal(http.ListenAndServe(":8080", nil))
}
