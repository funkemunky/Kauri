package dev.brighten.anticheat.processing.keepalive;

import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class KeepAlive {

    public final long start;
    public final short id;
    public long startStamp;

    public KeepAlive(long start, short id) {
        this.start = start;
        this.id = id;
    }

    public final Map<UUID, KAReceived> receivedKeepalive = new HashMap<>();

    public void received(ObjectData data) {
        receivedKeepalive.put(data.uuid, new KAReceived(data, Kauri.INSTANCE.keepaliveProcessor.tick));
    }

    public Optional<KAReceived> getReceived(UUID uuid) {
        return Optional.ofNullable(receivedKeepalive.getOrDefault(uuid, null));
    }

    @RequiredArgsConstructor
    public static class KAReceived {
        public final ObjectData data;
        public final long stamp;
        public long receivedStamp;
    }
}