package net.klingt.akai.midimix;

import com.bitwig.extension.api.Color;
import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.AbsoluteHardwareKnob;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.CursorRemoteControlsPage;
import com.bitwig.extension.controller.api.HardwareButton;
import com.bitwig.extension.controller.api.HardwareSlider;
import com.bitwig.extension.controller.api.HardwareSurface;
import com.bitwig.extension.controller.api.MidiIn;
import com.bitwig.extension.controller.api.MidiOut;
import com.bitwig.extension.controller.api.OnOffHardwareLight;
import com.bitwig.extension.controller.api.Send;
import com.bitwig.extension.controller.api.Track;
import com.bitwig.extension.controller.api.TrackBank;

import java.util.stream.IntStream;

import static com.bitwig.extension.api.util.midi.ShortMidiMessage.NOTE_ON;
import static java.lang.String.format;

public class MidimixExtension extends ControllerExtension {

    static final int[][] KNOBS = new int[][]{
            {16, 20, 24, 28, 46, 50, 54, 58},
            {17, 21, 25, 29, 47, 51, 55, 59},
            {18, 22, 26, 30, 48, 52, 56, 60}
    };
    static final int[] FADERS = new int[]{19, 23, 27, 31, 49, 53, 57, 61};
    static final int MASTER_FADER = 62;
    static final int[] MUTE = new int[]{1, 4, 7, 10, 13, 16, 19, 22};
    static final int[] SOLO = new int[]{2, 5, 8, 11, 14, 17, 20, 23};
    static final int[] REC_ARM = new int[]{3, 6, 9, 12, 15, 18, 21, 24};
    static final int BANK_LEFT = 25;
    static final int BANK_RIGHT = 26;
    static final int SOLO_MODE = 27;
    HardwareSurface hardwareSurface;

    protected MidimixExtension(final MidimixExtensionDefinition definition, final ControllerHost host) {
        super(definition, host);
    }

