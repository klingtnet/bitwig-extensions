package net.klingt.akai.mpkminimk2;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;
import com.bitwig.extension.controller.api.CursorRemoteControlsPage;
import com.bitwig.extension.controller.api.CursorTrack;
import com.bitwig.extension.controller.api.DrumPadBank;
import com.bitwig.extension.controller.api.PinnableCursorDevice;
import com.bitwig.extension.controller.api.RemoteControl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class MidiHandler {
    private static final Number MIDI_RESOLUTION = 128;
    private static int KNOB_OFFSET = 20;
    private static int PAD_OFFSET = 0;
    private final CursorRemoteControlsPage cursorRemoteControlsPage;
    private final Map<HandlerID, Consumer<ShortMidiMessage>> handlers;
    private final PinnableCursorDevice cursorDevice;
    private final DrumPadBank cursorDeviceDrumPads;
    private final CursorTrack cursorTrack;

    public MidiHandler(CursorTrack cursorTrack) {
        this.cursorTrack = cursorTrack;
        this.cursorDevice = cursorTrack.createCursorDevice();
        cursorDevice.hasDrumPads().markInterested();
        this.cursorDeviceDrumPads = cursorDevice.createDrumPadBank(8);
        this.cursorDeviceDrumPads.scrollPosition().markInterested();
        this.cursorRemoteControlsPage = cursorDevice.createCursorRemoteControlsPage(8);

        this.handlers = new HashMap<>();
        registerCCHandlers();
    }

    private void registerCCHandlers() {
        IntStream.rangeClosed(KNOB_OFFSET, KNOB_OFFSET + 7).forEach(cc -> this.handlers.put(HandlerID.of(0, cc), this::handleKnob));
        IntStream.rangeClosed(PAD_OFFSET, PAD_OFFSET + 7).forEach(cc -> this.handlers.put(HandlerID.of(9, cc), this::padHandler));
    }

    private void padHandler(ShortMidiMessage msg) {
        if (!cursorDevice.hasDrumPads().get() || isOff(msg)) {
            return;
        }

        int index = msg.getData1();
        cursorTrack.playNote(cursorDeviceDrumPads.scrollPosition().get() + index, msg.getData2());
    }

    private boolean isOff(ShortMidiMessage msg) {
        return msg.getData2() == 0;
    }

    void midiReceived(int statusByte, int data1, int data2) {
        ShortMidiMessage msg = new ShortMidiMessage(statusByte, data1, data2);
        HandlerID id = HandlerID.of(msg);
        if (handlers.containsKey(id)) {
            handlers.get(id).accept(msg);
        }
    }

    private void handleKnob(ShortMidiMessage msg) {
        if (!msg.isControlChange()) {
            return;
        }

        int index = msg.getData1() - KNOB_OFFSET;
        if (!inClosedRange(index, 0, cursorRemoteControlsPage.getParameterCount())) {
            return;
        }
        RemoteControl parameter = cursorRemoteControlsPage.getParameter(index);
        if (parameter == null) {
            return;
        }
        parameter.set(msg.getData2(), MIDI_RESOLUTION);
    }

    private boolean inClosedRange(int data1, int low, int high) {
        return data1 >= low && data1 <= high;
    }
}
