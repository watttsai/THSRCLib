package com.symlink.thsrlib.commands.fields;

import androidx.annotation.Nullable;

import java.util.EnumSet;
import java.util.Objects;

public enum Cmd {
    CONNECT(0x0001, "刷卡機連線"),
    SETTING(0x0101, "刷卡機設定"),
    READER_INQUIRY_SWITCH(0x0201, "票卡查詢開關"),
    READER_INQUIRY_RESULT(0x0202, "票卡查詢結果"),
    PRINT(0x0301, "資料列印"),
    CREDITCARD_SALE(0x0401, "信用卡補票交易結果"),
    CREDITCARD_SALE_RESULT(0x0402, "啟動信用卡補票交易"),
    CREDITCARD_VOID(0x0403, "啟動信用卡取消交易"),
    CREDITCARD_VOID_RESULT(0x0404, "信用卡取消交易結果"),
    CREDITCARD_INQUIRY(0x0501, "查詢信用卡交易資料"),
    CREDITCARD_TOTALS(0x0502, "查詢信用卡交易總帳");

    private final int code;
    private final String name;

    Cmd(int code, String name) {
        this.code = code;
        this.name = name;
    }

    @Nullable
    public static Cmd findByCode(int code) {
        for (Cmd cmd : EnumSet.allOf(Cmd.class)) {
            if (Objects.equals(cmd.code, code)) {
                return cmd;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
