package io.nikov;

import java.util.concurrent.ThreadLocalRandom;

public class EnumUtils {
    public static <T extends Enum<?>> T getRandomEnum(Class<T> enumClass) {
        T[] values = enumClass.getEnumConstants();
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }
}
