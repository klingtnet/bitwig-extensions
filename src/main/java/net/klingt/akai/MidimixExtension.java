package net.klingt.akai;

import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.ControllerHost;

public class MidimixExtension extends ControllerExtension {
    private MidiHandler midiHandler;

    protected MidimixExtension(final MidimixExtensionDefinition definition, final ControllerHost host) {
        super(definition, host);
    }

    @Override
    public void init() {
        final ControllerHost host = getHost();

        midiHandler = new MidiHandler(host.createTransport(),
                host.createMasterTrack(0),
                host.getProject().getShownTopLevelTrackGroup().createTrackBank(8, 0, 0, false),
                host.getMidiOutPort(0)
        );

        host.getMidiInPort(0).setMidiCallback(midiHandler);
        host.getMidiInPort(0).setSysexCallback(midiHandler::sysexReceived);


        host.showPopupNotification("Midimix Initialized");
    }

    @Override
    public void exit() {
        getHost().showPopupNotification("Midimix Exited");
    }

    @Override
    public void flush() {
    }
}
