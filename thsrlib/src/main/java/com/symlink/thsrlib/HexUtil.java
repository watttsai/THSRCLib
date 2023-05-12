package com.symlink.thsrlib;

/**
 * A utility class for encoding and decoding hexadecimal strings and byte arrays.
 * Created by Manson on 2017/7/19.
 */
public class HexUtil {
    /**
     * An array of characters for encoding byte arrays into lowercase hexadecimal strings.
     */
    private static final char[] DIGITS_LOWER = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f'
    };

    /**
     * An array of characters for encoding byte arrays into uppercase hexadecimal strings.
     */
    private static final char[] DIGITS_UPPER = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'
    };

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private HexUtil() {
    }

    /**
     * Encodes a byte array into a lowercase hexadecimal string.
     *
     * @param bytes The byte array to encode.
     * @return the hexadecimal string representation of the byte array
     */
    public static char[] encodeHex(byte[] bytes) {
        return encodeHex(bytes, true);
    }

    /**
     * Encodes a subset of a byte array into a lowercase hexadecimal string.
     *
     * @param bytes  The byte array to encode.
     * @param offset The starting index in the byte array.
     * @param length The number of bytes to encode.
     * @return the hexadecimal string representation of the byte array
     */
    public static char[] encodeHex(byte[] bytes, int offset, int length) {
        return encodeHex(bytes, offset, length, true);
    }

    /**
     * Encodes a byte array into a hexadecimal string,using either uppercase or lowercase
     * characters.
     *
     * @param bytes       The byte array to encode.
     * @param toLowerCase If true, encode using lowercase characters;
     *                    otherwise, use uppercase characters.
     * @return the hexadecimal string representation of the byte array
     */
    public static char[] encodeHex(byte[] bytes, boolean toLowerCase) {
        return encodeHex(bytes, 0, bytes.length, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    /**
     * Encodes a subset of a byte array into a hexadecimal string,
     * using either uppercase or lowercase characters.
     *
     * @param bytes       The byte array to encode.
     * @param offset      The starting index in the byte array.
     * @param length      The number of bytes to encode.
     * @param toLowerCase If true, encode using lowercase characters;
     *                    otherwise, use uppercase characters.
     * @return the hexadecimal string representation of the byte array
     */
    public static char[] encodeHex(byte[] bytes, int offset, int length, boolean toLowerCase) {
        return encodeHex(bytes, offset, length, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    /**
     * Encodes a byte array into a hexadecimal string,
     * using a custom character set.
     *
     * @param bytes    The byte array to encode.
     * @param offset   The starting index in the byte array.
     * @param length   The number of bytes to encode.
     * @param toDigits The character set to use for encoding.
     * @return the hexadecimal string representation of the byte array
     */
    protected static char[] encodeHex(byte[] bytes, int offset, int length, char[] toDigits) {
        if (bytes == null) {
            return new char[0];
        }
        char[] out = new char[length << 1];
        int j = 0;
        for (int i = offset; i < offset + length; i++) {
            out[j++] = toDigits[(0xF0 & bytes[i]) >>> 4];
            out[j++] = toDigits[0xF & bytes[i]];
        }
        return out;
    }

    /**
     * Encodes the given byte array as a hexadecimal string.
     *
     * @param bytes the byte array to encode
     * @return the hexadecimal string representation of the byte array
     */
    public static String encodeHexStr(byte[] bytes) {
        return encodeHexStr(bytes, 0, bytes.length);
    }

    /**
     * Encodes a section of the given byte array as a hexadecimal string.
     *
     * @param bytes  the byte array to encode
     * @param offset the starting index of the section to encode
     * @param length the length of the section to encode
     * @return the hexadecimal string representation of the byte array
     */
    public static String encodeHexStr(byte[] bytes, int offset, int length) {
        return encodeHexStr(bytes, offset, length, true);
    }

    /**
     * Encodes the given byte array as a hexadecimal string,
     * with the option to use lowercase characters.
     *
     * @param bytes       the byte array to encode
     * @param toLowerCase whether to use lowercase characters in the output
     * @return the hexadecimal string representation of the byte array
     */
    public static String encodeHexStr(byte[] bytes, boolean toLowerCase) {
        return encodeHexStr(bytes, 0, bytes.length, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    /**
     * Encodes a section of the given byte array as a hexadecimal string,
     * with the option to use lowercase characters.
     *
     * @param bytes       the byte array to encode
     * @param offset      the starting index of the section to encode
     * @param length      the length of the section to encode
     * @param toLowerCase whether to use lowercase characters in the output
     * @return the hexadecimal string representation of the byte array
     */
    public static String encodeHexStr(byte[] bytes, int offset, int length, boolean toLowerCase) {
        return encodeHexStr(bytes, offset, length, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    /**
     * Encodes the given byte array as a hexadecimal string, using the
     * specified character set.
     *
     * @param bytes    the byte array to encode
     * @param offset   the starting index of the section to encode
     * @param length   the length of the section to encode
     * @param toDigits the character set to use for the output
     * @return the hexadecimal string representation of the byte array
     */
    protected static String encodeHexStr(byte[] bytes, int offset, int length, char[] toDigits) {
        return new String(encodeHex(bytes, offset, length, toDigits));
    }

    /**
     * Decodes the given hexadecimal string into a byte array.
     *
     * @param data the hexadecimal string to decode
     * @return the byte array decoded from the hexadecimal string
     * @throws RuntimeException if the input string has an odd number of characters
     *                          or contains an illegal hexadecimal character
     */
    @SuppressWarnings("squid:S112")
    public static byte[] decodeHex(char[] data) {
        if ((data.length & 0x1) != 0) {
            throw new RuntimeException("Odd number of characters.");
        }
        byte[] out = new byte[data.length >> 1];
        int j = 0;
        for (int i = 0; i < data.length; i += 2) {
            int f = toDigit(data[i], i) << 4 | toDigit(data[i + 1], i + 1);
            out[j++] = (byte) (f & 0xFF);
        }
        return out;
    }

    /**
     * Convert a hexadecimal character to its corresponding integer value.
     *
     * @param ch    the hexadecimal character to convert
     * @param index the index of the character in the string
     * @return the corresponding integer value of the hexadecimal character
     * @throws RuntimeException if the input character is not a valid hexadecimal character
     */
    @SuppressWarnings("squid:S112")
    protected static int toDigit(char ch, int index) {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new RuntimeException("Illegal hexadecimal character " + ch +
                    " at index " + index);
        }
        return digit;
    }

    /**
     * Converts a hexadecimal string to a byte array.
     *
     * @param hexString the input hexadecimal string
     * @return the corresponding byte array
     */
    public static byte[] hexStringToByteArray(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return new byte[0];
        }
        hexString = hexString.replace(" ", "");
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4);
            d[i] |= (charToByte(hexChars[pos + 1]) & 0xff);
        }
        return d;
    }

    /**
     * Converts a character to a byte value based on its hexadecimal representation.
     *
     * @param c the input character
     * @return the corresponding byte value
     */
    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * Extracts a single byte of input from a byte array and returns it as a
     * hexadecimal string.
     *
     * @param input    the input byte array
     * @param position the index of the byte to extract
     * @return the hexadecimal string representation of the extracted byte
     */
    public static String extractData(byte[] input, int position) {
        return encodeHexStr(new byte[]{input[position]});
    }

    /**
     * Converts a byte array to an integer value.
     *
     * @param input the input byte array
     * @return the corresponding integer value
     */
    public static int byteArrToInteger(byte[] input) {
        return byteArrToInteger(input, 0, input.length);
    }

    /**
     * Converts a portion of a byte array to an integer value.
     *
     * @param bytes  the input byte array
     * @param offset the starting index of the portion to convert
     * @param length the length of the portion to convert
     * @return the corresponding integer value
     */
    public static int byteArrToInteger(byte[] bytes, int offset, int length) {
        int value = 0;
        for (int i = offset + length - 1; i >= offset; i--) {
            value = (value << 8) | (bytes[i] & 0xff);
        }
        return value;
    }

    /**
     * Converts an integer value to a byte array of specified length.
     *
     * @param value   the integer value to convert
     * @param byteLen the length of the result of the byte array
     * @return the corresponding byte array
     */
    public static byte[] integerToByteArr(long value, int byteLen) {
        byte[] convertedByteArr = new byte[byteLen];
        for (int i = 0; i < convertedByteArr.length; i++) {
            convertedByteArr[i] = (byte) (value >> 8 * i & 0xFFL);
        }
        return convertedByteArr;
    }

    /**
     * Swaps the order of the first two bytes in the input byte array.
     *
     * @param input the input byte array
     */
    public static void ByteArraySwap(byte[] input) {
        reversed(input, 0, 1);
    }

    /**
     * Reverses the order of the bytes in the input byte array.
     *
     * @param input the input byte array
     */
    public static void reversed(byte[] input) {
        reversed(input, 0, input.length);
    }

    /**
     * Reverses the order of the bytes in the input byte array between the
     * specified indices (inclusive).
     *
     * @param bytes     the input byte array
     * @param fromIndex the index of the first byte to be reversed
     * @param toIndex   the index of the last byte to be reversed
     */
    public static void reversed(byte[] bytes, int fromIndex, int toIndex) {
        int length = (toIndex - fromIndex + 1) >> 1;
        for (int i = 0; i < length; i++) {
            byte tmp = bytes[fromIndex + i];
            bytes[fromIndex + i] = bytes[toIndex - i];
            bytes[toIndex - i] = tmp;
        }
    }

    /**
     * Computes the bitwise XOR of all the bytes in the input byte array.
     *
     * @param input the input byte array
     * @return the result of the bitwise XOR
     */
    public static byte ByteArrayXOR(byte[] input) {
        return ByteArrayXOR(input, 0, input.length);
    }

    /**
     * Computes the bitwise XOR of a subset of the bytes in the input byte array
     * between the specified indices (inclusive).
     *
     * @param input  the input byte array
     * @param offset the index of the first byte to be computed
     * @param length the number of bytes to be computed
     * @return the result of the bitwise XOR
     */
    public static byte ByteArrayXOR(byte[] input, int offset, int length) {
        byte result = 0;
        for (int i = offset; i < offset + length; i++) {
            result = (byte) (result ^ input[i]);
        }
        return result;
    }

    /**
     * Converts the input byte array to a string using the ASCII character encoding.
     *
     * @param input the input byte array
     * @return the result of the string
     */
    public static String ByteArrayToStringAscii(byte[] input) {
        return new String(input);
    }
}
