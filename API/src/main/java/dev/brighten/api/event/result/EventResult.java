package dev.brighten.api.event.result;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public abstract class EventResult {
    private boolean cancelled;
}
