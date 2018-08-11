package net.klingt.edirol;

import com.bitwig.extension.api.PlatformType;
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList;
import com.bitwig.extension.controller.ControllerExtensionDefinition;
import com.bitwig.extension.controller.api.ControllerHost;

import java.util.UUID;

import static java.lang.String.format;

public class EdirolPCRExtensionDefinition extends ControllerExtensionDefinition {
    private static final UUID DRIVER_ID = UUID.fromString("253deb05-198c-443c-bec7-5fa0f44da207");

    public EdirolPCRExtensionDefinition() {
    }

    @Override
    public String getName() {
        return "Edirol PCR";
    }

    @Override
    public String getAuthor() {
        return "klingt.net";
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public UUID getId() {
        return DRIVER_ID;
    }

    @Override
    public String getHardwareVendor() {
        return "Edirol";
    }

    @Override
    public String getHardwareModel() {
        return "Edirol PCR";
    }

    @Override
    public int getRequiredAPIVersion() {
        return 4;
    }

    @Override
    public int getNumMidiInPorts() {
        return 3;
    }

    @Override
    public int getNumMidiOutPorts() {
        return 2;
    }

    @Override
    public void listAutoDetectionMidiPortNames(final AutoDetectionMidiPortNamesList list,
            final PlatformType platformType) {
        switch (platformType) {
        case WINDOWS:
            System.err.println(format("Support for platform '%s' is experimental.", platformType.name()));
        case MAC:
            list.add(new String[] { "PCR MIDI IN", "PCR 1", "PCR 2" }, new String[] { "PCR MIDI OUT", "PCR" });
            break;
        case LINUX:
            list.add(new String[] { "PCR MIDI", "PCR 1", "PCR 2" }, new String[] { "PCR MIDI", "PCR 1" });
        }
    }

    @Override
    public EdirolPCRExtension createInstance(final ControllerHost host) {
        return new EdirolPCRExtension(this, host);
    }

    public enum MidiInPorts {
        EXTERNAL(0),
        // keyboard nad left most controls
        ONE(1),
        // top control row (knobs, faders, pads, etc.)
        TWO(2);

        private final int value;

        MidiInPorts(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum MidiOutPorts {
        EXTERNAL(0),
        ONE(1);

        private final int value;

        MidiOutPorts(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
