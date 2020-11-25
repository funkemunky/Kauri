package dev.brighten.anticheat.data.classes;

import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumAnimation;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.PlayerTimer;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public class PlayerInformation {
    public boolean serverGround, lClientGround, clientGround, nearGround,
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

    public PlayerInformation(ObjectData data) {

        liquidTimer = new PlayerTimer(data, 50);
        webTimer = new PlayerTimer(data, 40);
        climbTimer = new PlayerTimer(data, 40);
        slimeTimer = new PlayerTimer(data, 75);
        iceTimer = new PlayerTimer(data, 45);
        blockAboveTimer = new PlayerTimer(data, 50);
        soulSandTimer = new PlayerTimer(data, 40);
        lastBrokenBlock = new PlayerTimer(data, 5);
        lastVelocity = new PlayerTimer(data, 20);
        lastTargetSwitch = new PlayerTimer(data, 3);
        lastBlockPlace = new PlayerTimer(data, 10);
        lastToggleFlight = new PlayerTimer(data, 10);
        lastChunkUnloaded = new PlayerTimer(data, 20);
        lastWindowClick = new PlayerTimer(data, 20);
        lastInsideBlock = new PlayerTimer(data, 5);
        lastHalfBlock = new PlayerTimer(data, 20);
        lastPlaceLiquid = new PlayerTimer(data, 20);
        lastBlockDigPacket = new PlayerTimer(data, 5);
        lastBlockPlacePacket = new PlayerTimer(data, 5);
        lastUseItem = new PlayerTimer(data, 15);
        lastTeleportTimer = new PlayerTimer(data, 10);
        lastGamemodeTimer = new PlayerTimer(data, 10);
        lastRespawnTimer = new PlayerTimer(data, 20);
        lastAttack = new PlayerTimer(data, 5);
        cinematicTimer = new PlayerTimer(data, 8);
        lastEntityCollision = new PlayerTimer(data, 4);
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
            lastBlockDigPacket, lastToggleFlight, lastAttack, lastEntityCollision,
            lastWindowClick, lastInsideBlock, lastHalfBlock, lastPlaceLiquid, lastUseItem,
            lastTeleportTimer, lastGamemodeTimer, lastRespawnTimer, lastChunkUnloaded, cinematicTimer;

    public double velocityX, velocityY, velocityZ;

    public WrappedEnumAnimation animation = WrappedEnumAnimation.NONE;

    public KLocation from = new KLocation(0,0,0), to = new KLocation(0,0,0),
            groundLoc;
}
