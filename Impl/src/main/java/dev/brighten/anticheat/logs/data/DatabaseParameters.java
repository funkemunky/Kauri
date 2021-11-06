package dev.brighten.anticheat.logs.data;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DatabaseParameters {
    private int skip = -1, limit = -1;
    private long timeFrom = -1, timeTo = -1;
}
