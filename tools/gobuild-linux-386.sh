#!/bin/sh

# Called by Ant task -native-libs

GOARCH=386 GOOS=linux go build $@
