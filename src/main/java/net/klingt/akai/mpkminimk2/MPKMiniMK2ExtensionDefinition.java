package net.klingt.akai.mpkminimk2;

import com.bitwig.extension.api.PlatformType;
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList;
import com.bitwig.extension.controller.ControllerExtensionDefinition;
import com.bitwig.extension.controller.HardwareDeviceMatcherList;
import com.bitwig.extension.controller.api.ControllerHost;

import java.util.UUID;

import static java.lang.String.format;
import static net.klingt.Constants.VERSION;

public class MPKMiniMK2ExtensionDefinition extends ControllerExtensionDefinition {
    private static final UUID DRIVER_ID = UUID.fromString("99943339-fe1e-45b3-bd12-62b119686908");

    public MPKMiniMK2ExtensionDefinition() {
    }

    @Override
    public String getName() {
        return "Akai MPK mini mk2";
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
        return "Akai";
    }

    @Override
    public String getHardwareModel() {
        return "Akai MPK mini mk2";
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
            case WINDOWS:
                list.add(new String[]{"MPKMini2"}, new String[]{"MPKMini2"});
                break;
            case MAC:
                // TODO: remove, just dont to auto detection.
                System.err.println(format("Support for platform '%s' is experimental.", platformType.name()));
                break;
            case LINUX:
                list.add(new String[]{"MPKmini2 MIDI 1"}, new String[]{"MPKmini2 MIDI 1"});
        }
    }

    public void listHardwareDevices(final HardwareDeviceMatcherList list) {
        // this crashes with "device not found" but the ids are correct.
        //list.add(new UsbDeviceMatcher("Akai MPK mini mk2", "idVendor == 0x2011 && idProduct == 0x0715"));
    }

    @Override
    public MPKMiniMK2Extension createInstance(final ControllerHost host) {
        return new MPKMiniMK2Extension(this, host);
    }
}