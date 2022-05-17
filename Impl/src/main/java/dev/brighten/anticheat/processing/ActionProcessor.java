package dev.brighten.anticheat.processing;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInEntityActionPacket;
import dev.brighten.anticheat.data.ObjectData;

public class ActionProcessor {

    public static void process(ObjectData data, WrappedInEntityActionPacket packet) {
        switch(packet.getAction()) {
            case START_SNEAKING:
                data.playerInfo.sneaking = true;
                data.predictionService.sneaking = true;
                break;
            case STOP_SNEAKING:
                data.playerInfo.sneaking = false;
                data.predictionService.sneaking = false;
                break;
            case START_RIDING_JUMP:
                data.playerInfo.ridingJump = true;
                break;
            case STOP_RIDING_JUMP:
                data.playerInfo.ridingJump = false;
                break;
            case START_SPRINTING:
                data.playerInfo.sprinting = true;
                data.predictionService.sprinting = true;
                break;
            case STOP_SPRINTING:
                data.playerInfo.sprinting = false;
                data.predictionService.sprinting = false;
                break;
        }
    }
}
