package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.PlayerUtils;
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
    public void onVelocity(WrappedOutVelocityPacket packet) {
        if(packet.getId() == data.getPlayer().getEntityId()) {
            data.runKeepaliveAction(ka -> {
                ldxz = Math.hypot(packet.getX(), packet.getZ());
                debug("set velocity: %v.3", ldxz);
            });
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        checkProccesing:
        {
            if (!packet.isPos()
                    || (data.playerInfo.deltaY == 0 && data.playerInfo.deltaXZ == 0)
                    || data.playerInfo.serverPos) {
                break checkProccesing;
            }
            float drag = friction;

            TagsBuilder tags = new TagsBuilder();
            double moveFactor = data.getPlayer().getWalkSpeed() / 2f;

            moveFactor+= moveFactor * 0.3f;

            if(data.potionProcessor.hasPotionEffect(PotionEffectType.SPEED))
                moveFactor += (PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.SPEED)
                        * (0.20000000298023224D)) * moveFactor;

            if(data.potionProcessor.hasPotionEffect(PotionEffectType.SLOW))
                moveFactor += (PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.SLOW)
                        * (-0.15000000596046448D)) * moveFactor;

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

            double ratio = (data.playerInfo.deltaXZ - ldxz) / moveFactor * 100;

            if (ratio > 100.1 && !data.playerInfo.generalCancel && data.playerInfo.lastVelocity.hasPassed(2)) {
                vl++;
                flag("p=%v.1% dxz=%v.3 aimove=%v.3 tags=%v",
                        ratio, data.playerInfo.deltaXZ, data.predictionService.aiMoveSpeed, tags.build());
            }
            debug("ratio=%v.1 tags=%v", ratio, tags.build());

            ldxz = data.playerInfo.deltaXZ * drag;
        }
        friction = data.blockInfo.currentFriction;
    }
}
