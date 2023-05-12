package com.symlink.thsrlib.commands.fields;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.EnumSet;
import java.util.Objects;

public enum SystemId {
    SYSTEM_THSR("001", "高鐵"),
    SYSTEM_TRTC("002", "台北捷運"),
    SYSTEM_KRTC("003", "高雄捷運"),
    SYSTEM_TAOYUAN_MRT("004", "桃園捷運"),
    SYSTEM_TRA("005", "台鐵"),
    SYSTEM_OTHERS("999", "客運/公車或其他交通業者"),
    ;
    private final String id;
    private final String systemName;

    SystemId(String id, String systemName) {
        this.id = id;
        this.systemName = systemName;
    }

    @Nullable
    public static SystemId findById(String id) {
        for (SystemId s : EnumSet.allOf(SystemId.class)) {
            if (Objects.equals(s.id, id)) {
                return s;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return id + "(" + systemName + ")";
    }

    public String getId() {
        return id;
    }

    public String getSystemName() {
        return systemName;
    }
}
