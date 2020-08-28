package dev.brighten.anticheat.data.classes;

import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumAnimation;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.TickTimer;
import lombok.NoArgsConstructor;
import org.bukkit.block.Block;

@NoArgsConstructor
public class PlayerInformation {
    public boolean serverGround, lClientGround, clientGround, nearGround,
            collided, insideBlock, phaseFlagged,
            onLadder, isClimbing, usingItem, wasOnIce, wasOnSlime, jumped, inAir, lworldLoaded, worldLoaded;
    public boolean generalCancel, flightCancel;
    public float fallDistance;
    public double deltaY, lDeltaY, deltaX, lDeltaX, deltaZ, lDeltaZ, deltaXZ, lDeltaXZ,
            jumpHeight, totalHeight, baseSpeed;
    public float headYaw, headPitch;
    public float deltaYaw, deltaPitch, lDeltaYaw, lDeltaPitch;
    public long lastVelocityTimestamp;
    public Block blockBelow, blockOnTo;

    public PlayerInformation(ObjectData data) {

        liquidTimer = new TickTimer(50);
        webTimer = new TickTimer(40);
        climbTimer = new TickTimer(40);
        slimeTimer = new TickTimer(75);
        iceTimer = new TickTimer(45);
        blockAboveTimer = new TickTimer(50);
        soulSandTimer = new TickTimer(40);
        lastBrokenBlock = new TickTimer(5);
        lastVelocity = new TickTimer(20);
        lastTargetSwitch = new TickTimer(3);
        lastBlockPlace = new TickTimer(10);
        lastToggleFlight = new TickTimer(10);
        lastChunkUnloaded = new TickTimer(20);
        lastWindowClick = new TickTimer(20);
        lastInsideBlock = new TickTimer(5);
        lastHalfBlock = new TickTimer(20);
        lastPlaceLiquid = new TickTimer(20);
        lastUseItem = new TickTimer(15);
        lastTeleportTimer = new TickTimer(10);
        lastGamemodeTimer = new TickTimer(10);
        lastRespawnTimer = new TickTimer(20);
        lastAttack = new TickTimer(5);
        cinematicTimer = new TickTimer(8);
    }

    //Cinematic
    public boolean cinematicMode;

    //Gcd
    public int yawGCD, pitchGCD, lastYawGCD, lastPitchGCD;

    //Server Position
    public long lastServerPos, lastRespawn;
    public boolean serverPos;
    public EvictingList<KLocation> posLocs = new EvictingList<>(5);

    //Attack
    public TickTimer lastAttack;
    public long lastAttackTimeStamp;

    //actions
    public boolean sneaking, sprinting, ridingJump, breakingBlock, flying, canFly, creative, inVehicle,
            gliding, riptiding, inventoryOpen, serverAllowedFlight;
    public int inventoryId = 0;

    //Keepalives
    public int velocityKeepalive, teleportKeepalive;

    //ticks
    public int groundTicks, airTicks;
    public TickTimer liquidTimer, webTimer, climbTimer, slimeTimer, iceTimer, blockAboveTimer, soulSandTimer;
    public TickTimer lastBrokenBlock, lastVelocity, lastTargetSwitch, lastBlockPlace, lastToggleFlight,
            lastWindowClick, lastInsideBlock, lastHalfBlock, lastPlaceLiquid, lastUseItem,
            lastTeleportTimer, lastGamemodeTimer, lastRespawnTimer, lastChunkUnloaded, cinematicTimer;

    public double velocityX, velocityY, velocityZ;

    public WrappedEnumAnimation animation = WrappedEnumAnimation.NONE;

    public KLocation from = new KLocation(0,0,0), to = new KLocation(0,0,0),
            groundLoc;
}
