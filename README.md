
Businfo is a free Android application whose goal is to provide an easy way to get 
bus schedules of french bus networks.

## Supported Bus Networks

* Hérault Transport
* Thau Agglo

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

### Quick Installation

The easiest way to compile the android application is to use the provided Docker image `gotsunami/businfo`:

    $ docker pull gotsunami/businfo-android
    $ mkdir /tmp/apk
    $ docker run --rm -ti -v /tmp/apk:/home/businfo/businfo-android/bin businfo debug

You'll get the `/tmp/apk/Businfo-debug.apk` ready to be uploaded to your emulator/device.

### Manual Installation

1. Create a `local.properties` file at the root of the project with the following entries:

    sdk.dir=/path/to/android/sdk
    tools.dir=/path/to/businfo-tools

2. Create a `gmaps.properties` file at the root of project with:

    gmaps.key.debug=your_gmaps_debug_key
    gmaps.key.release=your_gmaps_release_key

3. Build the project with

    $ ant clean
    $ ant debug install

## License

This is free software released under the GPL (see LICENSE).
