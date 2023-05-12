package com.symlink.thsrlib.commands;

import java.nio.ByteBuffer;

public class CreditCardInquiryRequest extends CommandRequest {

    private long amount;
    private String thsrTransId;
    private String smisTransId;
    private String staffId;

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

    @Override
    public byte[] build() {
        ByteBuffer buf = ByteBuffer.allocate(100);
        buf.put(prependZeros(amount, 12));
        buf.put(appendSpaces(thsrTransId, 8));
        buf.put(appendSpaces(smisTransId, 13));
        buf.put(appendSpaces(staffId, 8));
        return buf.array();
    }
}
