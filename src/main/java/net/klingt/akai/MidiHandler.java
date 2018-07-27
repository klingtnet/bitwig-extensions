package net.klingt.akai;

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
    private final CursorRemoteControlsPage cursorRemoteControlsPage;
    private final Map<HandlerID, Consumer<ShortMidiMessage>> handlers;
    private final PinnableCursorDevice cursorDevice;
    private final DrumPadBank cursorDeviceDrumPads;
    private final CursorTrack cursorTrack;

    public MidiHandler(CursorTrack cursorTrack) {
        this.cursorTrack = cursorTrack;
        this.cursorDevice = cursorTrack.createCursorDevice();
        cursorDevice.hasDrumPads().markInterested();
        this.cursorDeviceDrumPads = cursorDevice.createDrumPadBank(16);
        this.cursorDeviceDrumPads.scrollPosition().markInterested();
        this.cursorRemoteControlsPage = cursorDevice.createCursorRemoteControlsPage(8);

        this.handlers = new HashMap<>();
        registerCCHandlers();
    }

    private void registerCCHandlers() {
        IntStream.rangeClosed(20, 27).forEach(cc -> this.handlers.put(HandlerID.of(0, cc), this::handleKnob));
        IntStream.rangeClosed(60, 67).forEach(note -> this.handlers.put(HandlerID.of(9, note), this::padHandler));
    }

    private void padHandler(ShortMidiMessage msg) {
        if (!cursorDevice.hasDrumPads().get() || isOff(msg)) {
            return;
        }

        int offsetPadRowA = (81 - msg.getData1()) * 8;
        int index = msg.getChannel() + offsetPadRowA;
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
        if (!msg.isControlChange() || msg.getData1() != 16) {
            return;
        }

        RemoteControl parameter = cursorRemoteControlsPage.getParameter(msg.getChannel());
        if (parameter == null) {
            return;
        }
        parameter.set(msg.getData2(), MIDI_RESOLUTION);
    }
}
