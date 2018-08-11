package net.klingt.akai;

import com.bitwig.extension.callback.BooleanValueChangedCallback;
import com.bitwig.extension.controller.api.MidiOut;

import static com.bitwig.extension.api.util.midi.ShortMidiMessage.NOTE_ON;

public class ButtonObserver implements BooleanValueChangedCallback {
    private final Integer buttonNote;
    private final MidiOut midiOut;

    public ButtonObserver(Integer buttonNote, MidiOut midiOut) {
        this.buttonNote = buttonNote;
        this.midiOut = midiOut;
    }

    @Override
    public void valueChanged(boolean value) {
        lightButton(value);
    }

    private void lightButton(Boolean value) {
        midiOut.sendMidi(NOTE_ON, buttonNote, value.compareTo(false));
    }
}
