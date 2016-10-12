loadAPI(1);

host.defineController("Edirol", "PCR-300", "1.0", "08B54374-2AF7-4C1F-AB81-E4DDA0957165");
host.defineMidiPorts(3, 2);

host.addDeviceNameBasedDiscoveryPair(["PCR MIDI", "PCR 1", "PCR 2"], ["PCR MIDI", "PCR 1"]);

var transport; // a view onto bitwig's transport section
var cursorDevice;
var cursorTrack;
var cursorClip;
var masterTrack;
var trackBank;

var seqRoot = 36;

const MIDI_RES = 128;

// This script is written for "control map" 0 of the PCR keyboard.
// See the README.md for instructions on how to change or reset the control map.
function init() {
  transport = host.createTransport();
  cursorDevice = host.createCursorDevice();
  cursorTrack = host.createCursorTrack(0, 0); // sends, scenes
  cursorClip = host.createCursorClip(8, 2); // grid width, height
  masterTrack = host.createMasterTrack(0);
  trackBank = host.createMainTrackBank(8, 0, 0); // tracks, sends

  // Note input, exclude filtered messages from callback
  host.getMidiInPort(1).createNoteInput("PCR-300", "8?????" // Note On
    , "9?????" // Note Off
    , "B?40??" // Damper Pedal
    , "D?????" // Channel Pressue / After-Touch
    , "E?????"); // Pitch Bend

  // Control input
  host.getMidiInPort(2).setMidiCallback(midiInPCR2);
}

var controlMap = {
  "transport": {
    "match": function(channel, data1) {
      var channels = [14, 13, 8, 10, 0, 1, 2];
      if (data1 === 82) {
        for (idx in channels) {
          if (channel === channels[idx]) {
            return true;
          }
        }
      }
      return false;
    },
    "action": function(status, data1, data2) {
      if (data2 === 127) {
        switch (getChannel(status)) {
          case 14:
            transport.play();
            break;
          case 13:
            transport.stop();
            break;
          case 8:
            transport.rewind();
            break;
          case 10:
            transport.record();
            break;
          case 0:
            cursorTrack.getArm().toggle();
            break;
          case 1:
            cursorTrack.getSolo().toggle();
            break;
          case 2:
            cursorTrack.getMute().toggle();
            break;
        }
      }
    }
  },
  "masterFader": {
    "match": function(channel, data1) {
      return channel === 1 && data1 === 18;
    },
    "action": function(status, data1, data2) {
      masterTrack.getVolume().set(data2, MIDI_RES);
    }
  },
  "macroKnobs": {
    "match": function(channel, data1) {
      return channel < 8 && data1 === 16;
    },
    "action": function(status, data1, data2) {
      var macro = cursorDevice.getMacro(getChannel(status));
      if (macro != null) {
        macro.getAmount().set(data2, MIDI_RES);
      }
    }
  },
  "volumeFader": {
    "match": function(channel, data1) {
      return channel < 8 && data1 === 17;
    },
    "action": function(status, data1, data2) {
      var track = trackBank.getTrack(getChannel(status));
      if (track != null) {
        track.getVolume().set(data2, MIDI_RES);
      }
    }
  },
  "clipScroll": {
    "match": function(channel, data1) {
      return channel < 2 && data1 === 83;
    },
    "action": function(status, data1, data2) {
      if (data2 === 127) {
        if (getChannel(status) === 0) {
          seqRoot++;
        } else {
          seqRoot--;
        }
      }
    }
  },
  "clipPattern": {
    "match": function(channel, data1) {
      return channel < 8 && (data1 === 80 || data1 === 81);
    },
    "action": function(status, data1, data2) {
      if (data2 === 127) {
        cursorClip.toggleStep(getChannel(status) + 8 * (data1 - 80), seqRoot, 100);
      }
    }
  }
};

function midiInPCR2(status, data1, data2) {
  if (isChannelController(status)) {
    for (var key in controlMap) {
      match = controlMap[key].match;
      action = controlMap[key].action;
      if (match(getChannel(status), data1)) {
        action(status, data1, data2);
      }
    }
  }
}

function getChannel(status) {
  return status & 0xF;
}
