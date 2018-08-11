package net.klingt.edirol;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;
import com.bitwig.extension.controller.api.CursorRemoteControlsPage;
import com.bitwig.extension.controller.api.CursorTrack;
import com.bitwig.extension.controller.api.DrumPadBank;
import com.bitwig.extension.controller.api.MasterTrack;
import com.bitwig.extension.controller.api.PinnableCursorDevice;
import com.bitwig.extension.controller.api.PopupBrowser;
import com.bitwig.extension.controller.api.RemoteControl;
import com.bitwig.extension.controller.api.TrackBank;
import com.bitwig.extension.controller.api.Transport;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class MidiHandler {
    private static final Number MIDI_RESOLUTION = 128;
    private final Transport transport;
    private final MasterTrack masterTrack;
    private final TrackBank trackBank;
    private final CursorRemoteControlsPage cursorRemoteControlsPage;
    private final Map<HandlerID, Consumer<ShortMidiMessage>> handlers;
    private final PinnableCursorDevice cursorDevice;
    private final PopupBrowser popupBrowser;
    private final DrumPadBank cursorDeviceDrumPads;
    private final CursorTrack cursorTrack;
    private int prevProgramChange = 0;

    public MidiHandler(Transport transport, MasterTrack masterTrack, CursorTrack cursorTrack, TrackBank trackBank, PopupBrowser popupBrowser) {
        this.transport = transport;
        this.masterTrack = masterTrack;
        this.trackBank = trackBank;
        this.cursorTrack = cursorTrack;
        this.cursorDevice = cursorTrack.createCursorDevice();
        cursorDevice.hasDrumPads().markInterested();
        this.cursorDeviceDrumPads = cursorDevice.createDrumPadBank(16);
        this.cursorDeviceDrumPads.scrollPosition().markInterested();
        this.cursorRemoteControlsPage = cursorDevice.createCursorRemoteControlsPage(8);
        this.popupBrowser = popupBrowser;
        popupBrowser.exists().markInterested();

        this.handlers = new HashMap<>();
        registerCCHandlers();
    }

    private void registerCCHandlers() {
        this.handlers.put(HandlerID.of(8, 82), this::handleTransport);
        this.handlers.put(HandlerID.of(13, 82), this::handleTransport);
        this.handlers.put(HandlerID.of(14, 82), this::handleTransport);
        this.handlers.put(HandlerID.of(10, 82), this::handleTransport);

        IntStream.rangeClosed(0, 7).forEach(ch -> this.handlers.put(HandlerID.of(ch, 16), this::handleKnob));
        this.handlers.put(HandlerID.of(0, 18), this::unhandler); // master knob

        IntStream.rangeClosed(0, 7).forEach(ch -> this.handlers.put(HandlerID.of(ch, 17), this::handleFader));
        this.handlers.put(HandlerID.of(1, 18), this::handleMasterFader);

        this.handlers.put(HandlerID.of(0, 19), this::handleCrossfader);

        this.handlers.put(HandlerID.of(0, 82), this::selectPreviousPreset);
        this.handlers.put(HandlerID.of(1, 82), this::commitPreset);
        this.handlers.put(HandlerID.of(2, 82), this::selectNextPreset);

        // pads a1-a9
        IntStream.rangeClosed(0, 7).forEach(ch -> this.handlers.put(HandlerID.of(ch, 80), this::padHandler));
        this.handlers.put(HandlerID.of(0, 83), this::scrollPadsUp);
        // pads b1-b9
        IntStream.rangeClosed(0, 7).forEach(ch -> this.handlers.put(HandlerID.of(ch, 81), this::padHandler));
        this.handlers.put(HandlerID.of(1, 83), this::scrollPadsDown);
    }

    private void scrollPadsUp(ShortMidiMessage msg) {
        if (!cursorDevice.hasDrumPads().get() || isOff(msg)) {
            return;
        }

        cursorDeviceDrumPads.scrollBy(4);
    }

    private void scrollPadsDown(ShortMidiMessage msg) {
        if (!cursorDevice.hasDrumPads().get() || isOff(msg)) {
            return;
        }

        cursorDeviceDrumPads.scrollBy(-4);
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

    private void selectPreviousPreset(ShortMidiMessage msg) {
        if (isOff(msg)) {
            return;
        }

        cursorDevice.browseToReplaceDevice();
        popupBrowser.selectPreviousFile();
    }

    private void commitPreset(ShortMidiMessage msg) {
        if (isOff(msg)) {
            return;
        }

        if (popupBrowser.exists().get()) {
            popupBrowser.commit();
        }
    }

    private void selectNextPreset(ShortMidiMessage msg) {
        if (isOff(msg)) {
            return;
        }

        cursorDevice.browseToReplaceDevice();
        popupBrowser.selectNextFile();
    }

    private void unhandler(ShortMidiMessage msg) {
    }

    public void midiReceivedOnPortOne(int statusByte, int data1, int data2) {
        ShortMidiMessage msg = new ShortMidiMessage(statusByte, data1, data2);
        if (!msg.isProgramChange()) {
            return;
        }
        handleProgramChange(msg);
    }

    public void midiReceivedOnPortTwo(int statusByte, int data1, int data2) {
        ShortMidiMessage msg = new ShortMidiMessage(statusByte, data1, data2);
        handlers.get(HandlerID.of(msg)).accept(msg);
    }

    private void handleProgramChange(ShortMidiMessage msg) {
        if (!msg.isProgramChange()) {
            return;
        }

        cursorDevice.browseToReplaceDevice();
        int delta = msg.getData1() - prevProgramChange;
        if (delta < 0) {
            popupBrowser.selectPreviousFile();
        } else {
            popupBrowser.selectNextFile();
        }
        prevProgramChange = msg.getData1();
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

    private void handleFader(ShortMidiMessage msg) {
        if (!msg.isControlChange() || msg.getData1() != 17) {
            return;
        }

        int channel = msg.getChannel();
        if (channel < 0 || channel > trackBank.getSizeOfBank()) {
            return;
        }

        trackBank.getChannel(channel).getVolume().set(msg.getData2(), MIDI_RESOLUTION);
    }

    private void handleMasterFader(ShortMidiMessage msg) {
        if (!msg.isControlChange() || msg.getChannel() != 1) {
            return;
        }

        masterTrack.getVolume().set(msg.getData2(), MIDI_RESOLUTION);
    }

    private void handleCrossfader(ShortMidiMessage msg) {
        if (!msg.isControlChange() || msg.getData1() != 19) {
            return;
        }

        transport.getCrossfade().set(msg.getData2(), MIDI_RESOLUTION);
    }

    private void handleTransport(ShortMidiMessage msg) {
        if (!msg.isControlChange() || isOff(msg)) {
            return;
        }

        switch (msg.getChannel()) {
            case 8:
                transport.rewind();
                break;
            case 10:
                transport.record();
                break;
            case 13:
                transport.stop();
                break;
            case 14:
                transport.togglePlay();
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
