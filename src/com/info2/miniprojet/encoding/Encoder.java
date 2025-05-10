package com.info2.miniprojet.encoding;

public interface Encoder {
    /**
     * Encodes a string.
     * @param input The string to encode.
     * @return The encoded string.
     */
    String encode(String input);
    /**
     * Gets a user-friendly name for this encoder
     * @return The name identifier.
     */
    String getName();
}
