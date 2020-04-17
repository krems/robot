package ru.oval.util;

public class Utils {
    public static boolean isEmpty(final String string) {
        return string == null || string.isEmpty();
    }

    public static boolean isBlank(final String string) {
        return string == null || string.isBlank();
    }
}
