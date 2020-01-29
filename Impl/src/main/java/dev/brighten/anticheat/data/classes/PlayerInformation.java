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
    public float fallDistance, jumpHeight;
    public double deltaY, lDeltaY, deltaX, lDeltaX, deltaZ, lDeltaZ, deltaXZ, lDeltaXZ;
    public float headYaw, headPitch;
    public float deltaYaw, deltaPitch, lDeltaYaw, lDeltaPitch;
    public long lastVelocityTimestamp;
    public Block blockBelow, blockOnTo;

    //Cinematic
    public float cinematicYaw, cinematicPitch;
    public boolean cinematicMode;
    public MouseFilter yawSmooth = new MouseFilter(), pitchSmooth = new MouseFilter();

    //Gcd
    public double yawGCD, pitchGCD, lastYawGCD, lastPitchGCD;

    //Server Position
    public long lastServerPos;
    public boolean serverPos;
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
    public TickTimer liquidTimer = new TickTimer(50),
            webTimer = new TickTimer(40), climbTimer = new TickTimer(40),
            slimeTimer = new TickTimer(75), iceTimer = new TickTimer(45),
            blockAboveTimer = new TickTimer(50), soulSandTimer = new TickTimer(40);
    public TickTimer lastBrokenBlock = new TickTimer(5),
            lastVelocity = new TickTimer(20),
            lastTargetSwitch = new TickTimer(3),
            lastBlockPlace = new TickTimer(10),
            lastToggleFlight = new TickTimer(10),
            lastWorldUnload = new TickTimer(20),
            lastHalfBlock = new TickTimer(20);

    public double velocityX, velocityY, velocityZ;
    public double mvx, mvy, mvz;
    public boolean takingVelocity;

    public KLocation from = new KLocation(0,0,0), to = new KLocation(0,0,0),
            groundLoc;
}
