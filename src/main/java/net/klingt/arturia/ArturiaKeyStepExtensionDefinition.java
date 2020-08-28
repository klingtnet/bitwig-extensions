package net.klingt.arturia;

import com.bitwig.extension.api.PlatformType;
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList;
import com.bitwig.extension.controller.ControllerExtensionDefinition;
import com.bitwig.extension.controller.api.ControllerHost;

import java.util.UUID;

import static java.lang.String.format;
import static net.klingt.Constants.VERSION;

public class ArturiaKeyStepExtensionDefinition extends ControllerExtensionDefinition {
    private static final UUID DRIVER_ID = UUID.fromString("895c9e8a-2d9d-4044-8372-5f72bd464985");

    public ArturiaKeyStepExtensionDefinition() {
    }

    @Override
    public String getName() {
        return "KeyStep";
    }

    @Override
    public String getAuthor() {
        return "klingt.net";
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public UUID getId() {
        return DRIVER_ID;
    }

    @Override
    public String getHardwareVendor() {
        return "Arturia";
    }

    @Override
    public String getHardwareModel() {
        return "KeyStep";
    }

    @Override
    public int getRequiredAPIVersion() {
        return 10;
    }

    @Override
    public int getNumMidiInPorts() {
        return 1;
    }

    @Override
    public int getNumMidiOutPorts() {
        return 1;
    }

    @Override
    public void listAutoDetectionMidiPortNames(final AutoDetectionMidiPortNamesList list,
                                               final PlatformType platformType) {
        switch (platformType) {
            case MAC:
                System.err.println(format("Support for platform '%s' is experimental.", platformType.name()));
                break;
            case WINDOWS:
                list.add(new String[]{"Arturia KeyStep 32"}, new String[]{"Arturia KeyStep 32"});
                break;
            case LINUX:
                list.add(new String[]{"Arturia KeyStep 32 MIDI 1"}, new String[]{"Arturia KeyStep 32 MIDI 1"});
        }
    }

    @Override
    public ArturiaKeyStepExtension createInstance(final ControllerHost host) {
        return new ArturiaKeyStepExtension(this, host);
    }
}