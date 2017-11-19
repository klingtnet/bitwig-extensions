package net.klingt.akai;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;
import com.bitwig.extension.callback.ShortMidiDataReceivedCallback;
import com.bitwig.extension.controller.api.*;

import static net.klingt.akai.MidiMix.*;

public class MidiHandler implements ShortMidiDataReceivedCallback {
    private final Transport transport;
    private final MasterTrack masterTrack;
    private final TrackBank trackBank;
    private boolean soloMode;

    public MidiHandler(Transport transport, MasterTrack masterTrack, TrackBank trackBank, MidiOut midiOut) {
        this.transport = transport;
        this.masterTrack = masterTrack;
        this.trackBank = trackBank;

        // TODO: Move this into MidiExtension's init function
        for (int i = 0; i < trackBank.getSizeOfBank(); i++) {
            Track channel = trackBank.getChannel(i);
            if (channel == null) {
                continue;
            }
            channel.getMute().addValueObserver(new MuteObserver(midiOut, i));
            channel.getSolo().addValueObserver(new SoloObserver(midiOut, i));
            channel.getArm().addValueObserver(new ArmObserver(midiOut, i));
        }
    }

    @Override
    public void midiReceived(int statusByte, int data1, int data2) {
        ShortMidiMessage msg = new ShortMidiMessage(statusByte, data1, data2);
        handleCC(msg);
        handleNote(msg);
    }

    private void handleNote(ShortMidiMessage msg) {
        if (!msg.isNoteOn() && !msg.isNoteOff()) {
            return;
        }

        int key = msg.getData1();

        switch (key) {
            case SOLO_MODE:
                soloMode = msg.isNoteOn();
                break;
            case BANK_LEFT:
                break;
            case BANK_RIGHT:
        }

        // only handle rising edge
        if (msg.isNoteOff()) {
            return;
        }

        if (isIn(key, REC_ARM)) {
            handleArm(key);
        }
        if (isIn(key, MUTE)) {
            handleMute(key);
        }
        if (isIn(key, SOLO)) {
            handleSolo(key);
        }
    }

    private void handleSolo(int key) {
        indexOf(key, SOLO)
                .filter(i -> i < trackBank.getSizeOfBank())
                .map(trackBank::getChannel)
                .ifPresent(this::toggleSolo);
    }

    private void toggleSolo(Track track) {
        track.getSolo().toggle();
    }

    private void handleMute(int key) {
        indexOf(key, MUTE)
                .filter(i -> i < trackBank.getSizeOfBank())
                .map(trackBank::getChannel)
                .ifPresent(this::toggleMute);
    }

    private void toggleMute(Track track) {
        track.getMute().toggle();
    }

    private void handleArm(int key) {
        indexOf(key, REC_ARM)
                .filter(i -> i < trackBank.getSizeOfBank())
                .map(trackBank::getChannel)
                .ifPresent(this::toggleArm);
    }

    private void toggleArm(Track track) {
        track.getArm().toggle();
    }

    private void handleCC(ShortMidiMessage msg) {
        if (!msg.isControlChange()) {
            return;
        }

        int cc = msg.getData1();
        int value = msg.getData2();

        if (cc == MASTER_FADER) {
            masterTrack.getVolume().set(value, MIDI_RESOLUTION);
        }
        if (isIn(cc, KNOBS)) {
        }
        if (isIn(cc, FADERS)) {
            indexOf(cc, FADERS)
                    .filter(i -> i < trackBank.getSizeOfBank())
                    .map(trackBank::getChannel)
                    .ifPresent(ch -> ch.getVolume().set(value, MIDI_RESOLUTION));
        }
    }

    void sysexReceived(final String data) {
        // MMC Transport Controls:
        switch (data) {
            case "f07f7f0605f7":
                transport.rewind();
                break;
            case "f07f7f0604f7":
                transport.fastForward();
                break;
            case "f07f7f0601f7":
                transport.stop();
                break;
            case "f07f7f0602f7":
                transport.play();
                break;
            case "f07f7f0606f7":
                transport.record();
        }
    }
}
