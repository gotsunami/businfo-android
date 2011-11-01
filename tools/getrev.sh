#!/bin/sh

version=`grep app_version res/values/changelog.xml|sed 's,<string name="app_version">,,;s,</string>,,'`
echo $version
