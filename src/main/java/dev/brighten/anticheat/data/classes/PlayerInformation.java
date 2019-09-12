package dev.brighten.anticheat.data.classes;

import cc.funkemunky.api.utils.TickTimer;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.utils.KLocation;
import dev.brighten.anticheat.utils.MouseFilter;

public class PlayerInformation {
    public boolean serverGround, lServerGround, clientGround, nearGround, collidedGround,
            collidesVertically, collidesHorizontally, canFly, inCreative, isFlying, collided, blocksAbove;
    public boolean generalCancel, flightCancel;
    public boolean wasOnIce, wasOnSlime, jumped, inAir, breakingBlock;
    public float deltaY, lDeltaY, deltaX, lDeltaX, deltaZ, lDeltaZ, deltaXZ, lDeltaXZ, pDeltaXZ, lpDeltaXZ, prePDeltaY;
    public float pDeltaY, pDeltaX, pDeltaZ, lpDeltaX, lpDeltaY, lpDeltaZ;
    public float deltaYaw, deltaPitch, lDeltaYaw, lDeltaPitch;
    public float fallDistance;

    //Move
    public float strafe, forward;
    public String key;

    //Cinematic
    public float cinematicYaw, cinematicPitch;
    public boolean cinematicModeYaw, cinematicModePitch;
    public MouseFilter yawSmooth = new MouseFilter(), pitchSmooth = new MouseFilter(),
            mouseFilterX = new MouseFilter(), mouseFilterY = new MouseFilter();

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
            blocksAboveTicks;
    public TickTimer lastBrokenBlock = new TickTimer(5),
            lastVelocity = new TickTimer(20);

    public KLocation from, to;
}
