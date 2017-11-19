package net.klingt.akai;

import com.bitwig.extension.callback.BooleanValueChangedCallback;
import com.bitwig.extension.controller.api.MidiOut;

import static com.bitwig.extension.api.util.midi.ShortMidiMessage.NOTE_ON;
import static net.klingt.akai.MidiMix.MUTE;
import static net.klingt.akai.MidiMix.REC_ARM;

public class ArmObserver implements BooleanValueChangedCallback {
    private final int index;
    private final MidiOut midiOut;

    public ArmObserver(MidiOut midiOut, int index) {
        this.midiOut = midiOut;
        this.index = index;
    }

    @Override
    public void valueChanged(boolean newValue) {
        if (index >= REC_ARM.length) {
            return;
        }
        int value = newValue ? 1 : 0;
        midiOut.sendMidi(NOTE_ON, REC_ARM[index], value);
    }
}
