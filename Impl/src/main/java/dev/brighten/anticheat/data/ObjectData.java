package dev.brighten.anticheat.data;

import cc.funkemunky.api.handlers.ForgeHandler;
import cc.funkemunky.api.handlers.ModData;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.math.RollingAverageLong;
import cc.funkemunky.api.utils.math.cond.MaxInteger;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import cc.funkemunky.api.utils.world.CollisionBox;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Config;
import dev.brighten.anticheat.data.classes.BlockInformation;
import dev.brighten.anticheat.data.classes.CheckManager;
import dev.brighten.anticheat.data.classes.PlayerInformation;
import dev.brighten.anticheat.data.classes.PredictionService;
import dev.brighten.anticheat.processing.ClickProcessor;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.anticheat.processing.PotionProcessor;
import dev.brighten.anticheat.processing.keepalive.KeepAlive;
import dev.brighten.anticheat.utils.PastLocation;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.data.Data;
import lombok.AllArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ObjectData implements Data {

    public UUID uuid;
    private Player player;
    public boolean alerts, devAlerts, sniffing, usingLunar;

    //Debugging
    public String debugging;
    public UUID debugged;

    public long creation, lagTicks, noLagTicks;
    public PastLocation pastLocation, targetPastLocation;
    public LivingEntity target;
    public SimpleCollisionBox box = new SimpleCollisionBox();
    public ObjectData targetData;
    public CheckManager checkManager;
    public PlayerInformation playerInfo;
    public BlockInformation blockInfo;
    public LagInformation lagInfo;
    public PredictionService predictionService;
    public MovementProcessor moveProcessor;
    public PotionProcessor potionProcessor;
    public ClickProcessor clickProcessor;
    public int hashCode, playerTicks;
    public boolean banned;
    public ModData modData;
    public KLocation targetLoc;
    public ProtocolVersion playerVersion = ProtocolVersion.UNKNOWN;
    public Set<Player> boxDebuggers = new HashSet<>();
    public final List<Action> keepAliveStamps = new CopyOnWriteArrayList<>();
    public final List<CancelType> typesToCancel = Collections.synchronizedList(new EvictingList<>(10));
    public final Map<Long, Long> keepAlives = Collections.synchronizedMap(new HashMap<>());
    public final List<String> sniffedPackets = new CopyOnWriteArrayList<>();
    public final Map<Location, CollisionBox> ghostBlocks = Collections.synchronizedMap(new HashMap<>());
    public BukkitTask task;

    public ObjectData(UUID uuid) {
        this.uuid = uuid;
        hashCode = uuid.hashCode();

        if(!Config.testMode) {
            if(alerts = getPlayer().hasPermission("kauri.command.alerts"))
                Kauri.INSTANCE.dataManager.hasAlerts.add(this);
        }
        playerInfo = new PlayerInformation(this);
        creation = playerInfo.lastRespawn = System.currentTimeMillis();
        blockInfo = new BlockInformation(this);
        clickProcessor = new ClickProcessor(this);
        pastLocation = new PastLocation();
        lagInfo = new LagInformation();
        targetPastLocation = new PastLocation();
        potionProcessor = new PotionProcessor(this);
        checkManager = new CheckManager(this);
        checkManager.addChecks();

        predictionService = new PredictionService(this);
        moveProcessor = new MovementProcessor(this);

        modData = ForgeHandler.getMods(getPlayer());
        RunUtils.taskLaterAsync(() -> {
            modData = ForgeHandler.getMods(getPlayer());
        }, Kauri.INSTANCE, 100L);
        Kauri.INSTANCE.executor.execute(() -> {
            playerVersion = TinyProtocolHandler.getProtocolVersion(getPlayer());
        });
        if(task != null) task.cancel();
        task = RunUtils.taskTimerAsync(() -> {
            if(getPlayer() == null) {
                task.cancel();
                return;
            }

            if(Config.kickForLunar18 && usingLunar
                    && !playerVersion.equals(ProtocolVersion.UNKNOWN)
                    && playerVersion.isAbove(ProtocolVersion.V1_8)) {
                RunUtils.task(() -> getPlayer().kickPlayer(Color.Red + "Lunar Client 1.8.9 is not allowed.\nJoin on 1.7.10 or any other client."));
            }
        }, Kauri.INSTANCE, 40L, 40L);

        getPlayer().getActivePotionEffects().forEach(pe -> {
            runKeepaliveAction(d -> this.potionProcessor.potionEffects.add(pe));
        });
    }

    @Override
    public void reloadChecks() {
        unloadChecks();
        loadChecks();
    }

    @Override
    public void unloadChecks() {
        checkManager.checkMethods.clear();
        checkManager.checks.clear();
    }

    @Override
    public void loadChecks() {
        checkManager.addChecks();
    }

    public void unregister() {
        task.cancel();
        keepAliveStamps.clear();
        Kauri.INSTANCE.dataManager.hasAlerts.remove(this);
        Kauri.INSTANCE.dataManager.debugging.remove(this);
        checkManager.checkMethods.clear();
        checkManager.checks.clear();
        checkManager = null;
        typesToCancel.clear();
        sniffedPackets.clear();
        keepAliveStamps.clear();
        Kauri.INSTANCE.dataManager.dataMap.remove(uuid);
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Deprecated
    public ExecutorService getThread() {
        return Kauri.INSTANCE.executor;
    }

    @Override
    public boolean isUsingLunar() {
        return usingLunar;
    }

    @Override
    public ProtocolVersion getClientVersion() {
        return playerVersion;
    }

    @Override
    public ModData getForgeMods() {
        return modData;
    }

    public int[] getReceived() {
        int[] toReturn = new int[] {0, 0};
        val op = Kauri.INSTANCE.keepaliveProcessor.getResponse(this);

        if(op.isPresent()) {
            toReturn[0] = op.get().start;
            val op2 = op.get().getReceived(uuid);

            op2.ifPresent(kaReceived -> toReturn[1] = kaReceived.stamp);
        }

        return toReturn;
    }
    public short getRandomShort(int baseNumber, int bound) {
        return (short) getRandomInt(baseNumber, bound);
    }

    public int getRandomInt(int baseNumber, int bound) {
        return baseNumber + ThreadLocalRandom.current().nextInt(bound);
    }

    public long getRandomLong(long baseNumber, long bound) {
        return baseNumber + ThreadLocalRandom.current().nextLong(bound);
    }

    public int runKeepaliveAction(Consumer<KeepAlive> action) {
        return runKeepaliveAction(action, 0);
    }

    public int runKeepaliveAction(Consumer<KeepAlive> action, int later) {
        int id = Kauri.INSTANCE.keepaliveProcessor.currentKeepalive.start + later;

        keepAliveStamps.add(new Action(id, action));

        return id;
    }

    public Player getPlayer() {
        if(player == null) {
            this.player = Bukkit.getPlayer(uuid);
        }
        return this.player;
    }

    public class LagInformation {
        public long lastKeepAlive, lastTrans, lastClientTrans, ping, lastPing, averagePing,
                millisPing, lmillisPing, recieved, start;
        public int transPing, lastTransPing;
        public MaxInteger lagTicks = new MaxInteger(25);
        public boolean lagging;
        public Timer lastPacketDrop = new TickTimer(),
                lastPingDrop = new TickTimer();
        public RollingAverageLong pingAverages = new RollingAverageLong(10, 0);
        public long lastFlying = 0;
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

    @AllArgsConstructor
    public static class Action {
        public int stamp;
        public Consumer<KeepAlive> action;
    }
}