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
// An input like this one:
// 
// L à V LMaMeJ LàS
//
// will convert to a three element slice:
//
// [1-5 1,2,3,4 1-6]
func get_days(days string) []string {
    sdays := strings.Split(days, SEP)
    res := make([][]string, 0)
    k := 0

    for k < len(sdays) {
        if k < len(sdays)-1 {
            if found(sdays[k+1], DAYS_S) {
                res = append(res, sdays[k:k+3])
                k += 3
                continue
            } else {
                res = append(res, []string{sdays[k]})
            }
        } else {
            res = append(res, []string{sdays[k]})
        }
        k++
    }

    // [[L à V] [LMaMeJ] [LàS]]

    // Find separators in strings with no spaces
    for k, pat := range res {
        for _, sep := range DAYS_S {
            if len(pat) == 1 {
                if strings.Contains(pat[0], sep) {
                    z := strings.Split(pat[0], sep)
                    // new slice with distinct, separated elements
                    z = []string{z[0], sep, z[1]}
                    res[k] = z
                }
            }
        }
    }

    // [[L à V] [LMaMeJ] [L à S]]

    // Remaining strings without special chars
    for k, pat := range res {
        if len(pat) == 1 {
            // Like LMaMeJ, no spaces in it, one block
            tmp := []string{}
            for _, day := range DAYS {
                if strings.Contains(pat[0], day) {
                    // Special case: check for any day synonyms
                    if _, has := DUPS[day]; has {
                        if !found(DUPS[day], tmp) {
                            tmp = append(tmp, day)
                            tmp = append(tmp, "/")
                        }
                    } else {
                        tmp = append(tmp, day)
                        tmp = append(tmp, "/")
                    }
                }
            }
            res[k] = tmp[:len(tmp)-1] // remove trailing /
        }
        k++
    }

    // [[L à V] [L / Ma / Me / J] [L à S]]

    fres := []string{}
    for _, pat := range res {
        tmp := ""
        for _, j := range pat {
            tmp += HTMAP[j]
        }
        fres = append(fres, tmp)
    }

    return fres
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

    /*
       directions := []string{}
          dir1 := false
          dir2 := false
          for line := range directions {
          }
    */
    d := get_days("L à V LMaMeJ LàS")
    fmt.Println(d)
}
