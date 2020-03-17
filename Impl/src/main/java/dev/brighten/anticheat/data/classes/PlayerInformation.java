package dev.brighten.anticheat.data.classes;

import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumAnimation;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.TickTimer;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.utils.CollisionHandler;
import dev.brighten.anticheat.utils.MouseFilter;
import org.bukkit.block.Block;

public class PlayerInformation {
    public boolean serverGround, lClientGround, clientGround, nearGround,
            collided, insideBlock,
            onLadder, isClimbing, usingItem, wasOnIce, wasOnSlime, jumped, inAir, lworldLoaded, worldLoaded;
    public boolean generalCancel, flightCancel;
    public float fallDistance;
    public double deltaY, lDeltaY, deltaX, lDeltaX, deltaZ, lDeltaZ, deltaXZ, lDeltaXZ,
            jumpHeight, totalHeight, baseSpeed;
    public float headYaw, headPitch;
    public float deltaYaw, deltaPitch, lDeltaYaw, lDeltaPitch;
    public long lastVelocityTimestamp;
    public Block blockBelow, blockOnTo;

    //Cinematic
    public boolean cinematicMode;

    //Gcd
    public double yawGCD, pitchGCD, lastYawGCD, lastPitchGCD;

    //Server Position
    public long lastServerPos, lastRespawn;
    public boolean serverPos;
    public EvictingList<KLocation> posLocs = new EvictingList<>(5);

    //Attack
    public TickTimer lastAttack = new TickTimer(5);
    public long lastAttackTimeStamp;

    //actions
    public boolean sneaking, sprinting, ridingJump, breakingBlock, flying, canFly, creative, inVehicle,
            gliding, riptiding, inventoryOpen;
    public int inventoryId = 0;

    //Keepalives
    public int velocityKeepalive, teleportKeepalive;

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
            lastInsideBlock = new TickTimer(5),
            lastHalfBlock = new TickTimer(20),
            lastPlaceLiquid = new TickTimer(20),
            lastUseItem = new TickTimer(15),
            lastRespawnTimer = new TickTimer(20);

    public double velocityX, velocityY, velocityZ;
    public boolean lookingAtBlock;

    public WrappedEnumAnimation animation = WrappedEnumAnimation.NONE;

    public KLocation from = new KLocation(0,0,0), to = new KLocation(0,0,0),
            groundLoc;
}
