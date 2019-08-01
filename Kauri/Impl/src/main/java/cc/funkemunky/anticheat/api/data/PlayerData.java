package cc.funkemunky.anticheat.api.data;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.data.processors.ActionProcessor;
import cc.funkemunky.anticheat.api.data.processors.MovementProcessor;
import cc.funkemunky.anticheat.api.data.processors.SwingProcessor;
import cc.funkemunky.anticheat.api.data.processors.VelocityProcessor;
import cc.funkemunky.anticheat.api.pup.AntiPUP;
import cc.funkemunky.anticheat.api.utils.*;
import cc.funkemunky.anticheat.impl.config.CheckSettings;
import cc.funkemunky.anticheat.impl.config.MiscSettings;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.TickTimer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class PlayerData {
    public Location setbackLocation;
    private UUID uuid, debuggingPlayer;
    private Check debuggingCheck;
    private AlertTier alertTier = AlertTier.HIGH;
    private String specificPacketDebug = "";
    private boolean debuggingBox, debuggingPackets, banned = false, developerAlerts,
            ableToFly, creativeMode, invulnerable, flying, generalCancel, breakingBlock,
            cinematicMode, lagging, alertsEnabled, inventoryOpen, isPosition, loggedIn;
    private Player player;

    private List<Check> checks = new ArrayList<>();
    private List<AntiPUP> antiPUP = new ArrayList<>();

    private CancelType cancelType = CancelType.NONE;
    private Vector lastVelocityVector;
    private BoundingBox boundingBox;
    private TickTimer lastMovementCancel = new TickTimer(4),
            lastLag = new TickTimer(20),
            lastLogin = new TickTimer(60),
            lastBlockPlace = new TickTimer(30),
            lastFlag = new TickTimer(40),
            lastAttack = new TickTimer(4),
            lastBlockBreak = new TickTimer(3),
            lastPacketSkip = new TickTimer(5);
    private EvictingList<Vector> teleportLocations;
    private float walkSpeed, flySpeed;
    private LivingEntity target, attacker;
    private long transPing, lastTransaction, lastTransPing, ping, lastPing, lastKeepAlive, lastServerPosStamp, lastFlagTimestamp, lastPacketDrop;
    private MCSmooth yawSmooth = new MCSmooth(), pitchSmooth = new MCSmooth();
    private CustomLocation entityFrom, entityTo, positionLoc;
    private PastLocation entityPastLocation = new PastLocation();
    private Block blockBelow, blockAbove, blockInside;
    private Location teleportLoc;
    private long teleportTest, teleportPing;

    /* Processors */
    private MovementProcessor movementProcessor;
    private ActionProcessor actionProcessor;
    private VelocityProcessor velocityProcessor;
    private SwingProcessor swingProcessor;

    /* Combined Autoclicker Check */
    private Verbose typeC = new Verbose(), typeD = new Verbose(), typeH = new Verbose();

    private long lastDig;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.player = Bukkit.getPlayer(uuid);
        lastLogin.reset();
        loggedIn = true;
        teleportLocations = new EvictingList<>(5);

        if (CheckSettings.enableOnJoin && player.hasPermission("kauri.alerts")) alertsEnabled = true;

        actionProcessor = new ActionProcessor();
        velocityProcessor = new VelocityProcessor(this);
        movementProcessor = new MovementProcessor();
        swingProcessor = new SwingProcessor(this);

        Kauri.getInstance().getCheckManager().loadChecksIntoData(this);
        Kauri.getInstance().getAntiPUPManager().loadMethodsIntoData(this);

        Atlas.getInstance().getSchedular().scheduleAtFixedRate(() -> {
            if(target != null) {
                setEntityFrom(getEntityTo());
                setEntityTo(new CustomLocation(getTarget().getLocation()));
                getEntityPastLocation().addLocation(getEntityTo());
            }
        }, 50L, 50L, TimeUnit.MILLISECONDS);
    }

    public boolean isServerPos() {
        return System.currentTimeMillis() - lastServerPosStamp < Math.max(50, MiscSettings.serverPos + (transPing));
    }
}
