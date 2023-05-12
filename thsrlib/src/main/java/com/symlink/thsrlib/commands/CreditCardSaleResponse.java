package com.symlink.thsrlib.commands;

import com.symlink.thsrlib.commands.fields.Cmd;
import com.symlink.thsrlib.commands.exceptions.ParseErrorException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;

public class CreditCardSaleResponse extends CommandResponse {

    private String hostId;
    private String amount;
    private String thsrTransId;
    private String smisTransId;
    private String staffId;
    private String responseCode;
    private String invoiceNo;
    private String cardNo;
    private String transDate;
    private String transTime;
    private String approveCode;
    private String terminalId;
    private String merchantId;
    private String rrn;
    private String batchNo;
    private String creditCardType;
    private String posEntryMode;
    private String reserved;

    @Override
    public CommandResponse parse(int sn, Cmd cmd, ByteBuffer buf) throws ParseErrorException {
        this.sn = sn;
        this.cmd = cmd;
        hostId = getString(buf, 1);
        amount = getString(buf, 12);
        thsrTransId = getString(buf, 8);
        smisTransId = getString(buf, 13);
        staffId = getString(buf, 8);
        responseCode = getString(buf, 2);
        invoiceNo = getString(buf, 6);
        cardNo = getString(buf, 19);
        transDate = getString(buf, 6);
        transTime = getString(buf, 6);
        approveCode = getString(buf, 6);
        terminalId = getString(buf, 8);
        merchantId = getString(buf, 15);
        rrn = getString(buf, 12);
        batchNo = getString(buf, 6);
        creditCardType = getString(buf, 2);
        posEntryMode = getString(buf, 2);
        reserved = getString(buf, buf.remaining());
        return this;
    }

    public String format() {
        StringWriter out = new StringWriter();
        PrintWriter writer = new PrintWriter(out);
        writer.println("授權銀行：" + hostId);
        writer.println("交易金額：" + amount);
        writer.println("補票編號：" + thsrTransId);
        writer.println("SMIS 票號：" + smisTransId);
        writer.println("站務員編號：" + staffId);
        writer.println("回應碼：" + responseCode);
        writer.println("調閱編號：" + invoiceNo);
        writer.println("卡號：" + cardNo);
        writer.println("交易日期：" + transDate);
        writer.println("交易時間：" + transTime);
        writer.println("授權號碼：" + approveCode);
        writer.println("端末機代號：" + terminalId);
        writer.println("商店代號：" + merchantId);
        writer.println("交易序號：" + rrn);
        writer.println("批次號碼：" + batchNo);
        writer.println("卡別：" + creditCardType);
        writer.println("過卡方式：" + posEntryMode);
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

    public String getHostId() {
        return hostId;
    }

    public String getAmount() {
        return amount;
    }

    public String getThsrTransId() {
        return thsrTransId;
    }

    public String getSmisTransId() {
        return smisTransId;
    }

    public String getStaffId() {
        return staffId;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public String getCardNo() {
        return cardNo;
    }

    public String getTransDate() {
        return transDate;
    }

    public String getTransTime() {
        return transTime;
    }

    public String getApproveCode() {
        return approveCode;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getRrn() {
        return rrn;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public String getCreditCardType() {
        return creditCardType;
    }

    public String getPosEntryMode() {
        return posEntryMode;
    }

    public String getReserved() {
        return reserved;
    }
}
