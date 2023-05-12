package com.symlink.thsrlib.commands.fields;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.EnumSet;
import java.util.Objects;

public enum StationId {
    STATION_NANGAN("01", "南港站"),
    STATION_TAIPEI("02", "台北站"),
    STATION_BANQIAO("03", "板橋站"),
    STATION_TAOYUAN("04", "桃園站"),
    STATION_HSINCHU("05", "新竹站"),
    STATION_MIAOLI("06", "苗栗站"),
    STATION_TAICHUNG("07", "台中站"),
    STATION_CHANGHUA("08", "彰化站"),
    STATION_YUNLIN("09", "雲林站"),
    STATION_CHIAYU("10", "嘉義站"),
    STATION_TAINAN("11", "台南站"),
    STATION_ZUOYING("12", "左營站"),
    STATION_UNKNOWN("99", "其他/未在高鐵站進出"),
    ;
    private final String id;
    private final String stationName;

    StationId(String id, String stationName) {
        this.id = id;
        this.stationName = stationName;
    }

    @Nullable
    public static StationId findById(String id) {
        for (StationId s : EnumSet.allOf(StationId.class)) {
            if (Objects.equals(s.id, id)) {
                return s;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return id + "(" + stationName + ")";
    }

    public String getId() {
        return id;
    }

    public String getStationName() {
        return stationName;
    }
}
