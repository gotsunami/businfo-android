A Docker image for easily build the [Businfo](https://github.com/gotsunami/businfo-android) Android application.

### Quick Start

First, pull the Docker image:

    $ docker pull gotsunami/businfo-android
    $ mkdir /tmp/apk
    $ docker run --rm -ti -v /tmp/apk:/home/businfo/businfo-android/bin businfo debug

That's it, you'll get the `/tmp/apk/Businfo-debug.apk` ready to be uploaded to your emulator/device.
