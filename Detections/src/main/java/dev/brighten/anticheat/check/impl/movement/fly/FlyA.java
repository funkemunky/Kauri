package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.TagsBuilder;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Fly (A)", description = "Checks for improper acceleration.", checkType = CheckType.FLIGHT,
        vlToFlag = 4, punishVL = 15, executable = true)
@Cancellable
public class FlyA extends Check {

    private long lastPos;
    private float buffer;
    private static double mult = 0.98f;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(packet.isPos() && (data.playerInfo.deltaXZ > 0 || data.playerInfo.deltaY != 0)) {
            /* We check if the player is in ground, since theoretically the y should be zero. */
            double lDeltaY = data.playerInfo.lClientGround ? 0 : data.playerInfo.lDeltaY;
            boolean onGround = data.playerInfo.clientGround;
            double predicted = onGround ? lDeltaY : (lDeltaY - 0.08) * mult;

            if(data.playerInfo.lClientGround && !onGround && data.playerInfo.deltaY > 0) {
                predicted = MovementUtils.getJumpHeight(data);
            }

         /* Basically, this bug would only occur if the client's movement is less than a certain amount.
            If it is, it won't send any position packet. Usually this only occurs when the magnitude
            of motionY is less than 0.005 and it rounds it to 0.
            The easiest way I found to produce this oddity is by putting myself in a corner and just jumping. */
            if(Math.abs(predicted) < 0.005
                    && ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_9)) {
                predicted = 0;
            }

            if(timeStamp - lastPos > 60L) {
                double toCheck = (predicted - 0.08) * mult;

                if(Math.abs(data.playerInfo.deltaY - toCheck) < Math.abs(data.playerInfo.deltaY - predicted))
                    predicted = toCheck;
            }

            double deltaPredict = MathUtils.getDelta(data.playerInfo.deltaY, predicted);

            boolean flagged = false;
            if(!data.playerInfo.flightCancel
                    && data.playerInfo.lastBlockPlace.isPassed(5)
                    && data.playerInfo.lastVelocity.isPassed(3)
                    && !data.playerInfo.serverGround
                    && data.playerInfo.lastGhostCollision.isPassed(3)
                    && data.playerInfo.climbTimer.isPassed(15)
                    && data.playerInfo.blockAboveTimer.isPassed(5)
                    && deltaPredict > 0.016) {
                flagged = true;
                if(++buffer > 5) {
                    ++vl;
                    flag("dY=%.3f p=%.3f dx=%.3f", data.playerInfo.deltaY, predicted,
                            data.playerInfo.deltaXZ);
                    fixMovementBugs();
                }
            } else buffer-= buffer > 0 ? 0.25f : 0;

