package dev.brighten.anticheat.data;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.NMSObject;
import cc.funkemunky.api.tinyprotocol.api.packets.reflections.types.WrappedClass;
import cc.funkemunky.api.tinyprotocol.api.packets.reflections.types.WrappedMethod;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.ReflectionsUtil;
import dev.brighten.anticheat.api.check.Check;
import dev.brighten.anticheat.api.check.CheckInfo;
import dev.brighten.anticheat.api.check.Packet;
import dev.brighten.anticheat.utils.CollisionHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ObjectData {

    public UUID uuid;
    private Player player;
    public boolean alerts;
    public long ping, transPing;
    public BoundingBox box;
    public ObjectData INSTANCE;
    public CheckManager checkManager = new CheckManager();
    public PlayerInformation information = new PlayerInformation();

    public ObjectData(UUID uuid) {
        this.uuid = uuid;
        INSTANCE = this;
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

                entry.getValue().invoke(check, object);
            });
        }

        protected void addChecks() {
            Check.checkClasses.keySet().stream()
                    .map(clazz -> {
                        CheckInfo settings = Check.checkClasses.get(clazz);
                        Check check = clazz.getConstructor().newInstance();
                        check.data = INSTANCE;
                        check.enabled = settings.enabled();
                        check.executable = settings.executable();
                        check.banVL = settings.maxVL();
                        check.name = settings.name();
                        check.description = settings.description();
                        return check;
                    })
                    .forEach(check -> checks.put(check.name, check));

            for (String name : checks.keySet()) {
                Check check = checks.get(name);
                System.out.println("Added check: " + name);
                Arrays.stream(check.getClass().getMethods())
                        .filter(method -> method.isAnnotationPresent(Packet.class))
                        .forEach(method -> {
                            List<Map.Entry<String, WrappedMethod>> methods = checkMethods.getOrDefault(
                                    method.getParameterTypes()[0],
                                    new ArrayList<>());

                            methods.add(new AbstractMap.SimpleEntry<>(
                                    check.name,
                                    new WrappedMethod(new WrappedClass(check.getClass()), method)));

                            Bukkit.broadcastMessage("added 1: " + method.getName());

                            checkMethods.put(method.getParameterTypes()[0], methods);
                        });
                Arrays.stream(check.getClass().getDeclaredMethods())
                        .filter(method -> method.getParameterCount() > 0
                                && method.getParameterTypes()[0].equals(NMSObject.class))
                        .forEach(method -> {
                            List<Map.Entry<String, WrappedMethod>> methods = checkMethods.getOrDefault(
                                    method.getParameterTypes()[0],
                                    new ArrayList<>());

                            methods.add(new AbstractMap.SimpleEntry<>(
                                    check.name,
                                    new WrappedMethod(new WrappedClass(check.getClass()), method)));

                            Bukkit.broadcastMessage("added 2: " + method.getName());
                            checkMethods.put(method.getParameterTypes()[0], methods);
                        });
            }
        }
    }

    public class PlayerInformation {
        public boolean serverGround, clientGround, nearGround, collidedGround;
        public float deltaY, lDeltaY, deltaX, lDeltaX, deltaZ, lDeltaZ, deltaXZ, lDeltaXZ;
        public float pDeltaY, pDeltaX, pDeltaZ, lpDeltaX, lpDeltaY, lpDeltaZ;

        public BlockInformation blockInfo;

        public PlayerInformation() {
            blockInfo = new BlockInformation();
        }

        public class BlockInformation {
            public List<Block> collidingBlocks = new CopyOnWriteArrayList<>(),
                    blocksBelow = new CopyOnWriteArrayList<>();
            public List<Map.Entry<Block, BoundingBox>> allBlocks = new CopyOnWriteArrayList<>();
            public boolean onClimbable, onSlab, onStairs, onHalfBlock, inLiquid, inWeb;

            public boolean checkBlocks(Predicate<Block> block) {
                return collidingBlocks.parallelStream().anyMatch(block);
            }

            public boolean checkBelow(Predicate<Block> block) {
                return blocksBelow.parallelStream().anyMatch(block);
            }

            public void runCollisionCheck() {
                CollisionHandler handler = new CollisionHandler(ObjectData.this);

                List<BoundingBox> boxes = Atlas.getInstance().getBlockBoxManager().getBlockBox()
                        .getCollidingBoxes(getPlayer().getWorld(), box.grow(1.5f,1.5f,1.5f));

                //Running block checking
                boxes.parallelStream().forEach(box -> {
                    Block block = BlockUtils.getBlock(box.getMinimum().toLocation(getPlayer().getWorld()));

                    if(block != null) {
                        handler.onCollide(block, box, false);
                    }
                });

                //Running entity boundingBox check.
                new ArrayList<>(getPlayer().getWorld().getEntities())
                        .parallelStream()
                        .filter(entity -> (entity instanceof Vehicle))
                        .map(entity -> ReflectionsUtil.toBoundingBox(ReflectionsUtil.getBoundingBox(entity)))
                        .filter(box -> box.grow(1,1,1).collides(box))
                        .forEach(box -> handler.onCollide(null, box, true));

                onClimbable = handler.collidesHorizontally && BlockUtils.isClimbableBlock(Objects.requireNonNull(BlockUtils.getBlock(getPlayer().getLocation())));
                serverGround = handler.onGround;
                collidedGround = handler.collidingGround;
                nearGround = handler.nearGround;

                collidingBlocks.clear();
                collidingBlocks.addAll(handler.boxesColliding.stream().map(Map.Entry::getKey).collect(Collectors.toList()));
                blocksBelow.clear();
                blocksBelow.addAll(handler.blocksUnderPlayer);
                allBlocks.clear();
                allBlocks.addAll(handler.allBlocks);

                inLiquid = checkBlocks(BlockUtils::isLiquid);
                inWeb = checkBlocks(block -> block.getType().equals(Material.WEB));
                onHalfBlock = (onSlab = checkBelow(BlockUtils::isSlab))
                        || (onStairs = checkBelow(BlockUtils::isStair))
                        || checkBelow(block -> BlockUtils.isBed(block)
                        || block.getType().equals(Material.CAKE_BLOCK)
                        || block.getType().equals(Material.CAULDRON)
                        || (block.getType().toString().toLowerCase().contains("trap")
                                && block.getType().toString().toLowerCase().contains("door")
                ));
            }
        }
    }
}