package dev.brighten.anticheat.data;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.handlers.ForgeHandler;
import cc.funkemunky.api.handlers.ModData;
import cc.funkemunky.api.reflections.impl.MinecraftReflection;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.api.packets.channelhandler.TinyProtocol1_7;
import cc.funkemunky.api.tinyprotocol.api.packets.channelhandler.TinyProtocol1_8;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutTransaction;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.math.RollingAverageLong;
import cc.funkemunky.api.utils.math.cond.MaxInteger;
import cc.funkemunky.api.utils.objects.evicting.ConcurrentEvictingList;
import cc.funkemunky.api.utils.objects.evicting.ConcurrentEvictingMap;
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
import dev.brighten.anticheat.utils.EntityLocation;
import dev.brighten.anticheat.utils.PastLocation;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.data.Data;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.val;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class ObjectData implements Data {

    public UUID uuid;
    private Player player;
    public boolean sniffing, usingLunar;

    //Debugging
    public final Map<UUID, String> debugging = new HashMap<>();

    public long creation, lagTicks, noLagTicks;
    public PastLocation targetPastLocation, entityLocPastLocation;
    public LivingEntity target;
    public SimpleCollisionBox box = new SimpleCollisionBox();
    public ObjectData targetData;
    public EntityLocation entityLocation;
    public CheckManager checkManager;
    public PlayerInformation playerInfo;
    public Object playerConnection;
    public BlockInformation blockInfo;
    public LagInformation lagInfo;
    public PredictionService predictionService;
    public MovementProcessor moveProcessor;
    public PotionProcessor potionProcessor;
    public ClickProcessor clickProcessor;
    public int hashCode, playerTicks;
    public boolean banned, atlasBungeeInstalled;
    public ModData modData;
    public KLocation targetLoc;
    public ProtocolVersion playerVersion = ProtocolVersion.UNKNOWN;
    public final Set<Player> boxDebuggers = new HashSet<>();
    private final List<CollisionBox> lookingAtBoxes = Collections.synchronizedList(new ArrayList<>());
    public final List<Action> keepAliveStamps = new CopyOnWriteArrayList<>();
    public final ConcurrentEvictingList<CancelType> typesToCancel = new ConcurrentEvictingList<>(10);
    public final Map<Long, Long> keepAlives = Collections.synchronizedMap(new HashMap<>());
    public final List<String> sniffedPackets = new CopyOnWriteArrayList<>();
    public final Map<Location, CollisionBox> ghostBlocks = Collections.synchronizedMap(new HashMap<>());
    public final Map<Short, Runnable> instantTransaction = new HashMap<>();

    public ObjectData(UUID uuid) {
        this.uuid = uuid;
        hashCode = uuid.hashCode();

        player = Bukkit.getPlayer(uuid);

        playerInfo = new PlayerInformation(this);
        creation = playerInfo.lastRespawn = System.currentTimeMillis();
        blockInfo = new BlockInformation(this);
        clickProcessor = new ClickProcessor(this);
        lagInfo = new LagInformation();
        targetPastLocation = new PastLocation();
        entityLocPastLocation = new PastLocation();
        potionProcessor = new PotionProcessor(this);
        checkManager = new CheckManager(this);
        checkManager.addChecks();

        playerConnection = MinecraftReflection.getPlayerConnection(player);

        atlasBungeeInstalled = !Atlas.getInstance().getBungeeManager().isBungee()
                || Atlas.getInstance().getBungeeManager().isAtlasBungeeInstalled();

        //Alerts from database update
        if(getPlayer().hasPermission("kauri.command.alerts")) {
            Kauri.INSTANCE.loggerManager.storage.alertsStatus(uuid, result -> {
                if(result) {
                    synchronized (Kauri.INSTANCE.dataManager.hasAlerts) {
                        Kauri.INSTANCE.dataManager.hasAlerts.add(uuid.hashCode());
                    }
                    getPlayer().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("alerts-on",
                            "&aYou are now viewing cheat alerts."));
                }
            });
        }

        if(getPlayer().hasPermission("kauri.command.alerts.dev")) {
            Kauri.INSTANCE.loggerManager.storage.devAlertsStatus(uuid, result -> {
                if(result) {
                    synchronized (Kauri.INSTANCE.dataManager.devAlerts) {
                        Kauri.INSTANCE.dataManager.devAlerts.add(uuid.hashCode());
                    }
                    getPlayer().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("dev-alerts-on",
                            "&aYou are now viewing developer cheat alerts."));
                }
            });
        }

        predictionService = new PredictionService(this);
        moveProcessor = new MovementProcessor(this);

        modData = ForgeHandler.getMods(getPlayer());
        RunUtils.taskLaterAsync(() -> {
            modData = ForgeHandler.getMods(getPlayer());
        }, Kauri.INSTANCE, 100L);
        Kauri.INSTANCE.executor.execute(() -> {
            playerVersion = TinyProtocolHandler.getProtocolVersion(getPlayer());
        });

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
        keepAliveStamps.clear();
        Kauri.INSTANCE.dataManager.hasAlerts.remove(uuid.hashCode());
        Kauri.INSTANCE.dataManager.devAlerts.remove(uuid.hashCode());
        checkManager.checkMethods.clear();
        checkManager.checks.clear();
        checkManager = null;
        typesToCancel.clear();
        sniffedPackets.clear();
        keepAliveStamps.clear();
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    public ExecutorService getThread() {
        return Kauri.INSTANCE.executor;
    }

    @Override
    public boolean isUsingLunar() {
        return usingLunar;
    }

    public synchronized List<CollisionBox> getLookingAtBoxes() {
        return lookingAtBoxes;
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

    public void runInstantAction(Runnable runnable) {
        runInstantAction(runnable, false);
    }

    public void runInstantAction(Runnable runnable, boolean flush) {
        short id = (short) ThreadLocalRandom.current().nextInt(Short.MIN_VALUE, Short.MAX_VALUE);

        //Ensuring we don't have any duplicate IDS
        if(Kauri.INSTANCE.keepaliveProcessor.keepAlives.containsKey(id)) {
            id++;
        }

        final Object packet = new WrappedOutTransaction(0, id, false).getObject();

        if(!flush) TinyProtocolHandler.sendPacket(getPlayer(), packet);
        else sendPacket(packet);

        instantTransaction.put(id, runnable);
    }

    public void sendPacket(Object packet) {
        if(ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_8)) {
            TinyProtocol1_8 tp = (TinyProtocol1_8) TinyProtocolHandler.getInstance();
            tp.sendPacket(tp.getChannel(player), packet);
        } else {
            TinyProtocol1_7 tp = (TinyProtocol1_7) TinyProtocolHandler.getInstance();
            tp.sendPacket(tp.getChannel(player), packet);
        }
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
                .map(uuid -> Kauri.INSTANCE.dataManager.dataMap.get(uuid.hashCode())).filter(Objects::nonNull)
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