    @Override
    public void init() {
        final ControllerHost host = getHost();

        MidiIn midiIn = host.getMidiInPort(0);
        MidiOut midiOut = host.getMidiOutPort(0);
        Track masterTrack = host.createMasterTrack(0);
        TrackBank trackBank = host.createMainTrackBank(8, 2, 0);
        CursorRemoteControlsPage remoteControlsPage = host.createCursorTrack("cursor", "midimix-cursor", 0, 0, true).createCursorDevice().createCursorRemoteControlsPage(8);

        hardwareSurface = host.createHardwareSurface();
        hardwareSurface.setPhysicalSize(237, 198);
        // Hardware element positions are relative to top left corner of the element.

        IntStream.range(0, 3).forEach(row -> {
            double yOff = 21.0;
            double xOff = 12.0;
            double xDist = 25.0;
            double yDist = 32.0;

            IntStream.range(0, 8).forEach(col -> {
                AbsoluteHardwareKnob knob = hardwareSurface.createAbsoluteHardwareKnob(format("KNOB_%d_%d", row, col));
                knob.setBounds(xOff + col * xDist, yOff + row * yDist, 12.0, 12.0);
                knob.setAdjustValueMatcher(midiIn.createAbsoluteCCValueMatcher(0, KNOBS[row][col]));
                if (row == 0) {
                    knob.setBinding(remoteControlsPage.getParameter(col).value());
                } else {
                    Send send = trackBank.getItemAt(col).sendBank().getItemAt(row - 1);
                    knob.setBinding(send.value());
                }
            });
        });

        IntStream.range(0, 8).forEach(idx -> {
            HardwareSlider slider = hardwareSurface.createHardwareSlider(format("SLIDER_%d", idx));
            slider.setLabel(Integer.toString(idx + 1));
            slider.setBounds(8.0 + idx * 25.0, 145.0, 20.0, 50.0);
            slider.setAdjustValueMatcher(midiIn.createAbsoluteCCValueMatcher(0, FADERS[idx]));
            slider.setBinding(trackBank.getItemAt(idx).volume());
        });
        HardwareSlider masterSlider = hardwareSurface.createHardwareSlider("MASTER_SLIDER");
        masterSlider.setLabel("MASTER");
        masterSlider.setBounds(8.0 + 8 * 25.0, 145.0, 20.0, 50.0);
        masterSlider.setAdjustValueMatcher(midiIn.createAbsoluteCCValueMatcher(0, MASTER_FADER));
        masterSlider.setBinding(masterTrack.volume());

        Color red = Color.fromRGB(1.0, 0.0, 0.0);
        Color yellow = Color.fromRGB(1.0, 1.0, 0.0);
        IntStream.range(0, 8).forEach(idx -> {
            Track trackCursor = trackBank.getItemAt(idx);
            trackCursor.arm().markInterested();
            trackCursor.mute().markInterested();
            trackCursor.solo().markInterested();

            OnOffHardwareLight muteButtonLight = hardwareSurface.createOnOffHardwareLight(format("MUTE_BUTTON_LIGHT_%d", idx));
            muteButtonLight.setOnColor(yellow);
            muteButtonLight.isOn().setValueSupplier(trackCursor.mute());
            muteButtonLight.isOn().onUpdateHardware(state -> midiOut.sendMidi(NOTE_ON, MUTE[idx], state.compareTo(false)));
            HardwareButton muteButton = hardwareSurface.createHardwareButton(format("MUTE_BUTTON_%d", idx));
            muteButton.setLabel("MUTE");
            muteButton.setBounds(14.0 + idx * 25.0, 116.0, 10.0, 5.0);
            muteButton.setBackgroundLight(muteButtonLight);
            muteButton.pressedAction().setActionMatcher(midiIn.createNoteOnActionMatcher(0, MUTE[idx]));
            muteButton.pressedAction().setBinding(trackCursor.mute().toggleAction());

            OnOffHardwareLight soloButtonLight = hardwareSurface.createOnOffHardwareLight(format("SOLO_BUTTON_LIGHT_%d", idx));
            soloButtonLight.setOnColor(yellow);
            soloButtonLight.isOn().setValueSupplier(trackCursor.solo());
            soloButtonLight.isOn().onUpdateHardware(state -> midiOut.sendMidi(NOTE_ON, SOLO[idx], state.compareTo(false)));
            HardwareButton soloButton = hardwareSurface.createHardwareButton(format("SOLO_BUTTON_%d", idx));
            soloButton.setBounds(14.0 + idx * 25.0, 116.0, 10.0, 5.0);
            soloButton.setBackgroundLight(soloButtonLight);
            soloButton.pressedAction().setActionMatcher(midiIn.createNoteOnActionMatcher(0, SOLO[idx]));
            soloButton.pressedAction().setBinding(trackCursor.solo().toggleAction());

            OnOffHardwareLight recArmButtonLight = hardwareSurface.createOnOffHardwareLight(format("REC-ARM_BUTTON_LIGHT_%d", idx));
            recArmButtonLight.setOnColor(red);
            recArmButtonLight.isOn().setValueSupplier(trackCursor.arm());
            recArmButtonLight.isOn().onUpdateHardware(state -> midiOut.sendMidi(NOTE_ON, REC_ARM[idx], state.compareTo(false)));
            HardwareButton recArmButton = hardwareSurface.createHardwareButton(format("REC-ARM_BUTTON_%d", idx));
            recArmButton.setBounds(14.0 + idx * 25.0, 132.0, 10.0, 5.0);
            recArmButton.setLabel("REC ARM");
            recArmButton.setBackgroundLight(recArmButtonLight);
            recArmButton.pressedAction().setActionMatcher(midiIn.createNoteOnActionMatcher(0, REC_ARM[idx]));
            recArmButton.pressedAction().setBinding(trackCursor.arm().toggleAction());
        });

        // This button just sends all the controller settings as MIDI.
        // Note that this button only has an effect if immediate takeover is set.
        HardwareButton sendAllButton = hardwareSurface.createHardwareButton("SEND-ALL_BUTTON");
        sendAllButton.setLabel("SEND ALL");
        sendAllButton.setBounds(214.0, 25.0, 10.0, 5.0);

        OnOffHardwareLight bankLeftButtonLight = hardwareSurface.createOnOffHardwareLight("BANK-LEFT_LIGHT");
        bankLeftButtonLight.setOnColor(yellow);
        trackBank.canScrollBackwards().markInterested();
        bankLeftButtonLight.isOn().setValueSupplier(trackBank.canScrollBackwards());
        bankLeftButtonLight.isOn().onUpdateHardware(state -> midiOut.sendMidi(NOTE_ON, BANK_LEFT, state.compareTo(false)));
        HardwareButton bankLeftButton = hardwareSurface.createHardwareButton("BANK-LEFT_BUTTON");
        bankLeftButton.setLabel("BANK LEFT");
        bankLeftButton.setBounds(214.0, 25.0 + 32.0, 10.0, 5.0);
        bankLeftButton.setBackgroundLight(bankLeftButtonLight);
        bankLeftButton.pressedAction().setActionMatcher(midiIn.createNoteOnActionMatcher(0, BANK_LEFT));
        bankLeftButton.pressedAction().setBinding(trackBank.scrollPageBackwardsAction());

        OnOffHardwareLight bankRightButtonLight = hardwareSurface.createOnOffHardwareLight("BANK-RIGHT_LIGHT");
        bankRightButtonLight.setOnColor(yellow);
        trackBank.canScrollForwards().markInterested();
        bankRightButtonLight.isOn().setValueSupplier(trackBank.canScrollForwards());
        bankRightButtonLight.isOn().onUpdateHardware(state -> midiOut.sendMidi(NOTE_ON, BANK_RIGHT, state.compareTo(false)));
        HardwareButton bankRightButton = hardwareSurface.createHardwareButton("BANK-RIGHT_BUTTON");
        bankRightButton.setLabel("BANK RIGHT");
        bankRightButton.setBounds(214.0, 25.0 + 64.0, 10.0, 5.0);
        bankRightButton.setBackgroundLight(bankRightButtonLight);
        bankRightButton.pressedAction().setActionMatcher(midiIn.createNoteOnActionMatcher(0, BANK_RIGHT));
        bankRightButton.pressedAction().setBinding(trackBank.scrollPageForwardsAction());

        HardwareButton soloButton = hardwareSurface.createHardwareButton("SOLO_BUTTON");
        soloButton.setLabel("SOLO");
        // The solo button does not have an LED light.
        soloButton.setBounds(214.0, 25.0 + 91.0, 10.0, 5.0);
        soloButton.pressedAction().setActionMatcher(midiIn.createNoteOnActionMatcher(0, SOLO_MODE));
        // No need for a binding since the function toggle happens inside the MidiMix.

        host.showPopupNotification("Midimix Initialized");
    }

    @Override
    public void exit() {
        getHost().showPopupNotification("Midimix Exited");
    }

    @Override
    public void flush() {
        hardwareSurface.updateHardware();
    }
}
