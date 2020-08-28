# Bitwig Extensions of klingt.net


![CI](https://github.com/klingtnet/bitwig-extensions/workflows/CI/badge.svg)

[Releases](https://github.com/klingtnet/bitwig-extensions/releases)

This is a collection of my custom controller extensions, previously called scripts, for Bitwig.

## Instructions

The installation is super easy, either you use the precompiled extension from the [github release page][releases] or you build it yourself, given GNU Make, Maven and Java12+ is available:

```sh
$ make install
```

## Installation

The recommended installation method is to download a precompiled release from [this projects release page](https://github.com/klingtnet/bitwig-extensions/releases) into your `~/Bitwig Studio/Extensions` folder.
Note that the `Extensions` path is probably different for Windows (`%USERPROFILE%\Documents\Bitwig Studio\Extensions\`) or Mac.

## Troubleshooting

I only tested the extensions under Linux and Windows, so if does not automatically detect them please [open up an issue][issues] and tell me the names of the MIDI controller's ports as shown in Bitwig's controller menu.

[releases]: https://github.com/klingtnet/bitwig-extensions/releases
[issues]: https://github.com/klingtnet/bitwig-extensions/issues

## AKAI Midimix

How the controller is mapped:

- faders control track volume
- right most fader controls the master track volume
- mute, rec/arm and solo work as expected
- bank left/right steps a full page (8 channels) forward or backward
- the top row of knobs is mapped to the device parameters (macro controls)

## AKAI MPK mini mk2

A barebones Bitwig extension for AKAI's MPK mini mk2, with those features:

- knobs are automapped to macro controls of the selected device
- pads are automapped to the first 8 samples of the drum machine selected
- x-axis of joystick controller sends Pitchbend
- y-axis of joystick controller is assignable and not automapped

### Controller settings

I assume that the default controller programs are used.
To use the extension the controller must be in `PROG 4` which you can select like this:

- press and hold `PROG SELECT`
- press `PAD 8`
- release both

To use the automapping for pads the controller must be in `CC` mode.

![Top view of AKAI MPK mini mk2](./AKAI-MPK-mini-mk2.jpg)

## Edirol PCR


The controller script was only tested with the [PCR-300](http://www.rolandus.com/products/pcr-300/) but should work with the larger models (`PCR-{500,800}`), as well. The controller script assumes that you're using **controller map 0**. Check the [tips](#tips) section or refer to the user's manual on how to change the control map.

### Troubleshooting

- ~~Aftertouch must be turned off for the hold-pedal to work~~ It showed that the _hold pedal jack_ had a slight connection problem and rotating the plug some degrees fixed the issue.

### Notes

- the 9th knob is not mapped. If someone has a good idea on where to map it then please let me know!

### Tips

- Switch to controller map `x`:
    - Press `Control Map`
    - Turn value knob until `x` is shown in the display
    - Done
- Reset the keyboard to factory defaults (manual page 81):
    - Press `Edit`
    - Select `SYS` using the value knob and press `Enter`
    - Select `SY16` using the value knob and press `Enter`
    - if `RST` is blinking in the display, press `Enter`
    - Now `YES` should be blinking
    - Confirm with `Enter`
- Show (firmware) program version of your PCR keyboard:
    - Power off the keyboard
    - Press and hold `Dynamic Mapping` and `L1`
    - Power on
    - Version will be displayed
    - This script should work with older version but was only tested with the latest firmware version [`1.05`](http://roland.com/support/article/?q=downloads&p=PCR-300&id=1812363)

### Links

- [Owner's Manual](http://lib.roland.co.jp/support/en/manuals/res/1810983/PCR-300_500_800_e2.pdf)
