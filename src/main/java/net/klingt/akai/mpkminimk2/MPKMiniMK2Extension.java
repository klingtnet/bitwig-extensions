package net.klingt.akai.mpkminimk2;

import com.bitwig.extension.api.Color;
import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.AbsoluteHardwareKnob;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.CursorRemoteControlsPage;
import com.bitwig.extension.controller.api.CursorTrack;
import com.bitwig.extension.controller.api.HardwareButton;
import com.bitwig.extension.controller.api.HardwareSlider;
import com.bitwig.extension.controller.api.HardwareSurface;
import com.bitwig.extension.controller.api.MidiIn;
import com.bitwig.extension.controller.api.OnOffHardwareLight;
import com.bitwig.extension.controller.api.PianoKeyboard;
import com.bitwig.extension.controller.api.PinnableCursorDevice;
import com.bitwig.extension.controller.api.Preferences;
import com.bitwig.extension.controller.api.SettableEnumValue;
import com.bitwig.extension.controller.api.SettableRangedValue;
import com.bitwig.extension.controller.api.UserControlBank;

import java.util.Arrays;
import java.util.stream.IntStream;

import static java.lang.String.format;

public class MPKMiniMK2Extension extends ControllerExtension {
    public static final String NOTE_ON_MASK = "80????";
    public static final String NOTE_OFF_MASK = "90????";
    public static final String NOTE_ON_PAD_MASK = "89????";
    public static final String NOTE_OFF_PAD_MASK = "99????";
    public static final String PITCHBEND_MASK = "E0????";
    private static final String[] KEY_NAMES = new String[]{"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "H"};
    private HardwareSurface hardwareSurface;

    protected MPKMiniMK2Extension(final MPKMiniMK2ExtensionDefinition definition, final ControllerHost host) {
        super(definition, host);
    }

    @Override
    public void init() {
        final ControllerHost host = getHost();
        Preferences preferences = host.getPreferences();
        SettableEnumValue rootKey = preferences.getEnumSetting("Root Key", "Drum Pads", KEY_NAMES, "C");
        SettableRangedValue baseOctave = preferences.getNumberSetting("Octave", "Drum Pads", 0, 8, 1, "", 2);

        MidiIn midiIn = host.getMidiInPort(0);
        CursorTrack cursorTrack = host.createCursorTrack("cursor", "mpk-mini-mk2-cursor", 0, 0, true);
        PinnableCursorDevice cursorDevice = cursorTrack.createCursorDevice();
        CursorRemoteControlsPage remoteControlsPage = cursorDevice.createCursorRemoteControlsPage(8);

        hardwareSurface = host.createHardwareSurface();
        hardwareSurface.setPhysicalSize(318.0, 180.0);
        PianoKeyboard keyboard = hardwareSurface.createPianoKeyboard("keyboard", 25, 0, 0);
        keyboard.setChannel(0);
        keyboard.setIsVelocitySensitive(true);
        keyboard.setSupportsPolyAftertouch(false);
        midiIn.createNoteInput("MPK-mini-mk2-KEYS", NOTE_ON_MASK, NOTE_OFF_MASK, NOTE_ON_PAD_MASK, NOTE_OFF_PAD_MASK, PITCHBEND_MASK);
        keyboard.setMidiIn(midiIn);
        keyboard.setBounds(10.0, 95.0, 295.0, 80.0);

        HardwareSlider pitchbend = hardwareSurface.createHardwareSlider("JOYSTICK_PB");
        pitchbend.setIsHorizontal(true);
        pitchbend.setBounds(15.0, 17.5, 25.0, 10.0);
        pitchbend.setAdjustValueMatcher(midiIn.createAbsolutePitchBendValueMatcher(1));

        UserControlBank modWheel = host.createUserControls(2);
        HardwareSlider modwheelUp = hardwareSurface.createHardwareSlider("JOYSTICK_MODW_UP");
        modwheelUp.setBounds(22.5, 10.0, 10.0, 12.5);
        modwheelUp.setAdjustValueMatcher(midiIn.createAbsoluteCCValueMatcher(0, 30));
        modwheelUp.setBinding(modWheel.getControl(0).value());
        HardwareSlider modwheelDown = hardwareSurface.createHardwareSlider("JOYSTICK_MODW_DOWN");
        modwheelDown.setBounds(22.5, 22.5, 10.0, 12.5);
        modwheelDown.setAdjustValueMatcher(midiIn.createAbsoluteCCValueMatcher(0, 31));
        modwheelDown.setBinding(modWheel.getControl(1).value());

        // Mapped to controllers of program 4.
        IntStream.range(0, 8).forEach(idx -> {
            int col = idx % 4;
            int row = idx / 4;
            Color red = Color.fromRGB(1.0, 0.0, 0.0);

            AbsoluteHardwareKnob knob = hardwareSurface.createAbsoluteHardwareKnob(format("KNOB_%d", idx));
            knob.setLabel(format("K%d", idx + 1));
            knob.setBounds(195.0 + 30.0 * col, 15.0 + 30.0 * row, 12.0, 12.0);
            knob.setAdjustValueMatcher(midiIn.createAbsoluteCCValueMatcher(0, 20 + idx));
            knob.setBinding(remoteControlsPage.getParameter(idx).value());

            OnOffHardwareLight padLight = hardwareSurface.createOnOffHardwareLight(format("PAD_LIGHT_%d", idx));
            padLight.setOnColor(red);
            // Lights are not controllable :(
            HardwareButton pad = hardwareSurface.createHardwareButton(format("PAD_%d", idx));
            pad.setLabel(format("PAD %d", idx + 1));
            pad.setBounds(55.0 + 35.0 * col, 10.0 + 35.0 * (1 - row), 25.0, 25.0);
            pad.setBackgroundLight(padLight);
            pad.pressedAction().setPressureActionMatcher(midiIn.createAbsoluteCCValueMatcher(9, idx));
            pad.pressedAction().setBinding(host.createAction(pressure -> {
                if (pressure > 0.0) {
                    cursorTrack.playNote(padKey(rootKey, baseOctave, idx), 127);
                }
            }, () -> format("play drum pad %d", idx)));
        });

        host.showPopupNotification("Akai MPK mini mk2 Initialized.");
    }

    private int padKey(SettableEnumValue rootKey, SettableRangedValue baseOctave, int idx) {
        int keyInOctave = Arrays.asList(KEY_NAMES).indexOf(rootKey.get());
        if (keyInOctave < 0 || keyInOctave > 11) {
            keyInOctave = 0;
        }
        int baseKey = Double.valueOf(baseOctave.getRaw()).intValue() * 12;
        return baseKey + keyInOctave + idx;
    }

    @Override
    public void exit() {
        getHost().showPopupNotification("Akai MPK mini mk2 Exited.");
    }

    @Override
    public void flush() {
        hardwareSurface.updateHardware();
    }
}