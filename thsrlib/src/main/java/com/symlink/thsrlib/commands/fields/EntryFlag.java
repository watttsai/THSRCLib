package com.symlink.thsrlib.commands.fields;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.EnumSet;
import java.util.Objects;

public enum EntryFlag {
    INVALID("00", "最後一筆交易非進出高鐵車站"),
    ENTERED("01", "高鐵已進站，未出站"),
    EXITED("02", "高鐵已出站，未進站"),
    MANUAL_ADJUSTMENT("03", "人工調整"),
    ;
    private final String code;
    private final String description;

    EntryFlag(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Nullable
    public static EntryFlag findByCode(String code) {
        for (EntryFlag flag : EnumSet.allOf(EntryFlag.class)) {
            if (Objects.equals(flag.code, code)) {
                return flag;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return code + "(" + description + ")";
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
