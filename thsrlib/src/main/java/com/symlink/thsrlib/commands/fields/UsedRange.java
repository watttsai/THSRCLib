package com.symlink.thsrlib.commands.fields;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class UsedRange {
    private final String usedRangeStr;
    @Nullable
    private final StationId startId;
    @Nullable
    private final StationId endId;

    public UsedRange(String usedRangeStr) {
        Objects.requireNonNull(usedRangeStr);
        if (usedRangeStr.length() != 4) {
            throw new IllegalArgumentException("Invalid length of usedRange: " + usedRangeStr.length());
        }
        this.usedRangeStr = usedRangeStr;
        this.startId = StationId.findById(usedRangeStr.substring(0, 2));
        this.endId = StationId.findById(usedRangeStr.substring(2));
    }

    @NonNull
    @Override
    public String toString() {
        if (Objects.equals(usedRangeStr, "    ")) {
            return "(非高鐵回數/定期票)";
        } else if (startId == null || endId == null || startId == StationId.STATION_UNKNOWN || endId == StationId.STATION_UNKNOWN) {
            return usedRangeStr + "(非高鐵回數/定期票)";
        } else {
            return startId.getId() + endId.getId() + "(" + startId.getStationName() + "到" + endId.getStationName() + ")";
        }
    }
}
