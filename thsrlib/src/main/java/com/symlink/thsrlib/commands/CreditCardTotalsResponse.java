package com.symlink.thsrlib.commands;

import com.symlink.thsrlib.commands.exceptions.ParseErrorException;
import com.symlink.thsrlib.commands.fields.Cmd;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;

public class CreditCardTotalsResponse extends CommandResponse {

    private int creditSaleCount;
    private long creditSaleAmount;
    private int amexSaleCount;
    private long amexSaleAmount;
    private int cupSaleCount;
    private long cupSaleAmount;
    private int saleTotalCount;
    private long saleTotalAmount;
    private int creditVoidCount;
    private long creditVoidAmount;
    private int amexVoidCount;
    private long amexVoidAmount;
    private int cupVoidCount;
    private long cupVoidAmount;
    private int voidTotalCount;
    private long voidTotalAmount;
    private String reserved;

    @Override
    public CommandResponse parse(int sn, Cmd cmd, ByteBuffer buf) throws ParseErrorException {
        this.sn = sn;
        this.cmd = cmd;
        creditSaleCount = getInteger(buf, 6);
        creditSaleAmount = getLong(buf, 12);
        amexSaleCount = getInteger(buf, 6);
        amexSaleAmount = getLong(buf, 12);
        cupSaleCount = getInteger(buf, 6);
        cupSaleAmount = getLong(buf, 12);
        saleTotalCount = getInteger(buf, 6);
        saleTotalAmount = getLong(buf, 12);
        creditVoidCount = getInteger(buf, 6);
        creditVoidAmount = getLong(buf, 12);
        amexVoidCount = getInteger(buf, 6);
        amexVoidAmount = getLong(buf, 12);
        cupVoidCount = getInteger(buf, 6);
        cupVoidAmount = getLong(buf, 12);
        voidTotalCount = getInteger(buf, 6);
        voidTotalAmount = getLong(buf, 12);
        reserved = getString(buf, buf.remaining());
        return this;
    }

    public String format() {
        StringWriter out = new StringWriter();
        PrintWriter writer = new PrintWriter(out);
        writer.println("信用卡補票總筆數：" + creditSaleCount);
        writer.println("信用卡補票總金額：" + creditSaleAmount);
        writer.println("AMEX補票總筆數：" + amexSaleCount);
        writer.println("AMEX補票總金額：" + amexSaleAmount);
        writer.println("銀聯卡補票總筆數：" + cupSaleCount);
        writer.println("銀聯卡補票總金額：" + cupSaleAmount);
        writer.println("補票合計筆數：" + saleTotalCount);
        writer.println("補票合計金額：" + saleTotalAmount);

        writer.println("信用卡取消總筆數：" + creditVoidCount);
        writer.println("信用卡取消總金額：" + creditVoidAmount);
        writer.println("AMEX取消總筆數：" + amexVoidCount);
        writer.println("AMEX取消總金額：" + amexVoidAmount);
        writer.println("銀聯卡取消總筆數：" + cupVoidCount);
        writer.println("銀聯卡取消總金額：" + cupVoidAmount);
        writer.println("取消合計筆數：" + voidTotalCount);
        writer.println("取消合計金額：" + voidTotalAmount);
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

    public int getCreditSaleCount() {
        return creditSaleCount;
    }

    public long getCreditSaleAmount() {
        return creditSaleAmount;
    }

    public int getAmexSaleCount() {
        return amexSaleCount;
    }

    public long getAmexSaleAmount() {
        return amexSaleAmount;
    }

    public int getCupSaleCount() {
        return cupSaleCount;
    }

    public long getCupSaleAmount() {
        return cupSaleAmount;
    }

    public int getSaleTotalCount() {
        return saleTotalCount;
    }

    public long getSaleTotalAmount() {
        return saleTotalAmount;
    }

    public int getCreditVoidCount() {
        return creditVoidCount;
    }

    public long getCreditVoidAmount() {
        return creditVoidAmount;
    }

    public int getAmexVoidCount() {
        return amexVoidCount;
    }

    public long getAmexVoidAmount() {
        return amexVoidAmount;
    }

    public int getCupVoidCount() {
        return cupVoidCount;
    }

    public long getCupVoidAmount() {
        return cupVoidAmount;
    }

    public int getVoidTotalCount() {
        return voidTotalCount;
    }

    public long getVoidTotalAmount() {
        return voidTotalAmount;
    }

    public String getReserved() {
        return reserved;
    }
}
