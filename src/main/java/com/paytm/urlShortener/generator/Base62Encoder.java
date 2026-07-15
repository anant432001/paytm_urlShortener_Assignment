package com.paytm.urlShortener.generator;

public final class Base62Encoder {
    private static final String BASE62_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = BASE62_CHARACTERS.length();

    private Base62Encoder() {
        // Utility class; prevent object creation.
    }

    public static String encode(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Base62 encoding does not support negative values");
        }

        if (value == 0) {
            return String.valueOf(BASE62_CHARACTERS.charAt(0));
        }

        StringBuilder encodedValue = new StringBuilder();

        while (value > 0) {
            int remainder = (int) (value % BASE);
            encodedValue.append(BASE62_CHARACTERS.charAt(remainder));
            value /= BASE;
        }

        return encodedValue.reverse().toString();
    }
}