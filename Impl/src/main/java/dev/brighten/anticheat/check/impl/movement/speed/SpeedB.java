package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.PlayerUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

@CheckInfo(name = "Speed (B)", description = "Predicts the motion of a player accurately.", developer = true,
        executable = false, punishVL = 150)
public class SpeedB extends Check {

    public double previousDistance;
    private double drag = 0.91;
    private int fallTicks;
    private int noSlowStreak;
    private Enchantment DEPTH_STRIDER;
    private double omniVl = 0;

    public SpeedB() {
        try {
            DEPTH_STRIDER = Enchantment.DEPTH_STRIDER;
        } catch(Throwable e) {

        }
    }


    @Packet
    public void onPacket(WrappedInFlyingPacket packet, long timeStamp) {
        if (!packet.isPos()
                || (data.playerInfo.deltaXZ == 0 && data.playerInfo.deltaY == 0)
                || data.playerInfo.generalCancel
                || data.playerInfo.gliding) return;
        List<String> tags = new ArrayList<>();
        double deltaY = data.playerInfo.deltaY;

        double moveSpeed = Math.pow(data.getPlayer().getWalkSpeed() * 5, 2);
        double drag = this.drag;
        boolean onGround = data.playerInfo.clientGround || data.playerInfo.wasOnSlime;

        if (deltaY < 0) fallTicks++;
        else fallTicks = 0;

        Material type = data.getPlayer().getWorld().getBlockAt(data.getPlayer().getLocation().getBlockX(), (int) (data.getPlayer().getLocation().getY() - 1.8), data.getPlayer().getLocation().getBlockZ()).getType();

        if (onGround || data.playerInfo.jumped) {
            tags.add("ground");
            drag *= 0.91;
            moveSpeed *= drag > 0.708 ? 1.3 : 0.23315;
            moveSpeed *= 0.16277136 / Math.pow(drag, 3);

            if (deltaY > 0) {
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
                    tags.add("tp");
                }

                tags.add("hover");
                moveSpeed += 0.05;
                if (data.playerInfo.lastAttack.hasNotPassed(10)) {
                    tags.add("attacked");
                    moveSpeed += 0.2;
                }
            }
        } else {
            tags.add("air");
            moveSpeed = data.playerInfo.sprinting ? 0.026 : 0.02;
            drag = 0.91;

            if (timeStamp - data.playerInfo.lastServerPos < 500L) {
                moveSpeed *= 1.5;
                tags.add("tp");
            }

            if (fallTicks == 1 && !data.blockInfo.inLiquid) {
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

        if (data.blockInfo.inWater) {
            tags.add("water");
            moveSpeed *= 0.8;
        }

        if (data.getPlayer().getNoDamageTicks() == data.getPlayer().getMaximumNoDamageTicks()
                && data.blockInfo.inLava) {
            tags.add("lava");
            moveSpeed *= 0.6;
        }

        double previousHorizontal = previousDistance;
        double horizontalDistance = data.playerInfo.deltaXZ;
        boolean underBlock = data.playerInfo.blocksAboveTicks > 0;

        if (underBlock) {
            tags.add("under");
            moveSpeed += 2.6;
        }

        if (data.playerInfo.deltaY > 0.01 && data.playerInfo.deltaY < 0.02) {
            tags.add("waterjump");
            moveSpeed += 1;
        }

        if (data.getPlayer().getInventory().getBoots() != null && DEPTH_STRIDER != null) {
            int lvl = data.getPlayer().getInventory().getBoots().getEnchantmentLevel(DEPTH_STRIDER);
            if (lvl != 0) {
                tags.add("depthstrider");
                moveSpeed += lvl;
            }
        }

        if (data.blockInfo.onSlime || data.blockInfo.onStairs) {
            tags.add("weird");
            moveSpeed += 0.2;
        }

        if (timeStamp - data.playerInfo.lastServerPos < 150L) moveSpeed += 1;

        if(data.playerInfo.lastVelocity.hasNotPassed(20)) {
            moveSpeed += MathUtils.hypot(data.playerInfo.velocityX , data.playerInfo.velocityZ) ;
        }

        int speed = PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.SPEED);

        if (data.getPlayer().hasPotionEffect(PotionEffectType.SPEED)) {
            tags.add("speed");
            moveSpeed += (speed * .06);
        }

        int jump = PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.JUMP);

        if (data.getPlayer().hasPotionEffect(PotionEffectType.JUMP)) {
            tags.add("jump");
            moveSpeed += (jump * .06);
        }

        if (moveSpeed > 0.046
                && moveSpeed < 0.047
                && MathUtils.trim(4, deltaY) == 0.0784) {
            tags.add("fall");
            moveSpeed += 1;
        }

        if (data.blockInfo.inWeb) {
            tags.add("web");
            moveSpeed -= 0.2;
        }

        if (data.blockInfo.onSoulSand) {
            moveSpeed -= 0.05;
            if (type == Material.ICE || type == Material.PACKED_ICE) {
                moveSpeed -= 0.1;
                tags.add("soulice");
            } else tags.add("soul");
        }

        if (data.blockInfo.onSlime) {
            tags.add("slime");
            moveSpeed -= 0.07;
        }

        double dyf = MathUtils.trim(4, data.playerInfo.deltaY);
        if (dyf > -0.0785 && dyf < 0) {
            tags.add("first");
            moveSpeed += 0.21;
        }

        double horizontalMove = (horizontalDistance - previousHorizontal) - moveSpeed;
        if (horizontalDistance > 0.1) {
            String joined = String.join(",", tags);
            debug(horizontalMove + ": " + joined);

            if(horizontalMove > 0 && timeStamp - data.playerInfo.lastServerPos > 150L) {
                vl+= horizontalMove > 0.01 ? 4 : 1;
                if(vl > 2) {
                    flag("move=" + MathUtils.round(horizontalMove, 4) + " tags=" + joined);
                }
            } else vl-= vl > 0 ? 0.025f : 0;
        }

        debug((timeStamp - data.playerInfo.lastServerPos) + ", " + (timeStamp - data.playerInfo.lastVelocityTimestamp));

        this.previousDistance = horizontalDistance * drag;
        this.drag = MovementUtils.getFriction(data);
    }
}
