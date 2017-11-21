# Bitwig Controller Script for AKAI Midimix

[![Build Status](https://travis-ci.org/klingtnet/bitwig-akai-midimix.svg?branch=master)](https://travis-ci.org/klingtnet/bitwig-akai-midimix)

How it is mapped:

- faders control track volume
- right most fader controls the master track volume
- mute, rec/arm and solo work as expected
- bank left/right steps a full page (8 channels) forward or backward
- the top row of knobs is mapped to the device parameters (macro controls)

## Installation

Installing from source requires mvn and a Java 8 JDK.

```sh
$ mvn install
$ cp target/Midimix.bwextension ~/Bitwig\ Studio/Extensions
```
