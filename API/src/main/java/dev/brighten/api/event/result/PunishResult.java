package dev.brighten.api.event.result;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class PunishResult extends EventResult {
    private String broadcastMessage;
    private List<String> commands;
}
