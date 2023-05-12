package com.symlink.thsrlib.commands;

import com.symlink.thsrlib.commands.fields.CardType;
import com.symlink.thsrlib.commands.fields.Cmd;
import com.symlink.thsrlib.commands.fields.EntryFlag;
import com.symlink.thsrlib.commands.fields.StationId;
import com.symlink.thsrlib.commands.fields.SystemId;
import com.symlink.thsrlib.commands.fields.UsedRange;
import com.symlink.thsrlib.commands.exceptions.ParseErrorException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.Objects;

public class InquireCardResponse extends CommandResponse {

    private String cardNo;
    private String txnTime;
    private String systemIdStr;
    private SystemId systemId;
    private String stationIdStr;
    private StationId stationId;
    private String entryFlagStr;
    private EntryFlag entryFlag;
    private String usedRangeStr;
    private UsedRange usedRange;
    private String cardTypeStr;
    private CardType cardType;
    private String reserved;

    @Override
    public CommandResponse parse(int sn, Cmd cmd, ByteBuffer buf) throws ParseErrorException {
        this.sn = sn;
        this.cmd = cmd;
        cardNo = getString(buf, 20);
        txnTime = getString(buf, 14);
        systemIdStr = getString(buf, 3);
        systemId = SystemId.findById(systemIdStr);
        stationIdStr = getString(buf, 2);
        stationId = StationId.findById(stationIdStr);
        entryFlagStr = getString(buf, 2);
        entryFlag = EntryFlag.findByCode(entryFlagStr);
        usedRangeStr = getString(buf, 4);
        usedRange = new UsedRange(usedRangeStr);
        cardTypeStr = getString(buf, 2);
        cardType = CardType.findByCode(cardTypeStr);
        reserved = getString(buf, buf.remaining());
        return this;
    }

    public String format() {
        StringWriter out = new StringWriter();
        PrintWriter writer = new PrintWriter(out);
        writer.println("票卡號碼：" + cardNo);
        writer.println("日期時間：" + txnTime);
        writer.println("業者類別：" + Objects.requireNonNullElse(systemId, systemIdStr));
        writer.println("站別：" + Objects.requireNonNullElse(stationId, stationIdStr));
        writer.println("進站/出站：" + Objects.requireNonNullElse(entryFlag, entryFlagStr));
        writer.println("使用區間：" + usedRange);
        writer.println("交易票證公司：" + Objects.requireNonNullElse(cardType, cardTypeStr));
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

    public String getCardNo() {
        return cardNo;
    }

    public String getTxnTime() {
        return txnTime;
    }

    public String getSystemIdStr() {
        return systemIdStr;
    }

    public SystemId getSystemId() {
        return SystemId.findById(systemIdStr);
    }

    public String getStationIdStr() {
        return stationIdStr;
    }

    public StationId getStationId() {
        return StationId.findById(stationIdStr);
    }

    public String getEntryFlagStr() {
        return entryFlagStr;
    }

    public EntryFlag getEntryFlag() {
        return EntryFlag.findByCode(entryFlagStr);
    }

    public String getCardTypeStr() {
        return cardTypeStr;
    }

    public CardType getCardType() {
        return CardType.findByCode(cardTypeStr);
    }

    public String getUsedRangeStr() {
        return usedRangeStr;
    }

    public UsedRange getUsedRange() {
        return new UsedRange(usedRangeStr);
    }

    public String getReserved() {
        return reserved;
    }
}
