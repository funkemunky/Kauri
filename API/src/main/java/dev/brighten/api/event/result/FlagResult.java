package dev.brighten.api.event.result;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FlagResult {
    private final boolean cancelled;
}
