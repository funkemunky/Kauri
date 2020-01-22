package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.utils.Materials;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.PlayerUtils;
import cc.funkemunky.api.utils.TickTimer;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.anticheat.utils.Verbose;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

@Cancellable
@CheckInfo(name = "Speed (C)", description = "Firefly SpeedH check.", developer = true)
public class SpeedC extends Check {

    public double previousDistance;
    private double drag = 0.91;
    private int fallTicks;
    private int noSlowStreak;
    private double omniVl = 0;
    private double velocityX, velocityZ;
    private Verbose verbose = new Verbose(20, 40);
    private TickTimer horizontalIdle = new TickTimer(20);

    @Packet
    public void onTrans(WrappedInTransactionPacket packet) {
        if(packet.getAction() == (short)101) {
            velocityX = data.playerInfo.velocityX;
            velocityZ = data.playerInfo.velocityZ;
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if (!packet.isPos()
                || (data.playerInfo.deltaXZ == 0 && data.playerInfo.deltaY == 0)
                || data.playerInfo.generalCancel) return;

        List<String> tags = new ArrayList<>();
        double deltaY = data.playerInfo.to.y - data.playerInfo.from.y;

        double moveSpeed = Math.pow(data.getPlayer().getWalkSpeed() * 5, 2);
        double drag = this.drag;
        boolean onGround = packet.isGround() || data.blockInfo.onSlime;

        if (deltaY < 0) fallTicks++;
        else fallTicks = 0;

        double velocityXZ = MathUtils.hypot(velocityX, velocityZ);

        if(data.playerInfo.blockBelow == null) return;

        Material type = data.playerInfo.blockBelow.getType();

        if (onGround || data.playerInfo.jumped) {
            tags.add("ground");
            drag *= 0.91;
            moveSpeed *= drag > 0.708 ? 1.3 : 0.23315;
            moveSpeed *= 0.16277136 / Math.pow(drag, 3);

            if (deltaY > 0 && MathUtils.getDelta(data.playerInfo.jumpHeight, deltaY) < 0.1) {
                tags.add("ascend");
                moveSpeed += 0.2;

                if (data.playerInfo.jumped) {
                    tags.add("hop");
                    moveSpeed += 0.05;
                    if (data.playerInfo.wasOnSlime) {
                        tags.add("slimehop");
                        moveSpeed += 0.1;
                    }
                }
            } else if (deltaY < 0.0) {
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

                tags.add("hover");
                moveSpeed += 0.05;
                if (data.playerInfo.lastAttack.hasNotPassed(10)) moveSpeed += 0.2;
            }
        } else {
            tags.add("air");
            moveSpeed = 0.026;
            drag = 0.91;

            if (timeStamp - data.playerInfo.lastServerPos < 500L) {
                moveSpeed *= 1.5;
                tags.add("tp");
            }

            if (fallTicks == 1 && data.blockInfo.inLava) {
                double dy = Math.abs(deltaY);
                if (dy > 0.08 || dy < 0.07) {
                    tags.add("fallen");
                    moveSpeed /= (dy * 150);
                }
            }

            if (data.blockInfo.onSoulSand) {
                moveSpeed += 0.1;
                if (type == Material.ICE || type == Material.PACKED_ICE) {
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
            moveSpeed *= 0.9;
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
        boolean underBlock = data.playerInfo.blocksAboveTicks.value() > 0;

        if (underBlock) {
            tags.add("under");
            moveSpeed += 2.6;
        }

        val depth = PlayerUtils.getDepthStriderLevel(data.getPlayer());
        if (depth > 0 && data.playerInfo.liquidTicks.value() > 0) {
            tags.add("depthstrider");
            moveSpeed += depth;
        }

        if (data.blockInfo.onHalfBlock || data.blockInfo.onStairs || data.blockInfo.onSlab) {
            tags.add("weird");
            moveSpeed += 0.2;
        }

        if (timeStamp - data.playerInfo.lastServerPos < 180L) moveSpeed += 1;

        moveSpeed += velocityXZ * (data.playerInfo.lastVelocity.hasNotPassed(20) ? 2 : 1);

        int speed = PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.SPEED);

        if (speed > 0) {
            tags.add("speed");
            moveSpeed += (speed * .06);
        }

        int jump = PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.JUMP);

        if (jump > 0) {
            tags.add("jump");
            moveSpeed += (jump * .06);
        }

        if (moveSpeed > 0.046
                && moveSpeed < 0.047
                && MiscUtils.format(deltaY, 4) == 0.0784) {
            tags.add("fall");
            moveSpeed += 1;
        }

        if (data.playerInfo.deltaY == 0 && !data.playerInfo.clientGround) horizontalIdle.reset();

        if (horizontalIdle.hasNotPassed(1)) {
            tags.add("idle");
            moveSpeed += 0.5;
        }

        if (data.blockInfo.inWeb && data.playerInfo.webTicks.value() > 0) {
            tags.add("web");
            moveSpeed -= 0.2;
        }

        if (data.playerInfo.soulSandTicks.value() > 0) {
            moveSpeed -= 0.05;
            if (type == Material.ICE || type == Material.PACKED_ICE) {
                moveSpeed -= 0.1;
                tags.add("soulice");
            } else tags.add("soul");
        }

        if (data.playerInfo.wasOnSlime) {
            tags.add("slime");
            moveSpeed -= 0.07;
        }

        double dyf = MiscUtils.format(data.playerInfo.deltaY, 4);
        if (dyf > -0.0785 && dyf < 0 && !data.playerInfo.serverGround) {
            tags.add("first");
            moveSpeed += 0.21;
        }

        double horizontalMove = (horizontalDistance - previousHorizontal) - moveSpeed;
        if (horizontalDistance > 0.1) {
            debug("+%1,tags=%2", horizontalMove, String.join(",", tags));

            if (horizontalMove > 0) {
                if(verbose.flag(1, 1) || horizontalMove > 0.4) {
                    vl++;
                    flag("+%1,v=%2,tags=%3",
                            horizontalMove, data.playerInfo.velocityX, String.join(",", tags));
                }
            }
        }

        if(velocityXZ > 0) {
            velocityX*= (drag * (onGround ? data.blockInfo.currentFriction : 1));
            velocityZ*= (drag * (onGround ? data.blockInfo.currentFriction : 1));

            if(Math.abs(velocityX) < 0.005) velocityX = 0;
            if(Math.abs(velocityZ) < 0.005) velocityZ = 0;
        }

        this.previousDistance = horizontalDistance * drag;
        this.drag = data.blockInfo.currentFriction;
    }
}
