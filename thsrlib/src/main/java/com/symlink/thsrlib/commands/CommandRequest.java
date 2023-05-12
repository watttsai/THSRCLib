package com.symlink.thsrlib.commands;

import java.util.Arrays;

public abstract class CommandRequest {

    public abstract byte[] build();

    protected byte[] prependZeros(long num, int digits) {
        assert digits > 0 : "Invalid number of digits";
        byte[] result = new byte[digits];
        Arrays.fill(result, (byte) '0');
        for (int i = digits - 1; i >= 0 && num > 0; i--) {
            result[i] = (byte) ((num % 10) + '0');
            num /= 10;
        }
        return result;
    }

    protected byte[] prependZeros(int num, int digits) {
        assert digits > 0 : "Invalid number of digits";
        byte[] result = new byte[digits];
        Arrays.fill(result, (byte) '0');
        for (int i = digits - 1; i >= 0 && num > 0; i--) {
            result[i] = (byte) ((num % 10) + '0');
            num /= 10;
        }
        return result;
    }

    protected byte[] appendSpaces(String str, int size) {
        byte[] result = new byte[size];
        Arrays.fill(result, (byte) ' ');
        for (int i = 0; i < size && str != null && i < str.length(); i++) {
            result[i] = (byte) str.charAt(i);
        }
        return result;
    }
}
