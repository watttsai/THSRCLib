package com.symlink.thsrlib.commands;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreditCardVoidRequest extends CommandRequest {

    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
    private byte hostId;
    private long amount;
    private String thsrTransId;
    private String smisTransId;
    private String staffId;
    private int invoiceNo;
    private String cardNo;
    private Date transDatetime;
    private String approveCoe;
    private String cardType;

    public void setHostId(byte hostId) {
        this.hostId = hostId;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public void setThsrTransId(String thsrTransId) {
        this.thsrTransId = thsrTransId;
    }

    public void setSmisTransId(String smisTransId) {
        this.smisTransId = smisTransId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public void setInvoiceNo(int invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public void setTransDatetime(Date transDatetime) {
        this.transDatetime = transDatetime;
    }

    public void setApproveCoe(String approveCoe) {
        this.approveCoe = approveCoe;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    @Override
    public byte[] build() {
        ByteBuffer buf = ByteBuffer.allocate(100);
        buf.put(hostId);
        buf.put(prependZeros(amount, 12));
        buf.put(appendSpaces(thsrTransId, 8));
        buf.put(appendSpaces(smisTransId, 13));
        buf.put(appendSpaces(staffId, 8));
        buf.put(prependZeros(invoiceNo, 6));
        buf.put(appendSpaces(cardNo, 19));
        buf.put(appendSpaces(formatter.format(transDatetime), 12));
        buf.put(appendSpaces(approveCoe, 6));
        buf.put(appendSpaces(cardType, 2));
        return buf.array();
    }
}
