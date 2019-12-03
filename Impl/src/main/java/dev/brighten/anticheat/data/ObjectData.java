package dev.brighten.anticheat.data;

import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.TickTimer;
import cc.funkemunky.api.utils.math.RollingAverageLong;
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
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;

import java.util.UUID;

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
    public BoundingBox box, targetBounds;
    public ObjectData INSTANCE;
    public CheckManager checkManager;
    public PlayerInformation playerInfo;
    public BlockInformation blockInfo;
    public LagInformation lagInfo;
    public PredictionService predictionService;
    public MovementProcessor moveProcessor;
    public int hashCode;
    public ProtocolVersion playerVersion = ProtocolVersion.UNKNOWN;

    public ObjectData(UUID uuid) {
        this.uuid = uuid;
        hashCode = uuid.hashCode();
        INSTANCE = this;
        if(alerts = getPlayer().hasPermission("kauri.alerts")) {
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
        public long lastKeepAlive, lastTrans;
        public long ping, averagePing, transPing, lastPing, lastTransPing;
        public boolean lagging;
        public TickTimer lastPacketDrop = new TickTimer(10), lastPingDrop = new TickTimer(40);
        public RollingAverageLong pingAverages = new RollingAverageLong(10, 0);
        public long lastFlying = 0;
    }


    public void onLogout() {
        Kauri.INSTANCE.dataManager.hasAlerts.remove(this);
        Kauri.INSTANCE.dataManager.debugging.remove(this);
    }
}