loadAPI(1);

host.defineController("Edirol", "PCR-300", "1.0", "08B54374-2AF7-4C1F-AB81-E4DDA0957165");
host.defineMidiPorts(3, 2);

host.addDeviceNameBasedDiscoveryPair(["PCR MIDI", "PCR 1", "PCR 2"], ["PCR MIDI", "PCR 1"]);

var transport; // a view onto bitwig's transport section
var cursorDevice;
var masterTrack;
var trackBank;

const MIDI_RES = 128;

// This script is written for "control map" 0 of the PCR keyboard.
// See the README.md for instructions on how to change or reset the control map.
function init() {
  transport = host.createTransport();
  cursorDevice = host.createCursorDevice();
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
  "play": {
    "match": function(channel, data1) {
      return channel === 14 && data1 == 82;
    },
    "func": function(status, data1, data2) {
      if (data2 === 127) {
        transport.play();
      }
    }
  },
  "stop": {
    "match": function(channel, data1) {
      return channel === 13 && data1 == 82;
    },
    "func": function(status, data1, data2) {
      if (data2 === 127) {
        transport.stop();
      }
    }
  },
  "rewind": {
    "match": function(channel, data1) {
      return channel === 8 && data1 == 82;
    },
    "func": function(status, data1, data2) {
      if (data2 === 127) {
        transport.rewind();
      }
    }
  },
  "record": {
    "match": function(channel, data1) {
      return channel === 10 && data1 == 82;
    },
    "func": function(status, data1, data2) {
      if (data2 === 127) {
        transport.record();
      }
    }
  },
  "masterFader": {
    "match": function(channel, data1) {
      return channel === 1 && data1 == 18;
    },
    "func": function(status, data1, data2) {
      masterTrack.getVolume().set(data2, MIDI_RES);
    }
  },
  "macroKnobs": {
    "match": function(channel, data1) {
      return channel < 8 && data1 === 16;
    },
    "func": function(status, data1, data2) {
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
    "func": function(status, data1, data2) {
      var track = trackBank.getTrack(getChannel(status));
      if (track != null) {
        track.getVolume().set(data2, MIDI_RES);
      }
    }
  }
};

function midiInPCR2(status, data1, data2) {
  if (isChannelController(status)) {
    for (var key in controlMap) {
      match = controlMap[key].match;
      func = controlMap[key].func;
      if (match(getChannel(status), data1)) {
        func(status, data1, data2);
      }
    }
  }
}

function getChannel(status) {
  return status & 0xF;
}
