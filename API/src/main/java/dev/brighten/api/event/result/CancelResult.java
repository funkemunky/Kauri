package dev.brighten.api.event.result;

import dev.brighten.api.check.CancelType;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CancelResult {
    private final boolean cancelled;
    private CancelType type;
}
