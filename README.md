
Businfo is a [free Android application](https://play.google.com/store/apps/details?id=com.monnerville.transports.herault)
whose goal is to provide an easy way to get bus schedules of french bus networks.

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

### Quick Installation With Docker

The easiest way to compile the android application is to use the provided [Docker](http://www.docker.io) image 
[gotsunami/businfo-android](https://registry.hub.docker.com/u/gotsunami/businfo-android/):

    $ docker pull gotsunami/businfo-android
    $ mkdir /tmp/apk
    $ docker run --rm -ti -v /tmp/apk:/home/businfo/businfo-android/bin businfo debug

That's it! You'll get the `/tmp/apk/Businfo-debug.apk` ready to be uploaded to your emulator/device.

### Manual Installation

If you can't use Docker or prefer a source installation, please checkout those repositories first:

    $ git clone https://github.com/gotsunami/businfo-sample-lines.git
    $ git clone https://github.com/gotsunami/businfo-tools.git

The `businfo-sample-lines` repository has some lines definitions (not real schedules) so that sample
data can be used while building the Android application. The `businfo-tools` repository holds tools dealing
with (cleaning, compiling) the bus schedules defined in `businfo-sample-lines`, making chunks of data
that will be merged with the Android app's ressources.

#### Requirements

The following software is required to build the line schedules and the Android application:

* [The Android SDK](http://developer.android.com/sdk/index.html)
* [Go](http://www.golang.org)
* Python 2.7
* git
* sqlite3
* openjdk-7-jdk (also works with Java 1.6)
* ant
* make
* ia32 libs if you're running a 64-bit system

#### Final Setup

Create a `local.properties` file at the root of the project with the following entries:

    sdk.dir=/path/to/android/sdk
    tools.dir=/path/to/businfo-tools

Create a `gmaps.properties` file at the root of project with:

    gmaps.key.debug=your_gmaps_debug_key
    gmaps.key.release=your_gmaps_release_key

Build the project with `ant debug install`.

## License

This is free software released under the GPL (see LICENSE).
