package main

import "testing"

func equal(a, b []string) bool {
    if len(a) != len(b) {
        return false
    }
    for j := range a {
        if a[j] != b[j] {
            return false
        }
    }
    return true
}

func TestGetDays(t *testing.T) {
    set := map[string][]string{
        "L à V LMaMeJ LàS": {"1-5", "1,2,3,4", "1-6"},
        "L à V Ma à Di":    {"1-5", "2-7"},
        "LLL MaàJ":         {"1", "2-4"},
        "L   Ma":           {"1", "2"},
        "L   à    J":       {"1-4"},
        "L   à    J Ma V":  {"1-4", "2", "5"},
        "DF":               {"7,r"},
        "SaDF":             {"6,7,r"},
        "Me/Sa":            {"3,6"},
        // FIXME        "L/Ma/V Di":           {"1,2,5", "7"},
    }

    for in, out := range set {
        if res := get_days(in); !equal(res, out) {
            t.Errorf("%s: got %s, want %s", in, res, out)
        }
    }
}

func TestGetFeatures(t *testing.T) {
    set := map[string][]string{
        "NSCO[1]":                     {"S"},
        "NSCO   0":                    {"S", ""},
        "SCO 0 0   0 0 0  0 0  0 SCO": {"s", "", "", "", "", "", "", "", "", "s"},
    }

    for in, out := range set {
        if res := get_features(in); !equal(res, out) {
            t.Errorf("%s: got %s, want %s", in, res, out)
        }
    }
}
