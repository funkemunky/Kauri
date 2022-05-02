package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.PlayerUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.TagsBuilder;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "Speed (A)", description = "Minecraft code speed acceleration check.", executable = true,
        checkType = CheckType.SPEED, vlToFlag = 2, punishVL = 10)
@Cancellable
public class SpeedA extends Check {

    private double ldxz = .12f;
    private float friction = 0.91f;
    private float buffer;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(data.excuseNextFlying) return;
        checkProccesing:
        {
            if (!packet.isPos())
                break checkProccesing;

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
                drag *= 0.91f;
                moveFactor *= 0.16277136 / (drag * drag * drag);

                if (data.playerInfo.jumped) {
                    tags.addTag("jumped");
                    moveFactor += 0.2;
                }
            } else {
                tags.addTag("air");
                drag = 0.91f;
                moveFactor = 0.026f;
            }

            if(data.blockInfo.inWater) {
                tags.addTag("water");

                drag = data.getClientVersion().isOrAbove(ProtocolVersion.V1_13) ? 0.9f : 0.8f;
                moveFactor = 0.034;

                if(data.playerInfo.liquidTimer.getResetStreak() < 3) {
                    tags.addTag("water-enter");
                    moveFactor*= 1.35;
                }
            } else if(data.playerInfo.liquidTimer.isNotPassed(3)) {
                moveFactor*= 1.35;
                tags.addTag("water-leave");
            }

            if(data.playerInfo.lastTeleportTimer.isNotPassed(6)
                    || data.playerInfo.lastRespawnTimer.isNotPassed(6)) {
                tags.addTag("teleport");
                moveFactor+= 0.1;
                moveFactor*= 5;
            }

            if(data.playerInfo.lastEntityCollision.isNotPassed(2)) {
                tags.addTag("entity-collision");
                moveFactor+= 0.05;
            }

            if(data.blockInfo.pistonNear) {
                tags.addTag("piston");
                moveFactor+= 1;
            }

            if(data.blockInfo.inWeb
                    //Ensuring they aren't just entering or leaving web
                    && data.playerInfo.webTimer.getResetStreak() > 1) {
                tags.addTag("web");
                moveFactor*= 0.4;
            }

            if(data.blockInfo.onSoulSand && data.playerInfo.lClientGround) {
                tags.addTag("soulsand");
                moveFactor*= 0.88;
            }

            double ratio = (data.playerInfo.deltaXZ - ldxz) / moveFactor * 100;

            if (ratio > 100.8
                    && !data.blockInfo.inHoney
                    && !data.blockInfo.inScaffolding
                    && data.playerInfo.lastVelocity.isPassed(2)
                    && data.playerInfo.liquidTimer.isPassed(2)
                    && !data.playerInfo.generalCancel) {
                if(++buffer > 2) {
                    vl++;
                    flag("p=%.1f%% dxz=%.3f a/g=%s,%s aimove=%.3f tags=%s",
                            ratio, data.playerInfo.deltaXZ, data.playerInfo.airTicks, data.playerInfo.groundTicks,
                            data.predictionService.aiMoveSpeed, tags.build());
                } else if(ratio > 250) {
                    cancelAction(CancelType.MOVEMENT);
                    debug("Cancelled user movement: %.1f", ratio);
                }
                if(buffer >= 2)
                fixMovementBugs();
            } else if(buffer > 0) buffer-= 0.25f;
            debug("ratio=%.1f tags=%s tp=%s buffer=%.1f", ratio, tags.build(),
                    data.playerInfo.liquidTimer.getPassed(), buffer);

            ldxz = data.playerInfo.deltaXZ * drag;
        }
        friction = data.blockInfo.currentFriction;
    }
}
