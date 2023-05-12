package com.symlink.thsrlib.commands;

import com.symlink.thsrlib.commands.fields.Cmd;
import com.symlink.thsrlib.commands.exceptions.ParseErrorException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;

public class ReaderConnectionResponse extends CommandResponse {

    private String model;

    @Override
    public CommandResponse parse(int sn, Cmd cmd, ByteBuffer buf) throws ParseErrorException {
        this.sn = sn;
        this.cmd = cmd;
        model = getString(buf, 20);
        return this;
    }

    public String format() {
        StringWriter out = new StringWriter();
        PrintWriter writer = new PrintWriter(out);
        writer.println("刷卡機型號：" + model);
        return out.toString();
    }

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public int getReturnCode() {
        return 0;
    }

    public String getModel() {
        return model;
    }
}
