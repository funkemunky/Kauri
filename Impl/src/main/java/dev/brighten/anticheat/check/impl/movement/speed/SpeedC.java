package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInKeepAlivePacket;
import cc.funkemunky.api.utils.Materials;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.PlayerUtils;
import cc.funkemunky.api.utils.XMaterial;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.Helper;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.anticheat.utils.TickTimer;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

@Cancellable
@CheckInfo(name = "Speed (C)", description = "Speed check by DeprecatedLuke, improved by funkemunky.",
        punishVL = 30, vlToFlag = 5, developer = true)
public class SpeedC extends Check {

    public double previousDistance;
    private int fallTicks;
    private int webTicks;
    private double velocityX, velocityZ;
    private TickTimer horizontalIdle;
    private static Material ice = XMaterial.ICE.parseMaterial(), packed_ice = XMaterial.PACKED_ICE.parseMaterial();

    @Override
    public void setData(ObjectData data) {
        super.setData(data);
        horizontalIdle = new TickTimer(data, 20);
    }

    @Packet
    public void onTrans(WrappedInKeepAlivePacket packet) {
        if(packet.getTime() == data.getKeepAliveStamp("velocity")) {
            velocityX = data.playerInfo.velocityX;
            velocityZ = data.playerInfo.velocityZ;
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if (!packet.isPos()
                || (data.playerInfo.deltaXZ == 0 && data.playerInfo.deltaY == 0)) return;

        List<String> tags = new ArrayList<>();

        double moveSpeed = Math.pow(data.getPlayer().getWalkSpeed() * 5, 2);
        double drag = 0.91;
        boolean onGround = data.playerInfo.clientGround || data.blockInfo.onSlime;

        if (data.playerInfo.deltaY < 0) fallTicks++;
        else fallTicks = 0;

        double velocityXZ = MathUtils.hypot(velocityX, velocityZ);

        Material type = data.playerInfo.blockBelow == null
                ? XMaterial.AIR.parseMaterial()
                : data.playerInfo.blockBelow.getType();

        if (onGround || (data.playerInfo.lClientGround
                && MathUtils.getDelta(data.playerInfo.jumpHeight, data.playerInfo.deltaY) < 0.1)) {
            tags.add("ground");
            drag *= data.blockInfo.currentFriction;
            moveSpeed *= drag > 0.708 ? 1.3 : data.predictionService.aiMoveSpeed * 1.1f;
            moveSpeed *= 0.16277136 / Math.pow(drag, 3);

            if (data.playerInfo.deltaY > 0
                    && MathUtils.getDelta(data.playerInfo.jumpHeight, data.playerInfo.deltaY) < 0.1) {
                tags.add("ascend");
                moveSpeed += 0.2;

                if (data.playerInfo.jumped) {
                    tags.add("hop");
                    moveSpeed += 0.1;
                    if (data.playerInfo.wasOnSlime) {
                        tags.add("slimehop");
                        moveSpeed += 0.1;
                    }
                }
            } else if (data.playerInfo.deltaY < 0.0) {
                tags.add("fall");
                moveSpeed -= 0.1;
                if (data.playerInfo.wasOnSlime) {
                    tags.add("slimefall");
                    moveSpeed += 0.1;
                }
            } else {
                if (timeStamp - data.playerInfo.lastServerPos < 500L) {
                    moveSpeed *= 2;
                }

                if(!data.playerInfo.lClientGround && data.playerInfo.clientGround) {
                    tags.add("hover-land");
                    moveSpeed += 0.1;
                } else {
                    tags.add("hover");
                    moveSpeed+= 0.05;
                }
                if (data.playerInfo.lastAttack.hasNotPassed(10)) moveSpeed += 0.2;
            }
        } else {
            tags.add("air");
            moveSpeed = 0.027;
            drag = 0.91;

            if (timeStamp - data.playerInfo.lastServerPos < 100L) {
                moveSpeed *= 1.5;
                tags.add("tp");
            }

            if (fallTicks == 1 && data.blockInfo.inLava) {
                double dy = Math.abs(data.playerInfo.deltaY);
                if (dy > 0.08 || dy < 0.07) {
                    tags.add("fallen");
                    moveSpeed /= (dy * 150);
                }
            }

            if (data.blockInfo.onSoulSand) {
                moveSpeed += 0.1;
                if (type == ice || type == packed_ice) {
                    moveSpeed += 0.1;
                    tags.add("souliceair");
                } else tags.add("soulair");
            }
            if (data.playerInfo.wasOnSlime) {
                tags.add("slimeair");
                moveSpeed += 0.2;
            }
        }

        data.blockInfo.handler.setOffset(1);
        data.blockInfo.handler.setSize(0.6, 1);

        if (data.blockInfo.inWater) {
            tags.add("water");
        }

        if (data.blockInfo.inLava
                && data.getPlayer().getNoDamageTicks() == data.getPlayer().getMaximumNoDamageTicks()
                && data.blockInfo.handler.isCollidedWith(Materials.LAVA)) {
            tags.add("lava");
            moveSpeed *= 0.7;
        }

        data.blockInfo.handler.setOffset(0);

        double previousHorizontal = previousDistance;
        double horizontalDistance = data.playerInfo.deltaXZ;
        boolean underBlock = data.playerInfo.blockAboveTimer.hasNotPassed(6);

        if (underBlock) {
            tags.add("under");
            moveSpeed += 2.6;
        }

        val depth = MovementUtils.getDepthStriderLevel(data.getPlayer());
        if (depth > 0 && data.playerInfo.liquidTimer.hasNotPassed(4)) {
            tags.add("depthstrider");
            moveSpeed += depth;
        }

        if (data.blockInfo.onHalfBlock || data.blockInfo.onStairs || data.blockInfo.onSlab) {
            tags.add("weird");
            moveSpeed += 0.2;
        }

        if (timeStamp - data.playerInfo.lastServerPos < 180L) moveSpeed += 1;

        moveSpeed += velocityXZ * (data.playerInfo.lastVelocity.hasNotPassed(20) ? 2 : 1);

        /*int speed = PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.SPEED);

        if (speed > 0) {
            tags.add("speed");
            moveSpeed += (speed * .06);
        }*/

        int jump = PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.JUMP);

        if (jump > 0) {
            tags.add("jump");
            moveSpeed += (jump * .06);
        }

        if (moveSpeed > 0.046
                && moveSpeed < 0.047
                && Helper.format(data.playerInfo.deltaY, 4) == 0.0784) {
            tags.add("fall-2");
            moveSpeed += 1;
        }

        if (data.playerInfo.deltaY == 0 && !data.playerInfo.clientGround) horizontalIdle.reset();

        if (horizontalIdle.hasNotPassed(1)
                && !data.blockInfo.inLiquid && !data.blockInfo.inWeb) {
            tags.add("idle");
            moveSpeed += 0.5;
        }

        if (data.blockInfo.inWeb && data.playerInfo.lastBrokenBlock.hasPassed(3)) {
            if(webTicks++ > 2) {
                tags.add("web");
                moveSpeed -= 0.2;
            }
        } else webTicks = 0;

        if (data.playerInfo.soulSandTimer.hasNotPassed(0) && !tags.contains("air")) {
            moveSpeed -= 0.05;
            if (type == ice || type == packed_ice) {
                moveSpeed -= 0.1;
                tags.add("soulice");
            } else tags.add("soul");
        }

        if(data.playerInfo.lastBlockPlace.hasNotPassed(8)) {
            moveSpeed+= 0.24;
        } else if(data.playerInfo.lastBlockPlace.hasNotPassed(12)) {
            moveSpeed+= 0.1;
        }

        double dyf = Helper.format(data.playerInfo.deltaY, 4);
        if (dyf > -0.0785 && dyf < 0 && !data.playerInfo.serverGround) {
            tags.add("first");
            moveSpeed += 0.21;
        }

        double horizontalMove = (horizontalDistance - previousHorizontal) - moveSpeed;
        if (horizontalDistance > 0.1 && !data.playerInfo.generalCancel) {
            if (horizontalMove > 0 && data.playerInfo.lastVelocity.hasPassed(10)) {
                vl++;
                if(horizontalMove > 0.54 || vl > 7) {
                    flag("+%v,tags=%v",
                            MathUtils.round(horizontalMove, 5), String.join(",", tags));
                }
            } else vl-= vl > 0 ? 0.2 : 0;
        }

        debug("+%v.4,tags=%v,place=%v,dy=%v.3,jumped=%v", horizontalMove, String.join(",", tags),
                data.playerInfo.lastBlockPlace.getPassed(), data.playerInfo.deltaY, data.playerInfo.jumped);


        if(velocityXZ > 0) {
            velocityX*= (drag * (onGround ? data.blockInfo.currentFriction : 1));
            velocityZ*= (drag * (onGround ? data.blockInfo.currentFriction : 1));

            if(Math.abs(velocityX) < 0.005) velocityX = 0;
            if(Math.abs(velocityZ) < 0.005) velocityZ = 0;
        }

        this.previousDistance = horizontalDistance * drag;
    }
}
