package net.klingt.akai;

import java.util.Optional;
import java.util.stream.IntStream;

public class MidiMix {
    static final int[] KNOBS = new int[]{16, 17, 18, 20, 21, 22, 24, 25, 26, 28, 29, 30, 46, 47, 48, 50, 51, 52, 54, 55, 56, 58, 59, 60};
    static final int[] KNOBS_TOP_ROW = new int[]{16, 20, 24, 28, 46, 50, 54, 58};
    static final int[] KNOBS_USER = new int[]{17, 18, 21, 22, 25, 26, 29, 30, 47, 48, 51, 52, 55, 56, 59, 60};
    static final int[] FADERS = new int[]{19, 23, 27, 31, 49, 53, 57, 61};
    static final int MASTER_FADER = 62;
    static final int[] MUTE = new int[]{1, 4, 7, 10, 13, 16, 19, 22};
    static final int[] SOLO = new int[]{2, 5, 8, 11, 14, 17, 20, 23};
    static final int[] REC_ARM = new int[]{3, 6, 9, 12, 15, 18, 21, 24};
    static final int BANK_LEFT = 25;
    static final int BANK_RIGHT = 26;
    static final int SOLO_MODE = 27;
    static final int MIDI_RESOLUTION = 128;

    static boolean isIn(int y, int[] array) {
        return IntStream.of(array).anyMatch(x -> x == y);
    }

    static boolean isNotIn(int y, int[] array) {
        return !isIn(y, array);
    }

    static Optional<Integer> indexOf(int value, int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == value) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    static Optional<Integer> valueOfIndex(int index, int[] arr) {
        if (index < 0 || index >= arr.length) {
            return Optional.empty();
        }
        return Optional.of(arr[index]);
    }
}
