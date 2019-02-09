package net.klingt.edirol;

import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.MidiOut;
import com.bitwig.extension.controller.api.UserControlBank;
import net.klingt.edirol.EdirolPCRExtensionDefinition.MidiInPorts;
import net.klingt.edirol.EdirolPCRExtensionDefinition.MidiOutPorts;

public class EdirolPCRExtension extends ControllerExtension {
    static final int HOLD_PEDAL_IDX = 0;
    static final int EXPRESSION_PEDAL_IDX = 1;

    public static String[] MIDI_MASKS = new String[]{"8?????" // Note On
            , "9?????" // Note Off
            , "D?????" // Channel Pressue / After-Touch
            , "E?????"};
    private MidiOut midiOut;

    protected EdirolPCRExtension(final EdirolPCRExtensionDefinition definition, final ControllerHost host) {
        super(definition, host);
    }

    @Override
    public void init() {
        final ControllerHost host = getHost();

        UserControlBank pedals = host.createUserControls(2);
        pedals.getControl(HOLD_PEDAL_IDX).setLabel("Hold");
        pedals.getControl(EXPRESSION_PEDAL_IDX).setLabel("Expression");

        this.midiOut = host.getMidiOutPort(MidiOutPorts.ONE.getValue());
        MidiHandler midiHandler = new MidiHandler(host.createTransport()
                , host.createMasterTrack(0)
                , host.createCursorTrack(0, 0)
                , host.createTrackBank(8, 0, 0)
                , host.createPopupBrowser()
                , pedals
        );

        host.getMidiInPort(MidiInPorts.ONE.getValue()).createNoteInput("PCR-KEYS", MIDI_MASKS);
        host.getMidiInPort(MidiInPorts.ONE.getValue()).setMidiCallback(midiHandler::midiReceivedOnPortOne);
        host.getMidiInPort(MidiInPorts.TWO.getValue()).setMidiCallback(midiHandler::midiReceivedOnPortTwo);
        host.getMidiInPort(MidiInPorts.EXTERNAL.getValue()).setSysexCallback(midiHandler::sysexReceived);

        host.showPopupNotification("Edirol PCR Initialized");
    }

    @Override
    public void exit() {
    }

    @Override
    public void flush() {
    }
}
