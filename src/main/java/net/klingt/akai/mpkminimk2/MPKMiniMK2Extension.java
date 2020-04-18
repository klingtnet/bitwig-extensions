package net.klingt.akai.mpkminimk2;

import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.MidiOut;

public class MPKMiniMK2Extension extends ControllerExtension {
    public static String[] MIDI_MASKS = new String[]{"8?????" // Note On
            , "9?????" // Note Off
            , "B?40??" // Damper Pedal
            , "D?????" // Channel Pressue / After-Touch
            , "E?????"};
    private MidiOut midiOut;

    protected MPKMiniMK2Extension(final MPKMiniMK2ExtensionDefinition definition, final ControllerHost host) {
        super(definition, host);
    }

    @Override
    public void init() {
        final ControllerHost host = getHost();

        this.midiOut = host.getMidiOutPort(0);
        MidiHandler midiHandler = new MidiHandler(host.createCursorTrack(0, 0));

        host.getMidiInPort(0).createNoteInput("MPK-mini-mk2-KEYS", MIDI_MASKS);
        host.getMidiInPort(0).setMidiCallback(midiHandler::midiReceived);

        host.showPopupNotification("Akai MPK mini mk2 Initialized");
    }

    @Override
    public void exit() {
    }

    @Override
    public void flush() {
    }
}
