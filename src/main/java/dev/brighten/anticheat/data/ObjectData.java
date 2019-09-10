package dev.brighten.anticheat.data;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.NMSObject;
import cc.funkemunky.api.tinyprotocol.api.packets.reflections.types.WrappedClass;
import cc.funkemunky.api.tinyprotocol.api.packets.reflections.types.WrappedMethod;
import cc.funkemunky.api.utils.*;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckSettings;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.EntityProcessor;
import dev.brighten.anticheat.utils.CollisionHandler;
import dev.brighten.anticheat.utils.KLocation;
import dev.brighten.anticheat.utils.MouseFilter;
import dev.brighten.anticheat.utils.PastLocation;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ObjectData {

    public UUID uuid;
    private Player player;
    public boolean alerts;

    //Debugging
    public String debugging;
    public UUID debugged;

    public TickTimer creation;
    public PastLocation pastLocation,
            targetPastLocation;
    public LivingEntity target;
    public BoundingBox box, targetBounds;
    public ObjectData INSTANCE;
    public CheckManager checkManager;
    public PlayerInformation playerInfo;
    public BlockInformation blockInfo;
    public LagInformation lagInfo;
    public List<LivingEntity> entitiesNearPlayer = new ArrayList<>();

    public ObjectData(UUID uuid) {
        this.uuid = uuid;
        INSTANCE = this;
        creation = new TickTimer(10);
        alerts = getPlayer().hasPermission("kauri.alerts");
        creation.reset();
        playerInfo = new PlayerInformation();
        blockInfo = new BlockInformation();
        lagInfo = new LagInformation();
        pastLocation = new PastLocation();
        targetPastLocation = new PastLocation();
        checkManager = new CheckManager();
        checkManager.addChecks();
    }

    public Player getPlayer() {
        if(player == null) {
            this.player = Bukkit.getPlayer(uuid);
        }
        return this.player;
    }

    public class CheckManager {
        public Map<String, Check> checks = new HashMap<>();
        public Map<Class<?>, List<Map.Entry<String, WrappedMethod>>> checkMethods = new HashMap<>();

        public void runPacket(NMSObject object) {
            if(!checkMethods.containsKey(object.getClass())) return;
            checkMethods.get(object.getClass()).parallelStream().forEach(entry -> {
                Check check = checks.get(entry.getKey());
                Kauri.INSTANCE.profiler.start("check:" + check.name);

                if(check.enabled) {
                    try {
                        entry.getValue().getMethod().invoke(check, object);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        System.out.println("Error on " + check.name);
                        e.printStackTrace();
                    }
                }
                Kauri.INSTANCE.profiler.stop("check:" + check.name);
            });
        }

        protected void addChecks() {
            Check.checkClasses.keySet().parallelStream()
                    .map(clazz -> {
                        CheckInfo settings = Check.checkClasses.get(clazz);
                        Check check = clazz.getConstructor().newInstance();
                        check.data = INSTANCE;
                        CheckSettings checkSettings = Check.checkSettings.get(clazz);
                        check.enabled = checkSettings.enabled;
                        check.executable = checkSettings.executable;
                        check.name = settings.name();
                        check.description = settings.description();
                        return check;
                    })
                    .sequential()
                    .forEach(check -> checks.put(check.name, check));

            for (String name : checks.keySet()) {
                Check check = checks.get(name);
                WrappedClass checkClass = new WrappedClass(check.getClass());
                System.out.println("Added check: " + name);
                Arrays.stream(check.getClass().getDeclaredMethods())
                        .parallel()
                        .filter(method -> method.isAnnotationPresent(Packet.class))
                        .map(method -> new WrappedMethod(checkClass, method))
                        .sequential()
                        .forEach(method -> {
                            Class<?> parameter = method.getParameters().get(0);
                            List<Map.Entry<String, WrappedMethod>> methods = checkMethods.getOrDefault(
                                    parameter,
                                    new ArrayList<>());

                            methods.add(new AbstractMap.SimpleEntry<>(
                                    check.name, method));

                            System.out.println("added packet method: " + method.getName());
                            checkMethods.put(parameter, methods);
                        });
            }
        }
    }

    public class PlayerInformation {
        public boolean serverGround, lServerGround, clientGround, nearGround, collidedGround,
                collidesVertically, collidesHorizontally, canFly, inCreative;
        public boolean generalCancel, flightCancel;
        public boolean wasOnIce, wasOnSlime, jumped, inAir, breakingBlock;
        public float deltaY, lDeltaY, deltaX, lDeltaX, deltaZ, lDeltaZ, deltaXZ, lDeltaXZ, pDeltaXZ, lpDeltaXZ, prePDeltaY;
        public float pDeltaY, pDeltaX, pDeltaZ, lpDeltaX, lpDeltaY, lpDeltaZ;
        public float deltaYaw, deltaPitch, lDeltaYaw, lDeltaPitch;
        public float fallDistance;

        //Move
        public float strafe, forward;
        public String key;

        //Cinematic
        public float cinematicYaw, cinematicPitch;
        public boolean cinematicModeYaw, cinematicModePitch;
        public MouseFilter yawSmooth = new MouseFilter(), pitchSmooth = new MouseFilter(),
                mouseFilterX = new MouseFilter(), mouseFilterY = new MouseFilter();

        //Gcd
        public float yawGCD, pitchGCD, lastYawGCD, lastPitchGCD;

        //Server Position
        public long lastServerPos;
        public boolean serverPos;
        public EvictingList<KLocation> posLocs = new EvictingList<>(5);

        //Attack
        public TickTimer lastAttack = new TickTimer(5);

        //actions
        public boolean sneaking, sprinting, ridingJump;

        //ticks
        public int liquidTicks, groundTicks, airTicks, halfBlockTicks, webTicks, climbTicks, slimeTicks, iceTicks;
        public TickTimer lastBrokenBlock = new TickTimer(5),
                lastVelocity = new TickTimer(20);

        public KLocation from, to;
    }

    public class BlockInformation {
        public boolean onClimbable, onSlab, onStairs, onHalfBlock, inLiquid, inWeb, onSlime, onIce, onSoulSand;
        public void runCollisionCheck() {
            if(creation.hasNotPassed(2)) return; //Prevents errors, especially on plugin reloads.
            CollisionHandler handler = new CollisionHandler(ObjectData.this);

            List<BoundingBox> boxes = Atlas.getInstance().getBlockBoxManager().getBlockBox()
                    .getCollidingBoxes(getPlayer().getWorld(), box.grow(1f,1f,1f));

            //Running block checking;
            boxes.parallelStream().forEach(box -> {
                Block block = BlockUtils.getBlock(box.getMinimum().toLocation(getPlayer().getWorld()));

                if(block != null) {
                    handler.onCollide(block, box, false);
                }
            });

                //Running entity boundingBox check.
            EntityProcessor.vehicles.get(getPlayer().getWorld().getUID())
                        .stream()
                        .filter(entity -> entity.getLocation().distance(getPlayer().getLocation()) < 1.5)
                        .map(entity -> ReflectionsUtil.toBoundingBox(ReflectionsUtil.getBoundingBox(entity)))
                        .forEach(box -> handler.onCollide(null, box, true));

            playerInfo.serverGround = handler.onGround;
            playerInfo.nearGround = handler.nearGround;
            playerInfo.collidesHorizontally = handler.collidesHorizontally;
            playerInfo.collidesVertically = handler.collidesVertically;
            onSlab = handler.onSlab;
            onStairs = handler.onStairs;
            onHalfBlock = handler.onHalfBlock;
            inLiquid = handler.inLiquid;
            inWeb = handler.inWeb;
            onSlime = handler.onSlime;
            onIce = handler.onIce;
            onSoulSand = handler.onSoulSand;
        }
    }

    public class LagInformation {
        public long lastKeepAlive, lastTrans;
        public long ping, transPing, lastPing, lastTransPing;
        public boolean lagging;
        public TickTimer lastPacketDrop = new TickTimer(10), lastPingDrop = new TickTimer(40);
        public long lastFlying;
    }
}