package dev.brighten.anticheat.data.classes;

import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumAnimation;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.objects.evicting.ConcurrentEvictingList;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.PlayerTimer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.*;

@NoArgsConstructor
public class PlayerInformation {
    public boolean serverGround, lServerGround, lClientGround, clientGround, nearGround,
            collided, insideBlock, lookingAtBlock,
            onLadder, isClimbing, usingItem, wasOnIce, wasOnSlime, jumped, inAir, worldLoaded;
    public boolean generalCancel, flightCancel;
    public float fallDistance;
    public double deltaY, lDeltaY, deltaX, lDeltaX, deltaZ, lDeltaZ, deltaXZ, lDeltaXZ,
            jumpHeight, totalHeight, baseSpeed;
    public float headYaw, headPitch;
    public float deltaYaw, deltaPitch, lDeltaYaw, lDeltaPitch;
    public long lastVelocityTimestamp;
    public Map<Location, Material> shitMap = new HashMap<>();
    public Block blockBelow, blockOnTo;
    public Location setbackLocation = new Location(null, 0,0,0);

    public PlayerInformation(ObjectData data) {

        liquidTimer = new TickTimer(50);
        webTimer = new TickTimer(40);
        climbTimer = new TickTimer(40);
        slimeTimer = new TickTimer(75);
        iceTimer = new TickTimer(45);
        blockAboveTimer = new TickTimer(50);
        soulSandTimer =new TickTimer( 40);
        lastBrokenBlock = new TickTimer( 5);
        lastVelocity = new PlayerTimer(data);
        lastTargetSwitch = new TickTimer(3);
        lastBlockPlace = new TickTimer(10);
        lastToggleFlight = new TickTimer(10);
        lastChunkUnloaded = new TickTimer(20);
        lastWindowClick = new TickTimer(20);
        lastInsideBlock = new TickTimer(5);
        lastHalfBlock = new TickTimer(20);
        lastPlaceLiquid = new TickTimer(20);
        lastBlockDigPacket = new TickTimer(5);
        lastBlockPlacePacket = new TickTimer(5);
        lastUseItem = new TickTimer(15);
        lastTeleportTimer = new TickTimer(10);
        lastGamemodeTimer = new TickTimer(10);
        lastRespawnTimer = new TickTimer(20);
        lastAttack = new TickTimer(5);
        cinematicTimer = new TickTimer(8);
        lastEntityCollision = new TickTimer(4);
        lastMoveCancel = new TickTimer(15);
        lastGhostCollision = new TickTimer(5);
    }

    //Cinematic
    public boolean cinematicMode;

    //Gcd
    public int yawGCD, pitchGCD, lastYawGCD, lastPitchGCD;

    //Server Position
    public long lastServerPos, lastRespawn;
    public boolean serverPos;
    public final List<KLocation> posLocs = Collections.synchronizedList(new EvictingList<>(5));
    public final List<Vector> velocities = Collections.synchronizedList(new EvictingList<>(5));

    //Attack
    public long lastAttackTimeStamp;

    //actions
    public boolean sneaking, sprinting, ridingJump, breakingBlock, flying, canFly, creative, inVehicle,
            gliding, riptiding, inventoryOpen, serverAllowedFlight, doingVelocity, doingTeleport;
    public int inventoryId = 0;

    //Keepalives
    public int velocityKeepalive, teleportKeepalive;

    //ticks
    public int groundTicks, airTicks, kGroundTicks, kAirTicks;
    public Timer liquidTimer, webTimer, climbTimer, slimeTimer, iceTimer, blockAboveTimer, soulSandTimer;
    public Timer lastBrokenBlock, lastVelocity, lastTargetSwitch, lastBlockPlace, lastBlockPlacePacket,
            lastBlockDigPacket, lastToggleFlight, lastAttack, lastEntityCollision, lastMoveCancel,
            lastWindowClick, lastInsideBlock, lastHalfBlock, lastPlaceLiquid, lastUseItem, lastGhostCollision,
            lastTeleportTimer, lastGamemodeTimer, lastRespawnTimer, lastChunkUnloaded, cinematicTimer;

    public double velocityX, velocityY, velocityZ, calcVelocityX, calcVelocityY, calcVelocityZ;

    public WrappedEnumAnimation animation = WrappedEnumAnimation.NONE;

    public KLocation from = new KLocation(0,0,0), to = new KLocation(0,0,0),
            groundLoc;
}
