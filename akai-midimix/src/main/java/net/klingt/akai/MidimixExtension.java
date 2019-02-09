package net.klingt.akai;

import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.CursorRemoteControlsPage;
import com.bitwig.extension.controller.api.MidiOut;
import com.bitwig.extension.controller.api.Track;
import com.bitwig.extension.controller.api.TrackBank;
import com.bitwig.extension.controller.api.UserControlBank;

import java.util.stream.IntStream;

import static java.lang.String.format;
import static net.klingt.akai.MidiMix.BANK_LEFT;
import static net.klingt.akai.MidiMix.BANK_RIGHT;
import static net.klingt.akai.MidiMix.MUTE;
import static net.klingt.akai.MidiMix.REC_ARM;
import static net.klingt.akai.MidiMix.SOLO;
import static net.klingt.akai.MidiMix.valueOfIndex;

public class MidimixExtension extends ControllerExtension {

    private MidiOut midiOut;

    protected MidimixExtension(final MidimixExtensionDefinition definition, final ControllerHost host) {
        super(definition, host);
    }

    @Override
    public void init() {
        final ControllerHost host = getHost();
        this.midiOut = host.getMidiOutPort(0);
        TrackBank trackBank = host.getProject()
                .getShownTopLevelTrackGroup()
                .createTrackBank(8, 0, 0, false);
        CursorRemoteControlsPage cursorRemoteControlsPage = host.createCursorTrack(0, 0).createCursorDevice().createCursorRemoteControlsPage(8);
        UserControlBank userControls = host.createUserControls(16);
        IntStream.range(0, 16).forEach(idx -> userControls.getControl(idx).setLabel(this.userControlLabel(idx)));

        MidiHandler midiHandler = new MidiHandler(host.createTransport(),
                host.createMasterTrack(0),
                trackBank,
                cursorRemoteControlsPage,
                userControls
        );
        host.getMidiInPort(0).setMidiCallback(midiHandler);
        host.getMidiInPort(0).setSysexCallback(midiHandler::sysexReceived);

        registerObservers(trackBank);

        host.showPopupNotification("Midimix Initialized");
    }

    private String userControlLabel(int idx) {
        int row = (idx / 8) + 1;
        int num = (idx % 8) + 1;
        return format("Row %d Knob %d", row, num);
    }

    private void registerObservers(TrackBank trackBank) {
        IntStream.range(0, trackBank.getSizeOfBank())
                .filter(i -> trackBank.getChannel(i) != null)
                .forEach(i -> registerChannelObservers(trackBank.getChannel(i), i));

        trackBank.canScrollBackwards().addValueObserver(new ButtonObserver(BANK_LEFT, midiOut));
        trackBank.canScrollForwards().addValueObserver(new ButtonObserver(BANK_RIGHT, midiOut));
    }

    private void registerChannelObservers(Track channel, int index) {
        valueOfIndex(index, MUTE).ifPresent(buttonNote -> channel.getMute().addValueObserver(new ButtonObserver(buttonNote, midiOut)));
        valueOfIndex(index, SOLO).ifPresent(buttonNote -> channel.getSolo().addValueObserver(new ButtonObserver(buttonNote, midiOut)));
        valueOfIndex(index, REC_ARM).ifPresent(buttonNote -> channel.getArm().addValueObserver(new ButtonObserver(buttonNote, midiOut)));
    }

    @Override
    public void exit() {
        getHost().showPopupNotification("Midimix Exited");
    }

    @Override
    public void flush() {
    }
}
