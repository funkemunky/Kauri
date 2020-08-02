package dev.brighten.anticheat.data.classes;

import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumAnimation;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.objects.evicting.ConcurrentEvictingList;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.TickTimer;
import lombok.NoArgsConstructor;
import org.bukkit.block.Block;

@NoArgsConstructor
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

    public PlayerInformation(ObjectData data) {

        liquidTimer = new TickTimer(data, 50); 
        webTimer = new TickTimer(data, 40);  
        climbTimer = new TickTimer(data, 40); 
        slimeTimer = new TickTimer(data, 75); 
        iceTimer = new TickTimer(data, 45); 
        blockAboveTimer = new TickTimer(data, 50);  
        soulSandTimer = new TickTimer(data, 40);
        lastBrokenBlock = new TickTimer(data, 5); 
        lastVelocity = new TickTimer(data, 20);
        lastTargetSwitch = new TickTimer(data, 3);
        lastBlockPlace = new TickTimer(data, 10);
        lastToggleFlight = new TickTimer(data, 10);
        lastWindowClick = new TickTimer(data, 20);
        lastInsideBlock = new TickTimer(data, 5);
        lastHalfBlock = new TickTimer(data, 20);
        lastPlaceLiquid = new TickTimer(data, 20);
        lastUseItem = new TickTimer(data, 15);
        lastTeleportTimer = new TickTimer(data, 10);
        lastGamemodeTimer = new TickTimer(data, 10);
        lastRespawnTimer = new TickTimer(data, 20);
        lastAttack = new TickTimer(data, 5);
    }

    //Cinematic
    public boolean cinematicMode;

    //Gcd
    public int yawGCD, pitchGCD, lastYawGCD, lastPitchGCD;

    //Server Position
    public long lastServerPos, lastRespawn;
    public boolean serverPos;
    public ConcurrentEvictingList<KLocation> posLocs = new ConcurrentEvictingList<>(5);

    //Attack
    public TickTimer lastAttack;
    public long lastAttackTimeStamp;

    //actions
    public boolean sneaking, sprinting, ridingJump, breakingBlock, flying, canFly, creative, inVehicle,
            gliding, riptiding, inventoryOpen;
    public int inventoryId = 0;

    //Keepalives
    public int velocityKeepalive, teleportKeepalive;

    //ticks
    public int groundTicks, airTicks;
    public TickTimer liquidTimer, webTimer, climbTimer, slimeTimer, iceTimer, blockAboveTimer, soulSandTimer;
    public TickTimer lastBrokenBlock, lastVelocity, lastTargetSwitch, lastBlockPlace, lastToggleFlight,
            lastWindowClick, lastInsideBlock, lastHalfBlock, lastPlaceLiquid, lastUseItem,
            lastTeleportTimer, lastGamemodeTimer, lastRespawnTimer;

    public double velocityX, velocityY, velocityZ;

    public WrappedEnumAnimation animation = WrappedEnumAnimation.NONE;

    public KLocation from = new KLocation(0,0,0), to = new KLocation(0,0,0),
            groundLoc;
}