            debug((flagged ? Color.Green : "")
                            +"pos=%s deltaY=%.3f predicted=%.3f d=%.3f ground=%s lpass=%s cp=%s air=%s buffer=%.1f sg=%s cb=%s fc=%s ",
                    packet.getY(), data.playerInfo.deltaY, predicted, deltaPredict, onGround,
                    data.playerInfo.liquidTimer.getPassed(), data.playerInfo.climbTimer.getPassed(),
                    data.playerInfo.kAirTicks, buffer, data.playerInfo.serverGround,
                    data.playerInfo.climbTimer.getPassed(), data.playerInfo.flightCancel);
            lastPos = timeStamp;
        }
    }

    @CheckInfo(name = "Fly (B)", description = "Looks for players going above a possible height limit",
            checkType = CheckType.FLIGHT, devStage = DevStage.RELEASE, vlToFlag = 4, punishVL = 7, executable = true)
    @Cancellable
    public static class FlyB extends Check {

        private double vertical, limit, velocityY, slimeY;
        private boolean pistonBelow;

        @Packet
        public void onVelocity(WrappedOutVelocityPacket packet) {
            if(packet.getId() == data.getPlayer().getEntityId()) {
                velocityY = MovementUtils.getTotalHeight(data.playerVersion, (float)packet.getY());
            }
        }

        @Packet
        public void onFlying(WrappedInFlyingPacket packet) {
            if (data.playerInfo.generalCancel
                    || data.playerInfo.liquidTimer.isNotPassed(2)
                    || data.playerInfo.canFly
                    || data.playerInfo.creative
                    || data.playerInfo.climbTimer.isNotPassed(2)) {
                vertical = 0;
                limit = Double.MAX_VALUE;
                return;
            }


            if(data.playerInfo.serverGround || packet.getY() % (0.015625) == 0
                    || data.playerInfo.lastGhostCollision.isNotPassed(1)) {
                vertical = 0;

                pistonBelow = data.blockInfo.pistonNear && data.playerInfo.slimeTimer.isNotPassed(20);

                limit = MovementUtils.getTotalHeight(data.playerVersion, MovementUtils.getJumpHeight(data));
                if(data.playerInfo.lastVelocity.isPassed(3)) velocityY = 0;

                if(data.playerInfo.wasOnSlime && data.playerInfo.clientGround) {
                    slimeY = MovementUtils.getTotalHeight(data.playerVersion, (float)Math.abs(data.playerInfo.lDeltaY));
                    debug("SLIME: sy=%.2f dy=%.2f", slimeY, data.playerInfo.lDeltaY);
                } else if(data.playerInfo.slimeTimer.isPassed(4)) slimeY = 0;
            } else {
                vertical += data.playerInfo.deltaY;

                double limit = (this.limit + slimeY + velocityY) * 1.6;

                if(vertical > limit && !pistonBelow) {
                    vl++;
                    flag("%.3f>-%.3f", vertical, limit);
                }

                debug("v=%.3f l=%.3f", vertical, limit);
            }
        }
    }

    @CheckInfo(name = "Fly (C)", description = "Checks for invalid jump heights.",
            checkType = CheckType.FLIGHT, punishVL = 4, vlToFlag = 2, executable = true)
    @Cancellable
    public static class FlyC extends Check {

        @Packet
        public void onPacket(WrappedInFlyingPacket packet) {
            if (packet.isPos()) {
                if (!data.playerInfo.flightCancel
                        && data.playerInfo.jumped
                        && !data.playerInfo.wasOnSlime
                        && !data.blockInfo.collidesHorizontally
                        && data.playerInfo.lClientGround
                        && !data.blockInfo.miscNear
                        && data.playerInfo.lastGhostCollision.isNotPassed(1)
                        && !data.playerInfo.insideBlock
                        && data.playerInfo.blockAboveTimer.isPassed(6)
                        && data.playerInfo.lastBlockPlace.isPassed(20)
                        && data.playerInfo.lastHalfBlock.isPassed(4)
                        && data.playerInfo.lastVelocity.isPassed(4)
                        && MathUtils.getDelta(data.playerInfo.deltaY, data.playerInfo.jumpHeight) > 0.01f) {
                    vl++;
                    flag("deltaY=%.4f maxHeight=%.4f", data.playerInfo.deltaY, data.playerInfo.jumpHeight);

                    fixMovementBugs();
                } else if(vl > 0) vl-= 0.01f;

                debug("deltaY=%s above=%s", data.playerInfo.deltaY,
                        data.playerInfo.blockAboveTimer.getPassed());
            }
        }
    }

    @CheckInfo(name = "Fly (D)", description = "Air modification check", checkType = CheckType.FLIGHT,
            devStage = DevStage.BETA, executable = true, punishVL = 5, vlToFlag = 2)
    @Cancellable(cancelType = CancelType.MOVEMENT)
    public static class FlyD extends Check {

        @Packet
        public void onFlying(WrappedInFlyingPacket packet) {
            if(!packet.isPos()
                    || data.playerInfo.flightCancel
                    || (data.playerInfo.nearGroundTimer.isNotPassed(3) && (data.playerInfo.lClientGround
                    || data.playerInfo.clientGround))
                    || data.playerInfo.lastGhostCollision.isNotPassed(3)
                    || data.playerInfo.lastBlockPlace.isNotPassed(3)
                    || data.playerInfo.lastVelocity.isNotPassed(8)
            ) return;

            if(data.playerInfo.deltaY > data.playerInfo.lDeltaY) {
                vl++;
                flag("dy=%.2f ldy=%.2f", data.playerInfo.deltaY, data.playerInfo.lDeltaY);

                fixMovementBugs();
            }
        }
    }

    @CheckInfo(name = "Fly (E)", description = "Looks for consistent vertical acceleration",
            checkType = CheckType.FLIGHT, punishVL = 8, executable = true)
    @Cancellable
    public static class FlyE extends Check {

        private int buffer;
        @Packet
        public void onFlying(WrappedInFlyingPacket packet) {
            if(!packet.isPos() || data.playerInfo.flightCancel || data.excuseNextFlying) return;

            double accel = Math.abs(data.playerInfo.deltaY - data.playerInfo.lDeltaY);

            if(accel < 0.01 && Math.abs(data.playerInfo.deltaY) < 1.5
                    && data.playerInfo.lastTeleportTimer.isPassed(2)
                    && data.playerInfo.lastRespawnTimer.isPassed(20)
                    && !data.playerInfo.doingBlockUpdate
                    && !data.playerInfo.clientGround && !data.playerInfo.lClientGround) {
                buffer+= 4;

                if(buffer > 15) {
                    vl++;
                    flag("accel=%s deltaY=%.3f buffer=%s", accel, data.playerInfo.deltaY, buffer);
                }
            } else if(buffer > 0) buffer--;
        }
    }

    @Cancellable
    @CheckInfo(name = "Fly (F)", description = "Checks if an individual flys faster than possible.", executable = true,
            punishVL = 5,
            checkType = CheckType.FLIGHT)
    public static class FlyF extends Check {

        private double slimeY = 0;
        @Packet
        public void onPacket(WrappedInFlyingPacket packet) {
            if(data.playerInfo.deltaXZ == 0 && data.playerInfo.deltaY == 0) return;

            double max = Math.max((data.playerInfo.clientGround && data.playerInfo.serverGround)
                    ? 0.6001 : 0.5001, (data.playerInfo.lastVelocity.isNotPassed(20)
                    ? Math.max(data.playerInfo.velocityY, data.playerInfo.jumpHeight)
                    : data.playerInfo.jumpHeight) + 0.001);

            if(data.playerInfo.lastHalfBlock.isNotPassed(20)
                    || data.blockInfo.collidesHorizontally) max = Math.max(0.5625, max);

            if(data.playerInfo.wasOnSlime && data.playerInfo.clientGround && data.playerInfo.nearGround) {
                slimeY = Math.abs(data.playerInfo.deltaY);
                max = Math.max(max, slimeY);
                debug("SLIME: sy=%.2f", slimeY);
            } else if(data.playerInfo.wasOnSlime && data.playerInfo.airTicks > 2) {
                slimeY-= 0.08f;
                slimeY*= 0.98f;

                debug("SLIME ACCEL: sy=%.2f", slimeY);
                max = Math.max(max, slimeY);
            } else if(!data.playerInfo.wasOnSlime && slimeY != 0) {
                slimeY = 0;
            }

            if(data.playerInfo.deltaY > max
                    && !data.blockInfo.roseBush
                    && data.playerInfo.lastVelocity.isPassed(2)
                    && !data.playerInfo.doingVelocity
                    && data.playerInfo.slimeTimer.isPassed(10)
                    && !data.playerInfo.generalCancel) {
                ++vl;
                flag("dY=%.3f max=%.3f", data.playerInfo.deltaY, max);
            }

            debug("halfBlock=%s ticks=%s c/s=%s,%s", data.playerInfo.lastHalfBlock.getPassed(),
                    data.blockInfo.onHalfBlock, data.playerInfo.clientGround, data.playerInfo.serverGround);
        }

    }

    @CheckInfo(name = "Fly (G)", description = "Looks for impossible movements, commonly done by Step modules",
            devStage = DevStage.ALPHA, checkType = CheckType.FLIGHT, punishVL = 12)
    @Cancellable
    public static class FlyG extends Check {

        @Packet
        public void onFlying(WrappedInFlyingPacket packet) {
            if(!packet.isPos() || data.playerInfo.doingBlockUpdate) return;

            boolean toGround = data.playerInfo.clientGround && data.playerInfo.serverGround;
            boolean fromGround = data.playerInfo.lClientGround && data.playerInfo.lServerGround;

            TagsBuilder tags = new TagsBuilder();

            double max = data.playerInfo.jumpHeight;
            if(toGround) {
                if(!fromGround) {
                    if(data.playerInfo.lDeltaY > 0 && data.playerInfo.lastFenceBelow.isPassed(4)
                            && data.playerInfo.blockAboveTimer.isPassed(2)) {
                        tags.addTag("INVALID_LANDING");
                        max = 0;
                    }
                } else {
                    if(data.blockInfo.onSlab || data.blockInfo.onStairs)
                        max = 0.5;
                    else if(data.blockInfo.onHalfBlock || data.blockInfo.miscNear)
                        max = 0.5625;

                    tags.addTag("GROUND_STEP");
                    tags.addTag("max=" + max);
                }
            }

            if(data.playerInfo.deltaY > max && tags.getSize() > 0 && !data.playerInfo.flightCancel) {
                vl++;
                flag("t=" + tags.build());
            }
        }
    }

    @CheckInfo(name = "Fly (H)", description = "Checks for invalid downwards accelerations", checkType = CheckType.FLIGHT,
            punishVL = 10, executable = true)
    @Cancellable
    public static class FlyH extends Check {

        private int buffer;

        //Electrum is sexy if he was of age.
        @Packet
        public void onFlying(WrappedInFlyingPacket packet) {
            if(data.playerInfo.lastVelocity.isNotPassed(20)
                    || !data.playerInfo.checkMovement
                    || data.playerInfo.canFly
                    || data.playerInfo.doingBlockUpdate
                    || data.playerInfo.doingTeleport
                    || data.playerInfo.lastTeleportTimer.isNotPassed(2)
                    || data.playerInfo.creative
                    || data.blockInfo.blocksAbove)
                return;

            final double ldeltaY = data.playerInfo.lDeltaY, deltaY = data.playerInfo.deltaY;

            if(Math.abs(deltaY + ldeltaY) < 0.05
                    && data.playerInfo.lastHalfBlock.isPassed(2)
                    && data.playerInfo.slimeTimer.isPassed(5)
                    && Math.abs(deltaY) > 0.2) {
                buffer+=15;
                if(buffer > 20) {
                    vl++;
                    flag("dy=%.1f ldy=%.1f t=same", deltaY, ldeltaY);
                    buffer = 20; //Making sure the buffer doesn't go too high
                }
            } else if(buffer > 0) buffer--;
        }
    }

    @CheckInfo(name = "Fly (I)", description = "Checks for bad hovering.",
            checkType = CheckType.FLIGHT, devStage = DevStage.BETA, punishVL = 8)
    @Cancellable
    public static class FlyI extends Check {

        private int buffer;
        @Packet
        public void onFlying(WrappedInFlyingPacket packet) {
            if(!packet.isPos()
                    || data.playerInfo.generalCancel
                    || (data.playerInfo.serverGround
                            || (data.playerInfo.clientGround && data.playerInfo.groundTicks < 3))
                    || data.playerInfo.lastToggleFlight.isNotPassed(10)
                    || data.playerInfo.lastVelocity.isNotPassed(1)
                    || data.playerInfo.doingVelocity
                    || data.playerInfo.deltaY != 0
                    || data.playerInfo.climbTimer.isNotPassed(2)) {
                buffer = 0;
                debug("Resetting buffer");
            }

            if(++buffer > 6) {
                vl++;
                flag("b=%s a=%s", buffer, data.playerInfo.airTicks);
            }
        }
    }
}
