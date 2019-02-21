package cc.funkemunky.anticheat.api.data;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckSettings;
import cc.funkemunky.anticheat.api.data.processors.ActionProcessor;
import cc.funkemunky.anticheat.api.data.processors.MovementProcessor;
import cc.funkemunky.anticheat.api.data.processors.VelocityProcessor;
import cc.funkemunky.anticheat.api.utils.*;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.utils.BoundingBox;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
    private boolean debuggingBox;
    private Player player;
    private List<Check> checks = new ArrayList<>();
    private Map<String, List<Check>> packetChecks = new HashMap<>();
    private Map<Class, List<Check>> bukkitChecks = new HashMap<>();
    private CancelType cancelType = CancelType.NONE;
    private boolean ableToFly, creativeMode, invulnerable, flying, generalCancel, breakingBlock,
            cinematicMode, lagging, alertsEnabled;
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
    private LivingEntity target;
    private long transPing, lastTransaction, lastTransPing, ping, lastPing, lastKeepAlive;
    private MCSmooth yawSmooth = new MCSmooth(), pitchSmooth = new MCSmooth();
    private CustomLocation entityFrom, entityTo;
    private PastLocation entityPastLocation = new PastLocation();

    /* Processors */
    private MovementProcessor movementProcessor;
    private ActionProcessor actionProcessor;
    private VelocityProcessor velocityProcessor;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.player = Bukkit.getPlayer(uuid);
        lastLogin.reset();

        if(CheckSettings.enableOnJoin && player.hasPermission("Kauri.alerts")) alertsEnabled = true;

        actionProcessor = new ActionProcessor();
        velocityProcessor = new VelocityProcessor();
        movementProcessor = new MovementProcessor();

        Kauri.getInstance().getCheckManager().loadChecksIntoData(this);

        checks.stream().filter(check -> check.getClass().isAnnotationPresent(Packets.class)).forEach(check -> {
            Packets packets = check.getClass().getAnnotation(Packets.class);

            Arrays.stream(packets.packets()).forEach(packet -> {
                List<Check> checks = packetChecks.getOrDefault(packet, new ArrayList<>());

                checks.add(check);

                packetChecks.put(packet, checks);
            });
        });

        checks.stream().filter(check -> check.getClass().isAnnotationPresent(BukkitEvents.class)).forEach(check -> {
            BukkitEvents events = check.getClass().getAnnotation(BukkitEvents.class);

            Arrays.stream(events.events()).forEach(event -> {
                List<Check> checks = bukkitChecks.getOrDefault(event, new ArrayList<>());

                checks.add(check);

                bukkitChecks.put(event, checks);
            });
        });
        Atlas.getInstance().getSchedular().scheduleAtFixedRate(() -> {
            if(target != null && !target.isDead()) {
                entityFrom = entityTo;
                entityTo = new CustomLocation(target.getLocation());
                entityPastLocation.addLocation(entityTo);
            }
        }, 50L, 50L, TimeUnit.MILLISECONDS);
    }
}
