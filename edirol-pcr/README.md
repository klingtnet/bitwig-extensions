# Bitwig Controller Script for the [Edirol PCR](http://www.rolandus.com/products/pcr-300/) USB Midi controller family

[![Build Status](https://travis-ci.org/klingtnet/bitwig-edirol-pcr.svg?branch=master)](https://travis-ci.org/klingtnet/bitwig-edirol-pcr)

The controller script was only tested with the [PCR-300](http://www.rolandus.com/products/pcr-300/) but should work with the larger models (`PCR-{500,800}`), as well. The controller script assumes that you're using **controller map 0**. Check the [tips](#tips) section or refer to the user's manual on how to change the control map.

## Troubleshooting

- Aftertouch must be turned off for the hold-pedal to work

## Notes

- the 9th knob is not mapped. If someone has a good idea on where to map it then please let me know!

## Tips

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

## Links

- [Owner's Manual](http://lib.roland.co.jp/support/en/manuals/res/1810983/PCR-300_500_800_e2.pdf)

