package dev.brighten.anticheat.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class InstantAction {
    private final short startId, endId;
    private final boolean end;

}
