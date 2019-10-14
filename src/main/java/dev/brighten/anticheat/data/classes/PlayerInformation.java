package dev.brighten.anticheat.data.classes;

import cc.funkemunky.api.tinyprotocol.packet.types.WrappedEnumAnimation;
import cc.funkemunky.api.utils.TickTimer;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.utils.KLocation;
import dev.brighten.anticheat.utils.MouseFilter;

public class PlayerInformation {
    public boolean serverGround, lClientGround, clientGround, nearGround, useItem,
            collidesVertically, collidesHorizontally, serverCanFly, inCreative, serverIsFlying, collided,
            onLadder, usingItem, wasOnIce, wasOnSlime, jumped, inAir, breakingBlock, worldLoaded,
            clientIsFlying, clientCanFly;
    public boolean generalCancel, flightCancel;
    public float deltaY, lDeltaY, deltaX, lDeltaX, deltaZ, lDeltaZ, deltaXZ, lDeltaXZ, fallDistance, jumpHeight;
    public float pDeltaY;
    public float deltaYaw, deltaPitch, lDeltaYaw, lDeltaPitch;

    //Cinematic
    public float lCinematicYaw, lCinematicPitch;
    public float cinematicYaw, cinematicPitch, cDeltaYaw, cDeltaPitch;
    public boolean cinematicModeYaw, cinematicModePitch;
    public MouseFilter yawSmooth = new MouseFilter(), pitchSmooth = new MouseFilter(),
            mouseFilterX = new MouseFilter(), mouseFilterY = new MouseFilter();

    public WrappedEnumAnimation itemAnimation;

    //Gcd
    public float yawGCD, pitchGCD, lastYawGCD, lastPitchGCD;

    //Server Position
    public long lastServerPos;
    public boolean serverPos;
    public EvictingList<KLocation> posLocs = new EvictingList<>(5);

    //Attack
    public TickTimer lastAttack = new TickTimer(5);

    //actions
    public boolean sneaking, sprinting, ridingJump;

    //ticks
    public int liquidTicks, groundTicks, airTicks, halfBlockTicks, webTicks, climbTicks, slimeTicks, iceTicks,
            blocksAboveTicks, soulSandTicks;
    public TickTimer lastBrokenBlock = new TickTimer(5),
            lastVelocity = new TickTimer(20),
            lastTargetSwitch = new TickTimer(3),
            lastBlockPlace = new TickTimer(10);

    public KLocation from, to;
}
