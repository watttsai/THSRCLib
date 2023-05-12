package com.symlink.thsrlib.commands.fields;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.EnumSet;
import java.util.Objects;

public enum CardType {
    CARD_EASYCARD("01", "悠遊卡"),
    CARD_IPASS("02", "一卡通"),
    CARD_THSR("10", "高鐵"),
    ;
    private final String code;
    private final String cardTypeName;

    CardType(String code, String cardTypeName) {
        this.code = code;
        this.cardTypeName = cardTypeName;
    }

    @Nullable
    public static CardType findByCode(String code) {
        for (CardType cardType : EnumSet.allOf(CardType.class)) {
            if (Objects.equals(cardType.code, code)) {
                return cardType;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return code + "(" + cardTypeName + ")";
    }

    public String getCode() {
        return code;
    }

    public String getCardTypeName() {
        return cardTypeName;
    }
}
