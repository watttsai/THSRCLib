package com.symlink.thsrlib.commands;

import java.util.Arrays;

public class InquireCardSwitchRequest extends CommandRequest {

    private final boolean open;

    public InquireCardSwitchRequest(boolean open) {
        this.open = open;
    }

    public byte[] build() {
        byte[] bytes = new byte[50];
        Arrays.fill(bytes, (byte) ' ');
        if (open) {
            bytes[0] = '1';
        } else {
            bytes[0] = '0';
        }
        return bytes;
    }
}
