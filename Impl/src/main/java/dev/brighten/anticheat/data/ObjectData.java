package dev.brighten.anticheat.data;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.utils.TickTimer;
import cc.funkemunky.api.utils.math.RollingAverageLong;
import cc.funkemunky.api.utils.math.cond.MaxInteger;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.classes.BlockInformation;
import dev.brighten.anticheat.data.classes.CheckManager;
import dev.brighten.anticheat.data.classes.PlayerInformation;
import dev.brighten.anticheat.data.classes.PredictionService;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.anticheat.utils.PastLocation;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;

public class ObjectData {

    public UUID uuid;
    private Player player;
    public boolean alerts;

    //Debugging
    public String debugging;
    public UUID debugged;

    public long creation, lagTicks, noLagTicks;
    public PastLocation pastLocation, targetPastLocation;
    public LivingEntity target;
    public SimpleCollisionBox box, targetBounds;
    public ObjectData INSTANCE, targetData;
    public CheckManager checkManager;
    public PlayerInformation playerInfo;
    public BlockInformation blockInfo;
    public LagInformation lagInfo;
    public PredictionService predictionService;
    public MovementProcessor moveProcessor;
    public int hashCode;
    public ProtocolVersion playerVersion = ProtocolVersion.UNKNOWN;
    public Set<Player> boxDebuggers = new HashSet<>();

    public ObjectData(UUID uuid) {
        this.uuid = uuid;
        hashCode = uuid.hashCode();
        INSTANCE = this;
        if(alerts = getPlayer().hasPermission("kauri.command.alerts")) {
            Kauri.INSTANCE.dataManager.hasAlerts.add(this);
        }
        creation = System.currentTimeMillis();
        playerInfo = new PlayerInformation();
        blockInfo = new BlockInformation(this);
        pastLocation = new PastLocation();
        lagInfo = new LagInformation();
        targetPastLocation = new PastLocation();
        checkManager = new CheckManager(this);
        checkManager.addChecks();
        predictionService = new PredictionService(this);
        moveProcessor = new MovementProcessor(this);
        Kauri.INSTANCE.executor.execute(() -> {
            playerVersion = TinyProtocolHandler.getProtocolVersion(getPlayer());
        });
    }

    public Player getPlayer() {
        if(player == null) {
            this.player = Bukkit.getPlayer(uuid);
        }
        return this.player;
    }

    public class LagInformation {
        public long lastKeepAlive, lastTrans, lastClientTrans;
        public long ping, averagePing, transPing, lastPing, lastTransPing;
        public MaxInteger lagTicks = new MaxInteger(25);
        public boolean lagging;
        public TickTimer lastPacketDrop = new TickTimer(10),
                lastPingDrop = new TickTimer(40);
        public RollingAverageLong pingAverages = new RollingAverageLong(10, 0);
        public long lastFlying = 0;
    }


    public void onLogout() {
        Kauri.INSTANCE.dataManager.hasAlerts.remove(this);
        Kauri.INSTANCE.dataManager.debugging.remove(this);
    }

    public static void debugBoxes(boolean debugging, Player debugger) {
        debugBoxes(debugging, debugger, new ObjectData[0]);
    }
    public static void debugBoxes(boolean debugging, Player debugger, ObjectData... targets) {
        if(!debugging) {
            List<ObjectData> toRemove = targets.length == 0
                    ? new ArrayList<>(Kauri.INSTANCE.dataManager.dataMap.values()) : Arrays.asList(targets);

            toRemove.stream()
                    .filter(d -> d.boxDebuggers.contains(debugger))
                    .forEach(d -> d.boxDebuggers.remove(debugger));
        } else if(targets.length > 0) {
            Arrays.stream(targets).forEach(d -> d.boxDebuggers.add(debugger));
        }
    }

    public static void debugBoxes(boolean debugging, Player debugger, UUID... targets) {
        debugBoxes(debugging, debugger, Arrays.stream(targets)
                .map(uuid -> Kauri.INSTANCE.dataManager.dataMap.get(uuid)).filter(Objects::nonNull)
                .toArray(ObjectData[]::new));
    }

    public static void debugBoxes(boolean debugging, Player debugger, String... targets) {
        debugBoxes(debugging, debugger, (ObjectData[])Arrays.stream(targets).map(Bukkit::getPlayer)
                .map(Kauri.INSTANCE.dataManager::getData).toArray(ObjectData[]::new));
    }
}