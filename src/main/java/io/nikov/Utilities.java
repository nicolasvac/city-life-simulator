package io.nikov;

import java.security.SecureRandom;

public class Utilities {
    public static <T extends Enum<?>> T getRandomEnum(Class<T> enumClass) {
        T[] values = enumClass.getEnumConstants();
        return values[new SecureRandom().nextInt(values.length)];
    }

    public static int getRandomInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Min value must be less than or equal to Max value.");
        }

        return (int) (Math.random() * (max - min + 1)) + min;
    }
}
