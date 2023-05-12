package com.symlink.thsrlib.commands;

import com.symlink.thsrlib.commands.fields.Cmd;
import com.symlink.thsrlib.commands.exceptions.ParseErrorException;

import java.nio.ByteBuffer;

public class SuccessResponse extends CommandResponse {

    private final String message;

    public SuccessResponse(int sn, Cmd cmd, String message) {
        this.sn = sn;
        this.cmd = cmd;
        this.message = message;
    }

    @Override
    public CommandResponse parse(int sn, Cmd cmd, ByteBuffer buf) throws ParseErrorException {
        this.sn = sn;
        this.cmd = cmd;
        return this;
    }

    @Override
    public String format() {
        return message;
    }

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public int getReturnCode() {
        return 0;
    }
}
