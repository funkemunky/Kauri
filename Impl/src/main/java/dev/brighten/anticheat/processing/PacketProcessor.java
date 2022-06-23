package dev.brighten.anticheat.processing;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.listener.functions.PacketListener;
import cc.funkemunky.api.tinyprotocol.packet.in.*;
import cc.funkemunky.api.tinyprotocol.packet.out.*;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.XMaterial;
import cc.funkemunky.api.utils.math.IntVector;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.listeners.api.impl.KeepaliveAcceptedEvent;
import dev.brighten.anticheat.processing.thread.ThreadHandler;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.api.KauriAPI;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.IntStream;

public class PacketProcessor {

    public static boolean simLag = false;
    public static int amount = 500;

    static {

        PacketEvents.getAPI().getEventManager().registerListener(new com.github.retrooper.packetevents.event.PacketListener() {
            @Override
            public void onPacketReceive(PacketReceiveEvent event) {
                ObjectData data = Kauri.INSTANCE.dataManager.getData((Player)event.getPlayer());

                if(data == null || data.checkManager == null) return;

                //Packet exemption check
                if(KauriAPI.INSTANCE.getPacketExemptedPlayers().contains(data.uuid)) return;


                if(data.checkManager.runEvent(event)) event.setCancelled(true);

                if(simLag && event.getPacketType() == PacketType.Play.Client.PLAYER_FLYING) {
                    IntStream.range(0, amount).forEach(i -> {
                        try {
                            SecureRandom.getInstanceStrong().generateSeed(500);
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                    });
                }

                if(event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                    WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);

                    if(data.checkManager.runPacketCancellable(packet, event.getTimestamp())) {
                        event.setCancelled(true);
                    }
                } else if(event.getPacketType() == PacketType.Play.Client.PLUGIN_MESSAGE) {
                    WrapperPlayClientPluginMessage packet = new WrapperPlayClientPluginMessage(event);

                    if(data.checkManager.runPacketCancellable(packet, event.getTimestamp())) {
                        event.setCancelled(true);
                    }
                } else if(event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
                    WrapperPlayClientClickWindow packet = new WrapperPlayClientClickWindow(event);

                    if(data.checkManager.runPacketCancellable(packet, event.getTimestamp())) {
                        event.setCancelled(true);
                    }
                } else if(event.getPacketType() == PacketType.Play.Client.PLAYER_FLYING
                        || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION
                        || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION
                        || event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION) {
                    WrapperPlayClientPlayerFlying packet = new WrapperPlayClientPlayerFlying(event);

                    if(data.checkManager.runPacketCancellable(packet, event.getTimestamp())) {
                        event.setCancelled(true);
                    }
                } else if(event.getPacketType() == PacketType.Play.Client.CREATIVE_INVENTORY_ACTION) {
                    WrapperPlayClientCreativeInventoryAction packet = new WrapperPlayClientCreativeInventoryAction(event);

                    if(data.checkManager.runPacketCancellable(packet, event.getTimestamp())) {
                        event.setCancelled(true);
                    }
                } else if(event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
                    WrapperPlayClientUseItem packet = new WrapperPlayClientUseItem(event);

                    if (data.checkManager.runPacketCancellable(packet, event.getTimestamp())) {
                        event.setCancelled(true);
                    }
                } else if(event.getPacketType() == PacketType.Play.Client.CHAT_MESSAGE) {
                    WrapperPlayClientChatMessage packet = new WrapperPlayClientChatMessage(event);

                    if(data.checkManager.runPacketCancellable(packet, event.getTimestamp())) {
                        event.setCancelled(true);
                    }
                }

                ThreadHandler.INSTANCE.getThread(data).runTask(() -> {
                    if (data.checkManager == null) return;
                    try {
                        processClient(data, event);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onPacketSend(PacketSendEvent event) {
                ObjectData data = Kauri.INSTANCE.dataManager.getData((Player)event.getPlayer());

                if(data == null || data.checkManager == null) return;

                //Packet exemption check
                if(KauriAPI.INSTANCE.getPacketExemptedPlayers().contains(data.uuid)) return;


                if(data.checkManager.runEvent(event)) event.setCancelled(true);

                if(event.getPacketType() == PacketType.Play.Server.ENTITY_MOVEMENT) {
                    WrapperPlayServerEntityMovement packet = new WrapperPlayServerEntityMovement(event);

                    if(data.checkManager.runPacketCancellable(packet, event.getTimestamp())) {
                        event.setCancelled(true);
                    }
                } else if(event.getPacketType() == PacketType.Play.Server.ENTITY_RELATIVE_MOVE) {
                    WrapperPlayServerEntityRelativeMove packet = new WrapperPlayServerEntityRelativeMove(event);

                    if(data.checkManager.runPacketCancellable(packet, event.getTimestamp())) {
                        event.setCancelled(true);
                    }
                } else if(event.getPacketType() == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
                    WrapperPlayServerEntityRelativeMoveAndRotation packet = new WrapperPlayServerEntityRelativeMoveAndRotation(event);

                    if(data.checkManager.runPacketCancellable(packet, event.getTimestamp())) {
                        event.setCancelled(true);
                    }
                } else if(event.getPacketType() == PacketType.Play.Server.ENTITY_HEAD_LOOK) {
                    WrapperPlayServerEntityHeadLook packet = new WrapperPlayServerEntityHeadLook(event);

                    if(data.checkManager.runPacketCancellable(packet, event.getTimestamp())) {
                        event.setCancelled(true);
                    }
                } else if(event.getPacketType() == PacketType.Play.Server.ENTITY_TELEPORT) {
                    WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport(event);

                    if(data.checkManager.runPacketCancellable(packet, event.getTimestamp())) {
                        event.setCancelled(true);
                    }
                } else if(event.getPacketType() == PacketType.Play.Server.PING) {
                    WrapperPlayServerPing packet = new WrapperPlayServerPing(event);

                    if(data.checkManager.runPacketCancellable(packet, event.getTimestamp())) {
                        event.setCancelled(true);
                    }
                } else if(event.getPacketType() == PacketType.Play.Server.ENTITY_VELOCITY) {
                    WrapperPlayServerEntityVelocity packet = new WrapperPlayServerEntityVelocity(event);

                    if(data.checkManager.runPacketCancellable(packet, event.getTimestamp())) {
                        event.setCancelled(true);
                    }
                }
            }
        }, PacketListenerPriority.HIGH);
    }


    public void processClient(ObjectData data, PacketReceiveEvent event) {
        long timestamp = event.getTimestamp();
        PacketTypeCommon type = event.getPacketType();

        if(type == PacketType.Play.Client.PLAYER_ABILITIES) {
             WrapperPlayClientPlayerAbilities packet = new WrapperPlayClientPlayerAbilities(event);

            if(data.playerInfo.canFly)
                data.playerInfo.flying = packet.isFlying();

            data.checkManager.runPacket(packet, timestamp);
            if(data.sniffing) {
                data.sniffedPackets.add(type + ":@:"
                        + packet.getWalkSpeed() + ";" + packet.getFlySpeed() + ";" + packet.isFlightAllowed()
                        + ";" + packet.isInCreativeMode() + "; " + packet.isInGodMode() + ";" + packet.isFlying()
                        + ":@:" + timestamp);
            }
        } else if(type == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);

            Entity entity = Atlas.getInstance().getWorldInfo(data.getPlayer().getWorld())
                    .getEntityOrLock(packet.getEntityId()).orElse(null);

            if (packet.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                data.playerInfo.lastAttack.reset();
                data.playerInfo.lastAttackTimeStamp = timestamp;

                if (entity instanceof LivingEntity) {
                    if (data.target != null && data.target.getEntityId() != packet.getEntityId()) {
                        //Resetting location to prevent false positives.
                        synchronized (data.targetPastLocation.previousLocations) {
                            data.targetPastLocation.previousLocations.clear();
                        }
                        data.playerInfo.lastTargetSwitch.reset();
                        if (entity instanceof Player) {
                            data.targetData = Kauri.INSTANCE.dataManager.getData((Player) entity);
                        } else data.targetData = null;
                    }

                    if (data.target == null && entity instanceof Player)
                        data.targetData = Kauri.INSTANCE.dataManager.getData((Player) entity);
                    data.target = (LivingEntity) entity;
                }
                data.predictionService.hit = true;
                data.playerInfo.usingItem = false;
            }
            if(entity != null)
                data.checkManager.runPacket(packet, timestamp);

            if(data.sniffing) {
                data.sniffedPackets.add(type + ":@:" + packet.getEntityId() + ";" + packet.getAction().name()
                        + ":@:" + timestamp);
            }
        } else if(type == PacketType.Play.Client.PLAYER_FLYING
                || type == PacketType.Play.Client.PLAYER_POSITION
                || type == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION
                || type == PacketType.Play.Client.PLAYER_ROTATION) {
            WrapperPlayClientPlayerFlying packet = new WrapperPlayClientPlayerFlying(event);
            if(data.playerVersion.isOrAbove(ProtocolVersion.v1_17)
                    && packet.hasPositionChanged() && packet.hasRotationChanged()
                    && MiscUtils.isSameLocation(new KLocation(packet.getLocation().getX(),
                            packet.getLocation().getY(), packet.getLocation().getZ()),
                    data.playerInfo.to)) {
                data.excuseNextFlying = true;
            }

            data.entityLocationProcessor.onFlying();

            if(!data.excuseNextFlying) {
                if (timestamp - data.lagInfo.lastFlying <= 15) {
                    data.lagInfo.lastPacketDrop.reset();
                }

                data.playerInfo.checkMovement = MovementUtils.checkMovement(data.playerConnection);
                data.lagInfo.lastFlying = timestamp;
                data.potionProcessor.onFlying(packet);
                data.moveProcessor.process(packet, timestamp);
                data.predictionService.onReceive(packet); //Processing for prediction service.

                data.checkManager.runPacket(packet, timestamp);
                data.playerInfo.lastFlyingTimer.reset();
            }

            if(data.sniffing) {
                data.sniffedPackets.add(type + ":@:"
                        + packet.getLocation().getX() + ";" + packet.getLocation().getY() + ";"
                        + packet.getLocation().getZ() + ";" + packet.getLocation().getYaw() + ";"
                        + packet.getLocation().getPitch() + ";" + packet.isOnGround() +  ":@:" + timestamp);
            }

            data.excuseNextFlying = false;
            data.playerInfo.lsneaking = data.playerInfo.sneaking;

            data.lastFlying = new KLocation(packet.getLocation().getX(), packet.getLocation().getY(),
                    packet.getLocation().getZ());
        } else if(type == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction packet = new WrapperPlayClientEntityAction(event);

            ActionProcessor.process(data, packet);
            data.checkManager.runPacket(packet, timestamp);

            //MiscUtils.testMessage(data.getPlayer().getName() + ": " + packet.getAction());
            if(data.sniffing) {
                data.sniffedPackets.add(type + ":@:" + packet.getAction().name()
                        + ":@:" + timestamp);
            }
        } else if(type == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);

            data.playerInfo.lastBlockDigPacket.reset();
            switch (packet.getAction()) {
                case START_DIGGING: {
                    data.playerInfo.usingItem = false;
                    break;
                }
                case FINISHED_DIGGING: {
                    data.playerInfo.usingItem = false;
                    break;
                }
                case CANCELLED_DIGGING: {
                    data.playerInfo.usingItem = false;
                    break;
                }
                case RELEASE_USE_ITEM: {
                    data.playerInfo.usingItem = false;
                    break;
                }
                case DROP_ITEM_STACK:
                case DROP_ITEM: {
                    data.playerInfo.usingItem = false;
                    data.predictionService.dropItem = true;
                    break;
                }
            }
            if(data.sniffing) {
                data.sniffedPackets.add(type + ":@:" +
                        packet.getAction().name() + ";" + packet.getFace().name() + ";"
                        + "(" + packet.getBlockPosition().getX() + ","
                        + packet.getBlockPosition().getY() + "," + packet.getBlockPosition().getZ() + ")"
                        + ":@:" + timestamp);
            }
            data.checkManager.runPacket(packet, timestamp);
        } else if (type == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            WrapperPlayClientPlayerBlockPlacement packet = new WrapperPlayClientPlayerBlockPlacement(event);

            ItemStack bukkitStack = packet.getItemStack().map(SpigotConversionUtil::toBukkitItemStack)
                    .orElse(null);

            data.playerInfo.lastBlockPlacePacket.reset();

            if (data.getPlayer().getItemInHand() != null) {
                if (data.getPlayer().getItemInHand().getType().name().contains("BUCKET")) {
                    data.playerInfo.lastPlaceLiquid.reset();
                }
                Vector3i pos = packet.getBlockPosition();

                if (bukkitStack != null) {
                    if (pos.getX() == -1 && (pos.getY() == 255 || pos.getY() == -1)
                            && pos.getZ() == -1
                            && (bukkitStack.getType().name().contains("SWORD")
                            || bukkitStack.getType().equals(XMaterial.BOW.parseMaterial()))) {
                        data.playerInfo.usingItem = true;
                        data.playerInfo.lastUseItem.reset();
                    } else if (bukkitStack.getType().isBlock() && bukkitStack.getType().getId() != 0) {
                        data.playerInfo.lastBlockPlace.reset();
                        Location loc = new Location(
                                data.getPlayer().getWorld(), pos.getX(), pos.getY(), pos.getZ());

                        data.playerInfo.shitMap.put(loc, bukkitStack.getType());

                        RunUtils.taskLater(() -> data.runKeepaliveAction(ka -> {
                            data.playerInfo.shitMap.remove(loc);
                        }), 1);
                        // MiscUtils.testMessage(data.getPlayer().getItemInHand().getType().name());
                    }
                }
            }

            data.checkManager.runPacket(packet, timestamp);

            if (data.sniffing) {
                data.sniffedPackets.add(type + ":@:" +
                        (bukkitStack != null ? bukkitStack.getType().name() : "")
                        + ";(" + packet.getBlockPosition().getX() + ","
                        + packet.getBlockPosition().getY() + "," + packet.getBlockPosition().getZ() + ");"
                        + packet.getFace().name() + ";"
                        + packet.getCursorPosition().getX() + "," + packet.getCursorPosition().getY()
                        + "," + packet.getCursorPosition().getZ()
                        + ":@:" + timestamp);
            }
        } else if(type == PacketType.Play.Client.KEEP_ALIVE) {
            WrapperPlayClientKeepAlive packet = new WrapperPlayClientKeepAlive(event);

            long id = packet.getId();

            if(data.keepAlives.containsKey(id)) {
                long last = data.keepAlives.get(id);

                data.lagInfo.lastPing = data.lagInfo.ping;
                data.lagInfo.ping = timestamp - last;

                data.lagInfo.pingAverages.add(data.lagInfo.ping);
                data.lagInfo.averagePing = Math.round(data.lagInfo.pingAverages.getAverage());
            }

            data.checkManager.runPacket(packet, timestamp);
            if(data.sniffing) {
                data.sniffedPackets.add(type + ":@:" + id + ":@:" + timestamp);
            }
        } else if(type == PacketType.Play.Client.ADVANCEMENT_TAB) {
            WrapperPlayClientAdvancementTab packet = new WrapperPlayClientAdvancementTab(event);

            if(packet.getAction() == WrapperPlayClientAdvancementTab.Action.OPENED_TAB) {
                data.playerInfo.inventoryOpen = true;
            }

            data.checkManager.runPacket(packet, timestamp);

            if(data.sniffing) {
                data.sniffedPackets.add(type + ":@:" + packet.getAction().name()
                        + ":@:" + timestamp);
            }
        } else if(type == PacketType.Play.Client.PONG) {
            WrapperPlayClientPong packet = new WrapperPlayClientPong(event);

            long id = packet.getId();

            if(Kauri.INSTANCE.keepaliveProcessor.keepAlives.getIfPresent((int)id) != null) {
                Kauri.INSTANCE.keepaliveProcessor.addResponse(data, packet.getId());

                val optional = Kauri.INSTANCE.keepaliveProcessor.getResponse(data);

                long current = Kauri.INSTANCE.keepaliveProcessor.tick;

                optional.ifPresent(ka -> {
                    data.playerTicks++;
                    data.lagInfo.lastTransPing = data.lagInfo.transPing;
                    data.lagInfo.transPing = (int)(current - ka.start);

                    if(data.instantTransaction.size() > 0) {
                        synchronized (data.instantTransaction) {
                            Deque<Short> toRemove = new LinkedList<>();
                            data.instantTransaction.forEach((key, tuple) -> {
                                if((timestamp - tuple.one.getStamp()) > data.lagInfo.transPing * 52L + 750L) {
                                    tuple.two.accept(tuple.one);
                                    toRemove.add(key);
                                }
                            });
                            Short key = null;
                            while((key = toRemove.poll()) != null) {
                                data.instantTransaction.remove(key);
                            }
                        }
                    }

                    if (Math.abs(data.lagInfo.lastTransPing - data.lagInfo.transPing) > 1) {
                        data.lagInfo.lastPingDrop.reset();
                    }
                    data.clickProcessor.onFlying();

                    ka.getReceived(data.uuid).ifPresent(r -> {
                        r.receivedStamp = data.lagInfo.recieved = timestamp;
                        data.lagInfo.lmillisPing = data.lagInfo.millisPing;
                        data.lagInfo.millisPing = r.receivedStamp - (data.lagInfo.start = ka.startStamp);
                    });

                    KeepaliveAcceptedEvent e = Kauri.INSTANCE.eventHandler
                            .runEvent(new KeepaliveAcceptedEvent(data, ka));

                    for (ObjectData.Action action : data.keepAliveStamps) {
                        if (action.stamp > ka.start) continue;

                        action.action.accept(ka);

                        data.keepAliveStamps.remove(action);
                    }
                });
                data.lagInfo.lastClientTrans = timestamp;
            } else {
                Optional.ofNullable(data.instantTransaction.remove((int)packet.getId()))
                        .ifPresent(t -> t.two.accept(t.one));
            }

            data.checkManager.runPacket(packet, timestamp);
            if(data.sniffing) {
                data.sniffedPackets.add(type + ":@:" + packet.getId()
                        + ":@:" + timestamp);
            }
        } else if(type == PacketType.Play.Client.ANIMATION) {
            WrapperPlayClientAnimation packet = new WrapperPlayClientAnimation(event);

            data.clickProcessor.onArm(packet, timestamp);
            data.checkManager.runPacket(packet, timestamp);
            if(data.sniffing) {
                data.sniffedPackets.add(type + ":@:" + timestamp);
            }
        } else if(type == PacketType.Play.Client.CLIENT_SETTINGS) {
            WrapperPlayClientSettings packet = new WrapperPlayClientSettings(event);

            data.checkManager.runPacket(packet, timestamp);
        } else if(type == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            WrapperPlayClientHeldItemChange packet = new WrapperPlayClientHeldItemChange(event);

            data.playerInfo.usingItem = false;
            data.checkManager.runPacket(packet, timestamp);
            if(data.sniffing) {
                data.sniffedPackets.add(type + ":@:" +
                        packet.getSlot()
                        + ":@:" + timestamp);
            }
        }


        switch (type) {
            case Packet.Client.CUSTOM_PAYLOAD: {
                WrappedInCustomPayload packet = new WrappedInCustomPayload(object, data.getPlayer());

                data.checkManager.runPacket(packet, timestamp);
                break;
            }
            case Packet.Client.WINDOW_CLICK: {
                WrappedInWindowClickPacket packet = new WrappedInWindowClickPacket(object, data.getPlayer());

                data.playerInfo.usingItem = false;
                data.playerInfo.lastWindowClick.reset();
                data.checkManager.runPacket(packet, timestamp);

                if(data.sniffing) {
                    data.sniffedPackets.add(type + ":@:" +
                            packet.getAction().name() + ";" + packet.getButton() + ";" + packet.getId()
                            + ";" + (packet.getItem() != null ? packet.getItem().toString() : "NULL")
                            + ";" + packet.getMode() + ";" + packet.getCounter()
                            + ":@:" + timestamp);
                }
                break;
            }
            case Packet.Client.CLOSE_WINDOW: {
                WrappedInCloseWindowPacket packet
                        = new WrappedInCloseWindowPacket(object, data.getPlayer());

                data.playerInfo.usingItem = false;
                data.playerInfo.inventoryOpen = false;

                data.checkManager.runPacket(packet, timestamp);

                if(data.sniffing) {
                    data.sniffedPackets.add(type + ":@:" + timestamp);
                }
                break;
            }
            case Packet.Client.STEER_VEHICLE: {
                WrappedInSteerVehiclePacket packet = new WrappedInSteerVehiclePacket(object, data.getPlayer());

                if(data.getPlayer().isInsideVehicle() && packet.isUnmount()) {
                    data.playerInfo.vehicleTimer.reset();
                    data.playerInfo.inVehicle = false;
                    data.getPlayer().sendMessage("Dismounted");
                } else if(data.getPlayer().isInsideVehicle()) data.getPlayer().sendMessage("Mounted");

                if(data.sniffing) {
                    data.sniffedPackets.add(type + ":@:" + packet.isUnmount() + ";" + packet.getForward()
                            + ";" + packet.getSideways() + ";" + packet.isJump() + ";" + timestamp);
                }
                break;
            }
            default: {
                if(data.sniffing) {
                    data.sniffedPackets.add(type + ":@:" + timestamp);
                }
                break;
            }
        }
    }

    public void processServer(ObjectData data, Object object, String type, long timestamp) {
        switch (type) {
            case Packet.Server.ABILITIES: {
                WrappedOutAbilitiesPacket packet = new WrappedOutAbilitiesPacket(object, data.getPlayer());

                data.playerInfo.flying = packet.isFlying();
                data.checkManager.runPacket(packet, timestamp);
                break;
            }
            case Packet.Server.RESPAWN: {
                WrappedOutRespawnPacket packet = new WrappedOutRespawnPacket(object, data.getPlayer());

                data.playerInfo.lastRespawn = timestamp;
                data.playerInfo.lastRespawnTimer.reset();
                data.runKeepaliveAction(d -> {
                    data.playerInfo.lastRespawn = timestamp;
                    data.playerInfo.lastRespawnTimer.reset();
                });

                data.checkManager.runPacket(packet, timestamp);
                break;
            }
            case Packet.Server.HELD_ITEM: {
                WrappedOutHeldItemSlot packet = new WrappedOutHeldItemSlot(object, data.getPlayer());

                data.checkManager.runPacket(packet, timestamp);
                break;
            }
            case Packet.Server.ENTITY_EFFECT: {
                WrappedOutEntityEffectPacket packet = new WrappedOutEntityEffectPacket(object, data.getPlayer());

                if(packet.entityId == data.getPlayer().getEntityId()) {
                    data.potionProcessor.onPotionEffect(packet);
                    data.checkManager.runPacket(packet, timestamp);
                }
                break;
            }
            case Packet.Server.ENTITY_METADATA: {
                WrappedOutEntityMetadata packet = new WrappedOutEntityMetadata(object, data.getPlayer());

                data.checkManager.runPacket(packet, timestamp);
                break;
            }
            case Packet.Server.CLOSE_WINDOW: {
                WrappedOutCloseWindowPacket packet = new WrappedOutCloseWindowPacket(object, data.getPlayer());
                data.playerInfo.inventoryOpen = false;
                data.playerInfo.inventoryId = 0;

                data.checkManager.runPacket(packet, timestamp);
                break;
            }
            case Packet.Server.OPEN_WINDOW: {
                WrappedOutOpenWindow packet = new WrappedOutOpenWindow(object, data.getPlayer());

                data.checkManager.runPacket(packet, timestamp);
                data.playerInfo.inventoryOpen = true;
                data.playerInfo.inventoryId = packet.getId();
                break;
            }
            case Packet.Server.EXPLOSION: {
                WrappedOutExplosionPacket packet = new WrappedOutExplosionPacket(object, data.getPlayer());

                Vector vector = new Vector(packet.getMotionX(), packet.getMotionY(), packet.getMotionZ());
                data.playerInfo.velocities.add(vector);
                data.playerInfo.doingVelocity = true;
                data.runInstantAction(keepalive -> {
                    if(keepalive.isEnd() && data.playerInfo.velocities.contains(vector)) {
                        if(data.playerInfo.doingVelocity) {
                            data.playerInfo.lastVelocity.reset();

                            data.playerInfo.doingVelocity = false;
                            data.playerInfo.cva = data.playerInfo.cvb = data.playerInfo.cvc = true;
                            data.playerInfo.lastVelocityTimestamp = System.currentTimeMillis();
                            data.predictionService.velocity = true;
                            data.playerInfo.velocityX = data.playerInfo.calcVelocityX = (float) packet.getX();
                            data.playerInfo.velocityY = data.playerInfo.calcVelocityY = (float) packet.getY();
                            data.playerInfo.velocityZ = data.playerInfo.calcVelocityZ = (float) packet.getZ();
                            data.playerInfo.velocityXZ = Math.hypot(data.playerInfo.velocityX,
                                    data.playerInfo.velocityZ);
                        }
                        data.playerInfo.velocities.remove(vector);
                    }
                });
                break;
            }
            case Packet.Server.ENTITY_VELOCITY: {
                WrappedOutVelocityPacket packet = new WrappedOutVelocityPacket(object, data.getPlayer());

                if (packet.getId() == data.getPlayer().getEntityId()) {
                    //Setting velocity action.
                    Vector vector = new Vector(packet.getX(), packet.getY(), packet.getZ());
                    data.playerInfo.velocities.add(vector);
                    data.playerInfo.doingVelocity = true;
                    data.runInstantAction(keepalive -> {
                        if(keepalive.isEnd() && data.playerInfo.velocities.contains(vector)) {
                            if(data.playerInfo.doingVelocity) {
                                data.playerInfo.lastVelocity.reset();

                                data.playerInfo.doingVelocity = false;
                                data.playerInfo.cva = data.playerInfo.cvb = data.playerInfo.cvc = true;
                                data.playerInfo.lastVelocityTimestamp = System.currentTimeMillis();
                                data.predictionService.velocity = true;
                                data.playerInfo.velocityX = data.playerInfo.calcVelocityX = (float) packet.getX();
                                data.playerInfo.velocityY = data.playerInfo.calcVelocityY = (float) packet.getY();
                                data.playerInfo.velocityZ = data.playerInfo.calcVelocityZ = (float) packet.getZ();
                                data.playerInfo.velocityXZ = Math.hypot(data.playerInfo.velocityX,
                                        data.playerInfo.velocityZ);
                            }
                            data.playerInfo.velocities.remove(vector);
                        }
                    });
                }

                if(data.sniffing) {
                    data.sniffedPackets.add(type + ":@:" + packet.getId() + ";"
                            + packet.getX() + ";" + packet.getY() + ";" + packet.getZ()
                            + ":@:" + timestamp);
                }
                data.checkManager.runPacket(packet, timestamp);
                break;
            }
            case Packet.Server.ENTITY_HEAD_ROTATION: {
                WrappedOutEntityHeadRotation packet = new WrappedOutEntityHeadRotation(object, data.getPlayer());

                data.playerInfo.headYaw = packet.getPlayer().getLocation().getYaw();
                data.playerInfo.headPitch = packet.getPlayer().getLocation().getPitch();
                data.checkManager.runPacket(packet, timestamp);
                break;
            }
            case Packet.Server.ATTACH: {
                WrappedOutAttachEntity packet = new WrappedOutAttachEntity(object, data.getPlayer());

                if(packet.getHoldingEntityId() != -1) {
                    data.playerInfo.inVehicle = true;
                    data.playerInfo.vehicleTimer.reset();
                } else {
                    data.playerInfo.inVehicle = false;
                    data.playerInfo.vehicleTimer.reset();
                }

                if(data.sniffing) {
                    data.sniffedPackets.add(type + ":@:" + packet.getHoldingEntityId()
                            + ";" + packet.getAttachedEntityId());
                }
                break;
            }
            case Packet.Server.REL_LOOK:
            case Packet.Server.REL_POSITION:
            case Packet.Server.REL_POSITION_LOOK:
            case Packet.Server.ENTITY:
            case Packet.Server.LEGACY_REL_POSITION_LOOK:
            case Packet.Server.LEGACY_REL_POSITION:
            case Packet.Server.LEGACY_REL_LOOK: {
                WrappedOutRelativePosition packet = new WrappedOutRelativePosition(object, data.getPlayer());

                data.entityLocationProcessor.onRelPosition(packet);
                data.checkManager.runPacket(packet, timestamp);
                break;
            }
            case Packet.Server.ENTITY_TELEPORT: {
                WrappedOutEntityTeleportPacket packet = new WrappedOutEntityTeleportPacket(object, data.getPlayer());

                if(data.sniffing) {
                    data.sniffedPackets.add(type + ":@:" + packet.entityId + ";" + packet.x + ";" + packet.y + ";"
                            + packet.z + ";" + packet.yaw + ";" + packet.pitch + ":" + timestamp);
                }

                data.entityLocationProcessor.onTeleportSent(packet);

                data.checkManager.runPacket(packet, timestamp);
                break;
            }
            case Packet.Server.CUSTOM_PAYLOAD: {
                WrappedOutCustomPayload packet = new WrappedOutCustomPayload(object, data.getPlayer());

                data.checkManager.runPacket(packet, timestamp);
                break;
            }
            case Packet.Server.KEEP_ALIVE: {
                WrappedOutKeepAlivePacket packet = new WrappedOutKeepAlivePacket(object, data.getPlayer());

                data.lagInfo.lastKeepAlive = timestamp;
                data.keepAlives.put(packet.getTime(), System.currentTimeMillis());
                data.checkManager.runPacket(packet, timestamp);
                break;
            }
            case Packet.Server.TRANSACTION: {
                WrappedOutTransaction packet = new WrappedOutTransaction(object, data.getPlayer());

                if(data.sniffing) {
                    data.sniffedPackets.add(type + ":@:" + packet.getId() + ";"
                            + packet.getAction() + ":@:" + timestamp);
                }
                data.checkManager.runPacket(packet, timestamp);
                break;
            }
            case Packet.Server.BLOCK_CHANGE: {
                WrappedOutBlockChange blockChange = new WrappedOutBlockChange(object, data.getPlayer());

                Location loc = new Location(data.getPlayer().getWorld(), blockChange.getPosition().getX(),
                        blockChange.getPosition().getY(), blockChange.getPosition().getZ());

                if(loc.distanceSquared(data.getPlayer().getLocation()) < 25) {
                    data.blockUpdates++;
                    data.playerInfo.lastGhostCollision.reset();

                    data.runKeepaliveAction(ka -> {
                        data.blockUpdates--;
                    });
                }
                break;
            }
            case "PacketPlayOutMultiBlockChange": {
                data.blockUpdates++;
                data.playerInfo.lastGhostCollision.reset();

                data.runKeepaliveAction(ka -> {
                    data.blockUpdates--;
                });
                break;
            }
            case Packet.Server.POSITION: {
                WrappedOutPositionPacket packet = new WrappedOutPositionPacket(object, data.getPlayer());

                KLocation loc = new KLocation(packet.getX(), packet.getY(), packet.getZ(),
                        packet.getYaw(), packet.getPitch());

                int i = 0;
                if(packet.getFlags().contains(WrappedOutPositionPacket.EnumPlayerTeleportFlags.X)) {
                    loc.x+= data.playerInfo.to.x;
                }
                if(packet.getFlags().contains(WrappedOutPositionPacket.EnumPlayerTeleportFlags.Y)) {
                    loc.y+= data.playerInfo.to.y;
                }
                if(packet.getFlags().contains(WrappedOutPositionPacket.EnumPlayerTeleportFlags.Z)) {
                    loc.z+= data.playerInfo.to.z;
                }
                if(packet.getFlags().contains(WrappedOutPositionPacket.EnumPlayerTeleportFlags.X_ROT)) {
                    loc.pitch+= data.playerInfo.to.pitch;
                }
                if(packet.getFlags().contains(WrappedOutPositionPacket.EnumPlayerTeleportFlags.Y_ROT)) {
                    loc.yaw+= data.playerInfo.to.yaw;
                }

                data.teleportsToConfirm++;

                data.runKeepaliveAction(ka -> data.teleportsToConfirm--, 2);
                synchronized (data.playerInfo.posLocs) {
                    data.playerInfo.posLocs.add(loc);
                }

                data.playerInfo.phaseLoc = loc.clone();

                data.getPlayer().setSprinting(false);

                if(data.sniffing) {
                    data.sniffedPackets.add(type + ":@:" + packet.getX() + ";"
                            + packet.getY() + ";" + packet.getZ() + ":@:" + timestamp);
                }
                data.checkManager.runPacket(packet, timestamp);
                break;
            }
        }
    }

    private static String getType(String type) {
        switch (type) {
            case Packet.Client.FLYING:
            case Packet.Client.POSITION:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.LOOK: {
                return "PacketPlayInFlying";
            }
            default: {
                return type;
            }
        }
    }
}