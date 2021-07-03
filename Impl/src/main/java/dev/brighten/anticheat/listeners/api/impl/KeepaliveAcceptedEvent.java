package dev.brighten.anticheat.listeners.api.impl;

import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.processing.keepalive.KeepAlive;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class KeepaliveAcceptedEvent {

    private final ObjectData data;
    private final KeepAlive keepalive;
}
