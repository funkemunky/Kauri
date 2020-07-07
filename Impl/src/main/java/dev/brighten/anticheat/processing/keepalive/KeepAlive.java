package dev.brighten.anticheat.processing.keepalive;

import cc.funkemunky.api.Atlas;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.MiscUtils;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class KeepAlive {

    public final int start;
    public final int id;
    public long startStamp;

    public KeepAlive(int start) {
        this.start = start;
        id = (int)(System.nanoTime() / 1000000L) + start;
    }

    public final Map<UUID, KAReceived> receivedKeepalive = new HashMap<>();

    protected void received(ObjectData data) {
        receivedKeepalive.put(data.uuid, new KAReceived(data, Kauri.INSTANCE.keepaliveProcessor.tick));
    }

    public Optional<KAReceived> getReceived(UUID uuid) {
        return Optional.ofNullable(receivedKeepalive.getOrDefault(uuid, null));
    }

    @RequiredArgsConstructor
    public static class KAReceived {
        public final ObjectData data;
        public final int stamp;
        public long receivedStamp;
    }
}
