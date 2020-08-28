package net.klingt.arturia;

import com.bitwig.extension.api.Color;
import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.*;

import java.util.Arrays;
import java.util.stream.IntStream;

import static java.lang.String.format;

public class ArturiaKeyStepExtension extends ControllerExtension {
    public static final String NOTE_ON_MASK = "80????";
    public static final String NOTE_OFF_MASK = "90????";
    public static final String NOTE_ON_PAD_MASK = "89????";
    public static final String NOTE_OFF_PAD_MASK = "99????";
    public static final String PITCHBEND_MASK = "E900??";
    public static final String MODWHEEL_MSB_MASK = "B901??";
    private HardwareSurface hardwareSurface;

    protected ArturiaKeyStepExtension(final ArturiaKeyStepExtensionDefinition definition, final ControllerHost host) {
        super(definition, host);
    }

    @Override
    public void init() {
        final ControllerHost host = getHost();

        MidiIn midiIn = host.getMidiInPort(0);

        hardwareSurface = host.createHardwareSurface();
        hardwareSurface.setPhysicalSize(484.0, 145.0);
        PianoKeyboard keyboard = hardwareSurface.createPianoKeyboard("keyboard", 32, 0, 0);
        keyboard.setChannel(0);
        keyboard.setIsVelocitySensitive(true);
        keyboard.setSupportsPolyAftertouch(true);
        midiIn.createNoteInput("ARTURIA-KEYSTEP-KEYS", NOTE_ON_MASK, NOTE_OFF_MASK, NOTE_ON_PAD_MASK, NOTE_OFF_PAD_MASK, PITCHBEND_MASK, MODWHEEL_MSB_MASK);
        keyboard.setMidiIn(midiIn);
        keyboard.setBounds(67.0, 50.0, 402.0, 90.0);

        // TODO: check MIDI assignment
        HardwareSlider pitchbend = hardwareSurface.createHardwareSlider("ARTURIA-KEYSTEP-PITCHBEND");
        pitchbend.setIsHorizontal(false);
        pitchbend.setBounds(13.0, 80.0, 17.0, 55.0);
        pitchbend.setAdjustValueMatcher(midiIn.createAbsolutePitchBendValueMatcher(10));

        // TODO: check MIDI assignment
        UserControlBank modWheel = host.createUserControls(1);
        HardwareSlider modwheelSlider = hardwareSurface.createHardwareSlider("ARTURIA-KEYSTEP-MODWHEEL");
        modwheelSlider.setBounds(39.0, 80.0, 17.0, 55.0);
        modwheelSlider.setAdjustValueMatcher(midiIn.createAbsoluteCCValueMatcher(10, 1));
        modwheelSlider.setBinding(modWheel.getControl(0).value());

        host.showPopupNotification("Arturia Keystep Initialized.");
    }

    @Override
    public void exit() {
        getHost().showPopupNotification("Arturia Keystep Exited.");
    }

    @Override
    public void flush() {
        hardwareSurface.updateHardware();
    }
}