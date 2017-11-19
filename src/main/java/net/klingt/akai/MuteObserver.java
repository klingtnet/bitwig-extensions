package net.klingt.akai;

import com.bitwig.extension.callback.BooleanValueChangedCallback;
import com.bitwig.extension.controller.api.MidiOut;

import static com.bitwig.extension.api.util.midi.ShortMidiMessage.CONTROL_CHANGE;
import static com.bitwig.extension.api.util.midi.ShortMidiMessage.NOTE_ON;
import static net.klingt.akai.MidiMix.MUTE;

public class MuteObserver implements BooleanValueChangedCallback {
    private final int index;
    private final MidiOut midiOut;

    public MuteObserver(MidiOut midiOut, int index) {
        this.midiOut = midiOut;
        this.index = index;
    }

    @Override
    public void valueChanged(boolean newValue) {
        if (index >= MUTE.length) {
            return;
        }
        int value = newValue ? 1 : 0;
        midiOut.sendMidi(NOTE_ON, MUTE[index], value);
    }
}
