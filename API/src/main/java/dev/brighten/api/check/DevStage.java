package dev.brighten.api.check;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum DevStage {
    STABLE("Stable", true),
    BETA("Beta", false),
    CANARY("Canary", false);

    final String formattedName;
    final boolean release;

}
