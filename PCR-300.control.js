loadAPI(1);

host.defineController("Edirol", "PCR-300", "1.0", "08B54374-2AF7-4C1F-AB81-E4DDA0957165");
host.defineMidiPorts(3, 2);

host.addDeviceNameBasedDiscoveryPair(["PCR MIDI", "PCR 1", "PCR 2"], ["PCR MIDI", "PCR 1"]);

var transport; // a view onto bitwig's transport section

// This script is written for "control map" 0 of the PCR keyboard.
// See the README.md for instructions on how to change or reset the control map.
function init() {
  transport = host.createTransport();
  host.getMidiInPort(2).setMidiCallback(midiInPCR2);
}

var controlMap = {
  "play": [14, 82],
  "stop": [13, 82],
  "rewind": [12, 82],
  "record": [10, 82]
};

var actionMap = {
  "play": function(status, data1, data2) {
    if(data2 === 127) {
      transport.play();
    }
  },
  "stop": function(status, data1, data2) {
    if(data2 === 127) {
      transport.stop();
    }
  },
  "rewind": function(status, data1, data2) {
    if(data2 === 127) {
      transport.rewind();
    }
  },
  "record": function(status, data1, data2) {
    if(data2 === 127) {
      transport.record();
    }
  },
}

function midiInPCR2(status, data1, data2) {
  if(isChannelController(status)) {
    for (var key in controlMap) {
      ctrl = controlMap[key];
      if(ctrl[0] === channel(status) && ctrl[1] === data1) {
        actionMap[key](status, data1, data2);
      }
    }
  }
}

function channel(status) {
  return status & 0xF;
}
