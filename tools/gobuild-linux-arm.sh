#!/bin/sh

# Called by Ant task -native-libs

GOARCH=arm GOOS=linux go build $@
