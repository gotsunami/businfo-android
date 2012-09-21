package main

import (
    "fmt"
    "os"
    "sort"
    "strings"
)

const (
    SEP = " "
)

var (
    DAYS = []string{"L", "Ma", "Me", "J", "V", "S", "Sa", "D", "Di", "F"}

    // Usual days separators
    DAYS_S = []string{"à", "/"}

    // Mapping of possible duplicate days
    DUPS = map[string]string{"S": "Sa", "Sa": "S", "D": "Di", "Di": "D"}

    // Mapping between weekdays and the *num* format policy required by the 
    // Android app
    HTMAP = map[string]string{
        "L":  "1",
        "Ma": "2",
        "Me": "3",
        "J":  "4",
        "V":  "5",
        "S":  "6",
        "Sa": "6",
        "D":  "7",
        "Di": "7",
        "F":  "r", // rest days
        "à":  "-",
        "/":  ",",
    }

    // Bus line features mapping
    FEATMAP = map[string]string{"0": "", "NSCO": "S", "SCO": "s"}
)

type citySpot struct {
    city     string   // city name
    stations []string // city bus line stations
}

var cities = []citySpot{}
var cur_city = ""

func found(val string, a []string) bool {
    return a[sort.SearchStrings(a, val)] == val
}

// Compute the list of days
func get_days(days string) []string {
    sdays := strings.Split(days, SEP)
    res := []string{}
    k := 0

    for _, day := range sdays {
        if k < len(sdays)-1 {
            if found(sdays[k+1], DAYS_S) {
                append(res, sdays[k:k+3])
                k += 3
                continue
            }
            /*
               else {
                   append(res, sdays[k])
               }
            */
        } else {
            append(res, sdays[k])
        }
        k++
    }

    return res
}

func debug(msg string) {
    fmt.Println("=> " + msg)
}

func error(msg string) {
    fmt.Println("Error: " + msg)
    os.Exit(1)
}

func init() {
    sort.Strings(DAYS_S)
}

func main() {
    if len(os.Args) != 2 {
        fmt.Println("Missing bus line argument")
        fmt.Printf("Usage: %s line.in\n", os.Args[0])
        os.Exit(2)
    }

    directions := []string{}

    /*
       dir1 := false
       dir2 := false
       for line := range directions {
       }
    */
    s := append(directions, "too")
    fmt.Println(s)
}
