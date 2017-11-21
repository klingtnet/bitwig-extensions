# Bitwig Controller Script for AKAI Midimix

[![Build Status](https://travis-ci.org/klingtnet/bitwig-akai-midimix.svg?branch=master)](https://travis-ci.org/klingtnet/bitwig-akai-midimix)

How the controller is mapped:

- faders control track volume
- right most fader controls the master track volume
- mute, rec/arm and solo work as expected
- bank left/right steps a full page (8 channels) forward or backward
- the top row of knobs is mapped to the device parameters (macro controls)

## Installation

The recommend installation method is to download a precompiled release from [this projects release page](https://github.com/klingtnet/bitwig-akai-midimix/releases) into your `~/Bitwig Studio/Extensions` folder.
Note that the `Extensions` path is probably different for Windows or Mac.

### From Source (Development)

Installing from source requires mvn and a Java 8 JDK.

```sh
$ mvn install
```

I would recommend to symlink the extension like this:

```sh
$ ln -s "$PWD/target/Midimix.bwextension" ~/Bitwig\ Studio/Extensions
```
