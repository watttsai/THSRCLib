package com.symlink.thsrlib.commands;

import com.symlink.thsrlib.commands.fields.Cmd;
import com.symlink.thsrlib.commands.exceptions.ParseErrorException;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public abstract class CommandResponse {

    protected int sn;
    protected Cmd cmd;
    protected int returnCode;

    protected CommandResponse() {
    }

    public abstract CommandResponse parse(int sn, Cmd cmd, ByteBuffer buf) throws ParseErrorException;

    public abstract String format();

    public abstract boolean isError();

    public int getSn() {
        return sn;
    }

    public Cmd getCmd() {
        return cmd;
    }

    public int getReturnCode() {
        return returnCode;
    }

    protected String getString(ByteBuffer buf, int size) {
        byte[] bytes = new byte[size];
        Arrays.fill(bytes, (byte) ' ');
        buf.get(bytes, 0, Math.min(buf.remaining(), size));
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    protected int getInteger(ByteBuffer buf, int width) {
        return Integer.parseInt(getString(buf, width));
    }

    protected long getLong(ByteBuffer buf, int width) {
        return Long.parseLong(getString(buf, width));
    }
}
