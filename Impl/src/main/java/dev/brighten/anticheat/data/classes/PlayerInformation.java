package dev.brighten.anticheat.data.classes;

import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.TickTimer;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.utils.CollisionHandler;
import dev.brighten.anticheat.utils.MouseFilter;
import org.bukkit.block.Block;

public class PlayerInformation {
    public boolean serverGround, lClientGround, clientGround, nearGround,
            collided,
            onLadder, usingItem, wasOnIce, wasOnSlime, jumped, inAir, lworldLoaded, worldLoaded;
    public boolean generalCancel, flightCancel;
    public float deltaY, lDeltaY, deltaX, lDeltaX, deltaZ, lDeltaZ, deltaXZ, lDeltaXZ, fallDistance, jumpHeight;
    public float pDeltaY;
    public float deltaYaw, deltaPitch, lDeltaYaw, lDeltaPitch;
    public long lastVelocityTimestamp;
    public Block blockBelow, blockOnTo;

    //Cinematic
    public float lCinematicYaw, lCinematicPitch;
    public float cinematicYaw, cinematicPitch, cDeltaYaw, cDeltaPitch;
    public boolean cinematicModeYaw, cinematicModePitch;
    public MouseFilter yawSmooth = new MouseFilter(), pitchSmooth = new MouseFilter();

    //Gcd
    public float yawGCD, pitchGCD, lastYawGCD, lastPitchGCD;

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
    public int liquidTicks, groundTicks, airTicks, halfBlockTicks, webTicks, climbTicks, slimeTicks, iceTicks,
            blocksAboveTicks, soulSandTicks;
    public TickTimer lastBrokenBlock = new TickTimer(5),
            lastVelocity = new TickTimer(20),
            lastTargetSwitch = new TickTimer(3),
            lastBlockPlace = new TickTimer(10),
            lastToggleFlight = new TickTimer(10),
            lastLoadedPacketSend = new TickTimer(100);
    public boolean loadedPacketReceived = true;
    public long lastToggleFlightStamp;

    public float velocityX, velocityY, velocityZ;
    public float mvx, mvy, mvz;
    public boolean takingVelocity, sentpostofalse;

    public KLocation from, to;
}
