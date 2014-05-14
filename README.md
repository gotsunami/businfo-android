
Bus34 is a free Android application whose goal is to provide an easy way to get 
bus schedules of the french bus networks "Hérault Transport", "Thau Agglo" and 
TAM (south of France).

This is free software released under the GPL (see LICENSE).

## Features

* Easy to use
* Works anytime, anywhere: all schedules are available instantly with no Internet
  connection required (off-line mode)
* Bookmark favorite bus stations
* Share schedules
* Search information by voice using the voice recognition feature of Android 
  devices (if supported).
* Available in French, English and Spanish

Distributed on the Google Play Store. 

## Contribute

1. Create a `local.properties` file at the root of the project with the following entries:

        sdk.dir=/path/to/android/sdk
        lines.dir=/path/to/bus34-lines

2. Create a `gmaps.properties` file at the root of project with:

        gmaps.key.debug=your_gmaps_debug_key
        gmaps.key.release=your_gmaps_release_key
