package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.TagsBuilder;
import dev.brighten.api.check.CheckType;
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "Speed (D)", description = "Minecraft code acceleration check.",
        checkType = CheckType.SPEED, developer = true)
public class SpeedD extends Check {

    private double ldxz = .12f;
    private float friction = 0.91f;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        checkProccesing:
        {
            if (!packet.isPos()
                    || (data.playerInfo.deltaY == 0 && data.playerInfo.deltaXZ == 0)
                    || data.playerInfo.serverPos) {
                ldxz = 0.12f;
                break checkProccesing;
            }
            float drag = friction;

            TagsBuilder tags = new TagsBuilder();
            double moveFactor = data.predictionService.aiMoveSpeed;

            if (data.playerInfo.lClientGround) {
                tags.addTag("ground");
                drag *= 0.91;
                moveFactor *= 0.16277136 / (drag * drag * drag);

                if (data.playerInfo.jumped) {
                    tags.addTag("jumped");
                    moveFactor += 0.2;
                }
            } else {
                tags.addTag("air");
                drag = 0.91f;
                moveFactor = data.playerInfo.sprinting ? 0.026 : 0.02;
            }

            if (data.playerInfo.sprinting) tags.addTag("sprint");

            double ratio = (data.playerInfo.deltaXZ - ldxz) / moveFactor * 100;

            if (ratio > 100.1 && !data.playerInfo.generalCancel && data.playerInfo.lastVelocity.hasPassed(2)) {
                vl++;
                flag("p=%v.1% dxz=%v.5 ldxz=%v.5 tags=%v", ratio, data.playerInfo.deltaXZ, ldxz, tags.build());
            } else if (vl > 0)
                debug("ratio=%v.1 tags=%v", ratio, tags.build());

            ldxz = Math.max(0.12f, data.playerInfo.deltaXZ * drag);
        }
        friction = data.blockInfo.currentFriction;
    }
}
