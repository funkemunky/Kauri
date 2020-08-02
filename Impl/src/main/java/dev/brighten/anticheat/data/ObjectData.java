package dev.brighten.anticheat.data;

import cc.funkemunky.api.handlers.ForgeHandler;
import cc.funkemunky.api.handlers.ModData;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutTransaction;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.math.RollingAverageLong;
import cc.funkemunky.api.utils.math.cond.MaxInteger;
import cc.funkemunky.api.utils.objects.evicting.ConcurrentEvictingList;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.CancelType;
import dev.brighten.anticheat.check.api.Config;
import dev.brighten.anticheat.data.classes.BlockInformation;
import dev.brighten.anticheat.data.classes.CheckManager;
import dev.brighten.anticheat.data.classes.PlayerInformation;
import dev.brighten.anticheat.data.classes.PredictionService;
import dev.brighten.anticheat.listeners.PacketListener;
import dev.brighten.anticheat.processing.ClickProcessor;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.anticheat.processing.PotionProcessor;
import dev.brighten.anticheat.utils.PastLocation;
import dev.brighten.anticheat.utils.TickTimer;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class ObjectData {

    public UUID uuid;
    private Player player;
    public boolean alerts, devAlerts, sniffing, usingLunar;

    //Debugging
    public String debugging;
    public UUID debugged;

    public long creation, lagTicks, noLagTicks;
    public PastLocation pastLocation, targetPastLocation;
    public LivingEntity target;
    public SimpleCollisionBox box = new SimpleCollisionBox(), targetBounds = new SimpleCollisionBox();
    public ObjectData targetData;
    public CheckManager checkManager;
    public PlayerInformation playerInfo;
    public BlockInformation blockInfo;
    public LagInformation lagInfo;
    public PredictionService predictionService;
    public MovementProcessor moveProcessor;
    public PotionProcessor potionProcessor;
    public ClickProcessor clickProcessor;
    public int hashCode, currentTicks;
    public boolean banned;
    public ModData modData;
    public ProtocolVersion playerVersion = ProtocolVersion.UNKNOWN;
    public Set<Player> boxDebuggers = new HashSet<>();
    public List<PotionEffectType> potionEffects = new ArrayList<>(),
            effectsToChange = new ArrayList<>();
    public final Map<String, Long> keepAliveStamps = Collections.synchronizedMap(new HashMap<>());
    public final Map<String, Short> transactionActions = Collections.synchronizedMap(new HashMap<>());
    public ConcurrentEvictingList<CancelType> typesToCancel = new ConcurrentEvictingList<>(10);
    public final List<String> sniffedPackets = new CopyOnWriteArrayList<>();
    public BukkitTask task;
    private ExecutorService playerThread;

    public ObjectData(UUID uuid) {
        this.uuid = uuid;
        hashCode = uuid.hashCode();
        playerThread = Executors.newSingleThreadExecutor();

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

            if(System.currentTimeMillis() - lagInfo.lastTransPing > 2000L){
                TinyProtocolHandler.sendPacket(getPlayer(),
                        new WrappedOutTransaction(0, setTransactionAction("ping"), false)
                                .getObject());
            }
            if(Config.kickForLunar18 && usingLunar
                    && !playerVersion.equals(ProtocolVersion.UNKNOWN)
                    && playerVersion.isAbove(ProtocolVersion.V1_8)) {
                RunUtils.task(() -> getPlayer().kickPlayer(Color.Red + "Lunar Client 1.8.9 is not allowed.\nJoin on 1.7.10 or any other client."));
            }
        }, Kauri.INSTANCE, 40L, 40L);
    }

    public ExecutorService getThread() {
        if(PacketListener.expansiveThreading)
            return playerThread;
        return Kauri.INSTANCE.executor;
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

    public long setKeepAliveStamp(String name) {
        synchronized (keepAliveStamps) {
            long stamp = getRandomLong(100, 4000);
            keepAliveStamps.put(name, stamp);

            return stamp;
        }
    }

    public long getKeepAliveStamp(String name) {
        return keepAliveStamps.getOrDefault(name, -1L);
    }

    public short getTransactionAction(String name) {
        return transactionActions.getOrDefault(name, (short) 0);
    }

    public short setTransactionAction(String name) {
        synchronized (transactionActions) {
            short action = getRandomShort(10, 500);

            transactionActions.put(name, action);

            return action;
        }
    }

    public void addPotionEffect(PotionEffectType type) {
        setTransactionAction("addpotion");
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
        public TickTimer lastPacketDrop = new TickTimer(ObjectData.this, 10),
                lastPingDrop = new TickTimer(ObjectData.this, 40);
        public RollingAverageLong pingAverages = new RollingAverageLong(10, 0);
        public long lastFlying = 0;
    }

    public void onLogout() {
        getThread().shutdown();
        task.cancel();
        keepAliveStamps.clear();
        transactionActions.clear();
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