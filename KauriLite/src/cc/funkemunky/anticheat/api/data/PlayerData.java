package cc.funkemunky.anticheat.api.data;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckSettings;
import cc.funkemunky.anticheat.api.data.processors.ActionProcessor;
import cc.funkemunky.anticheat.api.data.processors.MovementProcessor;
import cc.funkemunky.anticheat.api.data.processors.VelocityProcessor;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.MCSmooth;
import cc.funkemunky.anticheat.api.utils.PastLocation;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.TickTimer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class PlayerData {
    public Location setbackLocation;
    private UUID uuid, debuggingPlayer;
    private Check debuggingCheck;
    private String specificPacketDebug = "";
    private boolean debuggingBox, debuggingPackets, banned = false, developerAlerts,
            ableToFly, creativeMode, invulnerable, flying, generalCancel, breakingBlock,
            cinematicMode, lagging, alertsEnabled, inventoryOpen;
    private Player player;

    private Map<String, List<Check>> packetChecks = new HashMap<>();
    private Map<Class, List<Check>> bukkitChecks = new HashMap<>();

    private CancelType cancelType = CancelType.NONE;
    private Vector lastVelocityVector;
    private BoundingBox boundingBox;
    private TickTimer lastMovementCancel = new TickTimer(4),
            lastServerPos = new TickTimer(8),
            lastLag = new TickTimer(20),
            lastLogin = new TickTimer(60),
            lastBlockPlace = new TickTimer(30),
            lastFlag = new TickTimer(40),
            lastAttack = new TickTimer(4);
    private float walkSpeed, flySpeed;
    private LivingEntity target, attacker;
    private long transPing, lastTransaction, lastTransPing, ping, lastPing, lastKeepAlive, lastServerPosStamp;
    private MCSmooth yawSmooth = new MCSmooth(), pitchSmooth = new MCSmooth();
    private CustomLocation entityFrom, entityTo;
    private PastLocation entityPastLocation = new PastLocation();

    /* Processors */
    private MovementProcessor movementProcessor;
    private ActionProcessor actionProcessor;
    private VelocityProcessor velocityProcessor;

    private long lastDig;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.player = Bukkit.getPlayer(uuid);
        lastLogin.reset();

        if (CheckSettings.enableOnJoin && player.hasPermission("kauri.alerts")) alertsEnabled = true;

        actionProcessor = new ActionProcessor();
        velocityProcessor = new VelocityProcessor(this);
        movementProcessor = new MovementProcessor();

        Kauri.getInstance().getCheckManager().loadChecksIntoData(this);


        Atlas.getInstance().getSchedular().scheduleAtFixedRate(() -> {
            if (target != null && !target.isDead()) {
                entityFrom = entityTo;
                entityTo = new CustomLocation(target.getLocation());
                entityPastLocation.addLocation(entityTo);
            }
        }, 50L, 50L, TimeUnit.MILLISECONDS);
    }

    public boolean isServerPos() {
        return System.currentTimeMillis() - lastServerPosStamp < 100;
    }
}
