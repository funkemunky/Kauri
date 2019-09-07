package dev.brighten.anticheat.processing;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.data.ObjectData;

public class MovementProcessor {

    public static void process(ObjectData data, WrappedInFlyingPacket packet) {
        data.information.blockInfo.runCollisionCheck();


    }
}
