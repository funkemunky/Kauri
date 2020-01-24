package dev.brighten.anticheat.data.classes;

import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.TickTimer;
import cc.funkemunky.api.utils.math.cond.MaxInteger;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.utils.CollisionHandler;
import dev.brighten.anticheat.utils.MouseFilter;
import org.bukkit.block.Block;

public class PlayerInformation {
    public boolean serverGround, lClientGround, clientGround, nearGround,
            collided,
            onLadder, usingItem, wasOnIce, wasOnSlime, jumped, inAir, lworldLoaded, worldLoaded;
    public boolean generalCancel, flightCancel;
    public float fallDistance, jumpHeight;
    public double deltaY, lDeltaY, deltaX, lDeltaX, deltaZ, lDeltaZ, deltaXZ, lDeltaXZ;
    public float headYaw, headPitch;
    public float deltaYaw, deltaPitch, lDeltaYaw, lDeltaPitch;
    public long lastVelocityTimestamp;
    public Block blockBelow, blockOnTo;

    //Cinematic
    public float lCinematicYaw, lCinematicPitch;
    public float cinematicYaw, cinematicPitch, cDeltaYaw, cDeltaPitch;
    public boolean cinematicMode, cinematicModePitch;
    public MouseFilter yawSmooth = new MouseFilter(), pitchSmooth = new MouseFilter();

    //Gcd
    public double yawGCD, pitchGCD, lastYawGCD, lastPitchGCD;

    //Server Position
    public long lastServerPos;
    public boolean serverPos;
    public long isTeleport, isVelocity;
    public EvictingList<KLocation> posLocs = new EvictingList<>(5);
    public CollisionHandler handler;

    //Attack
    public TickTimer lastAttack = new TickTimer(5);
    public long lastAttackTimeStamp;

    //actions
    public boolean sneaking, sprinting, ridingJump, breakingBlock, flying, canFly, creative, inVehicle,
            gliding, riptiding;

    //ticks
    public int groundTicks, airTicks;
    public MaxInteger liquidTicks = new MaxInteger(50), halfBlockTicks = new MaxInteger(60),
            webTicks = new MaxInteger(40), climbTicks = new MaxInteger(40), slimeTicks = new MaxInteger(75),
            iceTicks = new MaxInteger(45), blocksAboveTicks = new MaxInteger(50), soulSandTicks = new MaxInteger(40);
    public TickTimer lastBrokenBlock = new TickTimer(5),
            lastVelocity = new TickTimer(20),
            lastTargetSwitch = new TickTimer(3),
            lastBlockPlace = new TickTimer(10),
            lastToggleFlight = new TickTimer(10),
            lastWorldUnload = new TickTimer(20);
    public long lastToggleFlightStamp;

    public double velocityX, velocityY, velocityZ;
    public double mvx, mvy, mvz;
    public boolean takingVelocity;

    public KLocation from = new KLocation(0,0,0), to = new KLocation(0,0,0),
            groundLoc;
}
