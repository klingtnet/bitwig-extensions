package net.klingt.edirol.pcr;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;

import java.util.Objects;

public class HandlerID {
    private final int channel;
    private final int ccNumber;

    private HandlerID(int channel, int ccNumber) {
        this.channel = channel;
        this.ccNumber = ccNumber;
    }

    public static HandlerID of(int channel, int ccNumber) {
        return new HandlerID(channel, ccNumber);
    }

    public static HandlerID of(ShortMidiMessage msg) {
        if (msg == null || !msg.isControlChange()) {
            return new HandlerID(-1, -1);
        }
        return new HandlerID(msg.getChannel(), msg.getData1());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HandlerID handlerID = (HandlerID) o;
        return channel == handlerID.channel &&
                ccNumber == handlerID.ccNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel, ccNumber);
    }
}

