package dev.brighten.api.check;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum DevStage {
    RELEASE("Release", true),
    BETA("Beta", false),
    ALPHA("Alpha", false);

    final String formattedName;
    final boolean release;

}
