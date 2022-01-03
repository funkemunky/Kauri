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
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.listeners.api.impl.KeepaliveAcceptedEvent;
import dev.brighten.anticheat.utils.EntityLocation;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.anticheat.utils.ThreadHandler;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

public class PacketProcessor {

    public static boolean simLag = false;

    public static final Set<String> incomingPackets = new HashSet<>(), outgoingPackets = new HashSet<>();

    static {
        new WrappedClass(Packet.Server.class).getFields().forEach(field -> {
            String packet = field.get(null);
            outgoingPackets.add(packet);
        });

        new WrappedClass(Packet.Client.class).getFields().forEach(field -> {
            String packet = field.get(null);
            incomingPackets.add(packet);
        });
    }

    public final PacketListener listener = Atlas.getInstance().getPacketProcessor()
            .process(Kauri.INSTANCE, EventPriority.NORMAL, info -> {
                if(info.isCancelled()) return;
                ObjectData data = Kauri.INSTANCE.dataManager.getData(info.getPlayer());

                if(data == null || data.checkManager == null) return;

               try {
                   if(outgoingPackets.contains(info.getType())) {
                       processServer(data, info.getPacket(), info.getType(), info.getTimestamp());
                   } else if(incomingPackets.contains(info.getType())) {
                       processClient(data, info.getPacket(), info.getType(), info.getTimestamp());

                       if(info.getType().equals(Packet.Client.FLYING)) {
                           IntStream.range(0, 100).forEach(i -> {
                               try {
                                   SecureRandom.getInstanceStrong().generateSeed(80);
                               } catch (NoSuchAlgorithmException e) {
                                   e.printStackTrace();
                               }
                           });
                       }
                   }

                   if(data.checkManager.runEvent(info)) info.setCancelled(true);
               } catch(Exception e) {
                   e.printStackTrace();
               }
            });

    public final PacketListener cancelListener = Atlas.getInstance().getPacketProcessor()
            .process(Kauri.INSTANCE, EventPriority.LOW, info -> {
                ObjectData data = Kauri.INSTANCE.dataManager.getData(info.getPlayer());

                if(data == null || data.checkManager == null) return;

                switch(info.getType()) {
                    case Packet.Client.USE_ENTITY: {
                        val packet = new WrappedInUseEntityPacket(info.getPacket(), info.getPlayer());

                        if(data.checkManager.runPacketCancellable(packet, info.getTimestamp())) {
                            info.setCancelled(true);
                        }
                        break;
                    }
                    case Packet.Client.CUSTOM_PAYLOAD: {
                        val packet = new WrappedInCustomPayload(info.getPacket(), info.getPlayer());

                        if(data.checkManager.runPacketCancellable(packet, info.getTimestamp())) {
                            info.setCancelled(true);
                        }
                        break;
                    }
                    case Packet.Client.WINDOW_CLICK: {
                        val packet = new WrappedInWindowClickPacket(info.getPacket(), info.getPlayer());

                        if(data.checkManager.runPacketCancellable(packet, info.getTimestamp())) {
                            info.setCancelled(true);
                        }
                        break;
                    }
                    case Packet.Client.FLYING:
                    case Packet.Client.POSITION:
                    case Packet.Client.POSITION_LOOK:
                    case Packet.Client.LOOK: {
                        val packet = new WrappedInFlyingPacket(info.getPacket(), info.getPlayer());

                        if(data.checkManager.runPacketCancellable(packet, info.getTimestamp())) {
                            info.setCancelled(true);
                        }
                        break;
                    }
                    case Packet.Client.CREATIVE_SLOT: {
                        val packet = new WrappedInSetCreativeSlotPacket(info.getPacket(), info.getPlayer());

                        if(data.checkManager.runPacketCancellable(packet, info.getTimestamp())) {
                            info.setCancelled(true);
                        }
                        break;
                    }
                    case Packet.Server.REL_LOOK:
                    case Packet.Server.REL_POSITION:
                    case Packet.Server.REL_POSITION_LOOK:
                    case Packet.Server.ENTITY:
                    case Packet.Server.LEGACY_REL_LOOK:
                    case Packet.Server.LEGACY_REL_POSITION:
                    case Packet.Server.LEGACY_REL_POSITION_LOOK: {
                        val packet = new WrappedOutRelativePosition(info.getPacket(), info.getPlayer());

                        if(data.checkManager.runPacketCancellable(packet, info.getTimestamp())) {
                            info.setCancelled(true);
                        }
                        break;
                    }
                    case Packet.Server.ENTITY_TELEPORT: {
                        WrappedOutEntityTeleportPacket packet
                                = new WrappedOutEntityTeleportPacket(info.getPacket(), data.getPlayer());

                        if(data.checkManager.runPacketCancellable(packet, info.getTimestamp())) {
                            info.setCancelled(true);
                        }
                        break;
                    }
                    case Packet.Server.TRANSACTION: {
                        val packet = new WrappedOutTransaction(info.getPacket(), info.getPlayer());

                        if(data.checkManager.runPacketCancellable(packet, info.getTimestamp())) {
                            info.setCancelled(true);
                        }
                        break;
                    }
                    case Packet.Server.ENTITY_VELOCITY: {
                        val packet = new WrappedOutVelocityPacket(info.getPacket(), info.getPlayer());

                        if(data.checkManager.runPacketCancellable(packet, info.getTimestamp())) {
                            info.setCancelled(true);
                        }
                        break;
                    }
                    case Packet.Client.CHAT: {
                        val packet = new WrappedInChatPacket(info.getPacket(), info.getPlayer());

                        if(data.checkManager.runPacketCancellable(packet, info.getTimestamp())) {
                            info.setCancelled(true);
                        }
                        break;
                    }
                }
            }, Packet.Client.USE_ENTITY, Packet.Client.FLYING, Packet.Client.POSITION, Packet.Client.CREATIVE_SLOT,
                    Packet.Client.POSITION_LOOK, Packet.Client.LOOK, Packet.Server.REL_LOOK, Packet.Client.WINDOW_CLICK,
                    Packet.Client.CUSTOM_PAYLOAD, Packet.Client.CHAT, Packet.Client.ARM_ANIMATION,
                    Packet.Client.WINDOW_CLICK,
                    Packet.Server.REL_POSITION, Packet.Server.REL_POSITION_LOOK, Packet.Server.LEGACY_REL_LOOK,
                    Packet.Server.LEGACY_REL_POSITION, Packet.Server.LEGACY_REL_POSITION_LOOK, Packet.Server.ENTITY,
                    Packet.Server.TRANSACTION, Packet.Server.ENTITY_VELOCITY, Packet.Server.ENTITY_TELEPORT);


    public void processClient(ObjectData data, Object object, String type,
                                           long timestamp) {
        switch (type) {
            case Packet.Client.ABILITIES: {
                WrappedInAbilitiesPacket packet = new WrappedInAbilitiesPacket(object, data.getPlayer());

                if(data.playerInfo.canFly)
                data.playerInfo.flying = packet.isFlying();

                data.checkManager.runPacket(packet, timestamp);
                if(data.sniffing) {
                    data.sniffedPackets.add(type + ":@:"
                            + packet.getWalkSpeed() + ";" + packet.getFlySpeed() + ";" + packet.isAllowedFlight()
                            + ";" + packet.isCreativeMode() + "; " + packet.isInvulnerable() + ";" + packet.isFlying()
                            + ":@:" + timestamp);
                }
                break;
            }
            case Packet.Client.USE_ENTITY: {
                WrappedInUseEntityPacket packet = new WrappedInUseEntityPacket(object, data.getPlayer());

                if (packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) {
                    data.playerInfo.lastAttack.reset();
                    data.playerInfo.lastAttackTimeStamp = timestamp;

                    if (packet.getEntity() instanceof LivingEntity) {
                        if (data.target != null && data.target.getEntityId() != packet.getId()) {
                            //Resetting location to prevent false positives.
                            synchronized (data.targetPastLocation.previousLocations) {
                                data.targetPastLocation.previousLocations.clear();
                            }
                            data.playerInfo.lastTargetSwitch.reset();
                            synchronized (data.entityLocPastLocation.previousLocations) {
                                data.entityLocPastLocation.previousLocations.clear();
                            }
                            if (packet.getEntity() instanceof Player) {
                                data.targetData = Kauri.INSTANCE.dataManager.getData((Player) packet.getEntity());
                            } else data.targetData = null;
                        }

                        if (data.target == null && packet.getEntity() instanceof Player)
                            data.targetData = Kauri.INSTANCE.dataManager.getData((Player) packet.getEntity());
                        data.target = (LivingEntity) packet.getEntity();
                    }
                    data.predictionService.hit = true;
                    data.playerInfo.usingItem = false;
                }
                if(packet.getEntity() != null)
                data.checkManager.runPacket(packet, timestamp);

                if(data.sniffing) {
                    data.sniffedPackets.add(type + ":@:" + packet.getId() + ";" + packet.getAction().name()
                            + ":@:" + timestamp);
                }
                break;
            }
            case Packet.Client.FLYING:
            case Packet.Client.POSITION:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.LOOK: {
                WrappedInFlyingPacket packet = new WrappedInFlyingPacket(object, data.getPlayer());

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

                if(data.sniffing) {
                    data.sniffedPackets.add(type + ":@:"
                            + packet.getX() + ";" + packet.getY() + ";" + packet.getZ() + ";"
                            + packet.getYaw() + ";" + packet.getPitch() + ";" + packet.isGround()
                            +  ":@:" + timestamp);
                }

                data.playerInfo.lsneaking = data.playerInfo.sneaking;
                break;
            }
            case Packet.Client.ENTITY_ACTION: {
                WrappedInEntityActionPacket packet = new WrappedInEntityActionPacket(object, data.getPlayer());

                ActionProcessor.process(data, packet);
                data.checkManager.runPacket(packet, timestamp);

                //MiscUtils.testMessage(data.getPlayer().getName() + ": " + packet.getAction());
                if(data.sniffing) {
                    data.sniffedPackets.add(type + ":@:" + packet.getAction().name()
                            + ":@:" + timestamp);
                }
                break;
            }
            case Packet.Client.BLOCK_DIG: {
                WrappedInBlockDigPacket packet = new WrappedInBlockDigPacket(object, data.getPlayer());

                data.playerInfo.lastBlockDigPacket.reset();
                switch (packet.getAction()) {
                    case START_DESTROY_BLOCK: {
                        data.playerInfo.usingItem = false;
                        break;
                    }
                    case STOP_DESTROY_BLOCK: {
                        data.playerInfo.usingItem = false;
                        break;
                    }
                    case ABORT_DESTROY_BLOCK: {
                        data.playerInfo.usingItem = false;
                        break;
                    }
                    case RELEASE_USE_ITEM: {
                        data.playerInfo.usingItem = false;
                        break;
                    }
                    case DROP_ALL_ITEMS:
                    case DROP_ITEM: {
                        data.playerInfo.usingItem = false;
                        data.predictionService.dropItem = true;
                        break;
                    }
                }
                if(data.sniffing) {
                    data.sniffedPackets.add(type + ":@:" +
                            packet.getAction().name() + ";" + packet.getDirection().name() + ";"
                            + "(" + packet.getPosition().getX() + ","
                            + packet.getPosition().getY() + "," + packet.getPosition().getZ() + ")"
                            + ":@:" + timestamp);
                }
                data.checkManager.runPacket(packet, timestamp);
                break;
            }
            case Packet.Client.BLOCK_PLACE: {
                WrappedInBlockPlacePacket packet = new WrappedInBlockPlacePacket(object, data.getPlayer());

                data.playerInfo.lastBlockPlacePacket.reset();
                if (data.getPlayer().getItemInHand() != null) {
                    if(data.getPlayer().getItemInHand().getType().name().contains("BUCKET")) {
                        data.playerInfo.lastPlaceLiquid.reset();
                    }
                    IntVector pos = packet.getBlockPosition();
                    val stack = packet.getItemStack();

                    if(pos.getX() == -1 && (pos.getY() == 255 || pos.getY() == -1)
                            && pos.getZ() == -1 && stack != null
                            && (stack.getType().name().contains("SWORD")
                            || stack.getType().equals(XMaterial.BOW.parseMaterial()))) {
                        data.playerInfo.usingItem = true;
                        data.playerInfo.lastUseItem.reset();
                    } else if(stack != null) {
                        if(stack.getType().isBlock() && stack.getType().getId() != 0) {
                            data.playerInfo.lastBlockPlace.reset();
                            Location loc = new Location(
                                    data.getPlayer().getWorld(), pos.getX(), pos.getY(), pos.getZ());

                            data.playerInfo.shitMap.put(loc, stack.getType());

                            RunUtils.taskLater(() -> data.runKeepaliveAction(ka -> {
                                data.playerInfo.shitMap.remove(loc);
                            }), 1);
                           // MiscUtils.testMessage(data.getPlayer().getItemInHand().getType().name());
                        }
                    }
                }
                if(data.sniffing) {
                    data.sniffedPackets.add(type + ":@:" +
                            (packet.getItemStack() != null ? packet.getItemStack().toString() : "NULL")
                            + ";(" + packet.getBlockPosition().getX() + ","
                            + packet.getBlockPosition().getY() + "," + packet.getBlockPosition().getZ() + ");"
                            + packet.getFace().name() + ";"
                            + packet.getVecX() + "," + packet.getVecY() + "," + packet.getVecZ()
                            + ":@:" + timestamp);
                }

                data.checkManager.runPacket(packet, timestamp);
                break;
            }
            case Packet.Client.KEEP_ALIVE: {
                WrappedInKeepAlivePacket packet = new WrappedInKeepAlivePacket(object, data.getPlayer());

                long time = packet.getTime();

                if(data.keepAlives.containsKey(time)) {
                    long last = data.keepAlives.get(time);

                    data.lagInfo.lastPing = data.lagInfo.ping;
                    data.lagInfo.ping = timestamp - last;

                    data.lagInfo.pingAverages.add(data.lagInfo.ping);
                    data.lagInfo.averagePing = Math.round(data.lagInfo.pingAverages.getAverage());
                }

                data.checkManager.runPacket(packet, timestamp);
                if(data.sniffing) {
                    data.sniffedPackets.add(type + ":@:" + time + ":@:" + timestamp);
                }
                break;
            }
            case Packet.Client.CLIENT_COMMAND: {
                WrappedInClientCommandPacket packet = new WrappedInClientCommandPacket(object, data.getPlayer());

                if(packet.getCommand()
                        .equals(WrappedInClientCommandPacket.EnumClientCommand.OPEN_INVENTORY_ACHIEVEMENT)) {
                    data.playerInfo.inventoryOpen = true;
                }

                data.checkManager.runPacket(packet, timestamp);

                if(data.sniffing) {
                    data.sniffedPackets.add(type + ":@:" + packet.getCommand().name()
                            + ":@:" + timestamp);
                }
                break;
            }
            case Packet.Client.TRANSACTION: {
                WrappedInTransactionPacket packet = new WrappedInTransactionPacket(object, data.getPlayer());

                if(packet.getId() == 0) {
                    RunUtils.task(() -> {
                        if(Kauri.INSTANCE.keepaliveProcessor.keepAlives.containsKey(packet.getAction())) {
                            Kauri.INSTANCE.keepaliveProcessor.addResponse(data, packet.getAction());

                            val optional = Kauri.INSTANCE.keepaliveProcessor.getResponse(data);

                            int current = Kauri.INSTANCE.keepaliveProcessor.tick;

                            optional.ifPresent(ka -> {
                                data.playerTicks++;
                                data.lagInfo.lastTransPing = data.lagInfo.transPing;
                                data.lagInfo.transPing = (current - ka.start);

                                if (Math.abs(data.lagInfo.lastTransPing - data.lagInfo.transPing) > 1) {
                                    data.lagInfo.lastPingDrop.reset();
                                }
                                data.clickProcessor.onFlying(packet);

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
                            Optional.ofNullable(data.instantTransaction.remove(packet.getAction()))
                                    .ifPresent(t -> t.two.accept(t.one));
                        }
                    });
                }

                data.checkManager.runPacket(packet, timestamp);
                if(data.sniffing) {
                    data.sniffedPackets.add(type + ":@:" + packet.getAction() + ";" + packet.getId()
                            + ":@:" + timestamp);
                }
                break;
            }
            case Packet.Client.ARM_ANIMATION: {
                WrappedInArmAnimationPacket packet = new WrappedInArmAnimationPacket(object, data.getPlayer());

                data.clickProcessor.onArm(packet, timestamp);
                data.checkManager.runPacket(packet, timestamp);
                if(data.sniffing) {
                    data.sniffedPackets.add(type + ":@:" + timestamp);
                }
                break;
            }
            case Packet.Client.SETTINGS: {
                WrappedInSettingsPacket packet = new WrappedInSettingsPacket(object, data.getPlayer());

                data.checkManager.runPacket(packet, timestamp);
                break;
            }
            case Packet.Client.HELD_ITEM_SLOT: {
                WrappedInHeldItemSlotPacket packet = new WrappedInHeldItemSlotPacket(object, data.getPlayer());

                data.playerInfo.usingItem = false;
                data.checkManager.runPacket(packet, timestamp);
                if(data.sniffing) {
                    data.sniffedPackets.add(type + ":@:" +
                           packet.getSlot()
                            + ":@:" + timestamp);
                }
                break;
            }
            case Packet.Client.TAB_COMPLETE: {
                WrappedInTabComplete packet = new WrappedInTabComplete(object, data.getPlayer());

                if(packet.getMessage().startsWith("/yourmom")) {
                    WrappedOutTabComplete tabComplete;
                    if(ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_13)) {
                        tabComplete = new WrappedOutTabComplete(0, packet.getMessage(), "gay", "homo");
                    } else {
                        tabComplete = new WrappedOutTabComplete("gay", "homo");
                    }

                    TinyProtocolHandler.sendPacket(data.getPlayer(), tabComplete.getObject());
                }
                break;
            }
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
                data.runKeepaliveAction(end -> {
                    if(data.playerInfo.velocities.contains(vector)) {
                        if(data.playerInfo.doingVelocity) {
                            data.playerInfo.lastVelocity.reset();

                            data.playerInfo.doingVelocity = false;
                            data.playerInfo.lastVelocityTimestamp = System.currentTimeMillis();
                            data.playerInfo.cva = data.playerInfo.cvb = data.playerInfo.cvc = true;
                            data.predictionService.velocity = true;
                            data.playerInfo.velocityX = data.playerInfo.calcVelocityX = (float) packet.getX();
                            data.playerInfo.velocityY = data.playerInfo.calcVelocityY = (float) packet.getY();
                            data.playerInfo.velocityZ = data.playerInfo.calcVelocityZ = (float) packet.getZ();
                            data.playerInfo.velocityXZ =
                                    Math.hypot(data.playerInfo.velocityX, data.playerInfo.velocityZ);
                        }
                        data.playerInfo.velocities.remove(vector);
                    }
                }, 1);
                break;
            }
            case Packet.Server.ENTITY_VELOCITY: {
                WrappedOutVelocityPacket packet = new WrappedOutVelocityPacket(object, data.getPlayer());

                if (packet.getId() == data.getPlayer().getEntityId()) {
                    //Setting velocity action.
                    Vector vector = new Vector(packet.getX(), packet.getY(), packet.getZ());
                    data.playerInfo.velocities.add(vector);
                    data.playerInfo.doingVelocity = true;
                    data.runKeepaliveAction(keepalive -> {
                        if(data.playerInfo.velocities.contains(vector)) {
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
                    }, 1);
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
            case Packet.Server.REL_LOOK:
            case Packet.Server.REL_POSITION:
            case Packet.Server.REL_POSITION_LOOK:
            case Packet.Server.ENTITY:
            case Packet.Server.LEGACY_REL_POSITION_LOOK:
            case Packet.Server.LEGACY_REL_POSITION:
            case Packet.Server.LEGACY_REL_LOOK: {
                WrappedOutRelativePosition packet = new WrappedOutRelativePosition(object, data.getPlayer());

                Atlas.getInstance().getWorldInfo(data.getPlayer().getWorld())
                        .getEntity(packet.getId()).ifPresent(entity -> {
                            EntityLocation eloc = data.getEntityLocation(entity);

                            //We don't need to do version checking here. Atlas handles this for us.
                            if(ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_9)) {
                                eloc.newX += (byte)packet.getX() / 32D;
                                eloc.newY += (byte)packet.getY() / 32D;
                                eloc.newZ += (byte)packet.getZ() / 32D;
                                eloc.newYaw += (float)(byte)packet.getYaw() / 256.0F * 360.0F;
                                eloc.newPitch += (float)(byte)packet.getPitch() / 256.0F * 360.0F;
                            } else if(ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_14)) {
                                eloc.newX += (int)packet.getX() / 4096D;
                                eloc.newY += (int)packet.getY() / 4096D;
                                eloc.newZ += (int)packet.getZ() / 4096D;
                                eloc.newYaw += (float)(byte)packet.getYaw() / 256.0F * 360.0F;
                                eloc.newPitch += (float)(byte)packet.getPitch() / 256.0F * 360.0F;
                            } else {
                                eloc.newX += (short)packet.getX() / 4096D;
                                eloc.newY += (short)packet.getY() / 4096D;
                                eloc.newZ += (short)packet.getZ() / 4096D;
                                eloc.newYaw += (float)(byte)packet.getYaw() / 256.0F * 360.0F;
                                eloc.newPitch += (float)(byte)packet.getPitch() / 256.0F * 360.0F;
                            }

                            eloc.interpolateLocations();

                            if(data.target != null && data.target.getEntityId() == packet.getId()) {
                                for (int i = 0; i < eloc.interpolatedLocations.size(); i++) {
                                    KLocation loc = eloc.interpolatedLocations.get(i);
                                    loc.timeStamp+= i;

                                    data.entityLocPastLocation.addLocation(loc);
                                }
                                data.playerInfo.lastTargetUpdate.reset();
                            }

                            if(data.sniffing) {
                                data.sniffedPackets.add(type + ":@:" + packet.getId() + ";" + packet.getX() + ";"
                                        + packet.getY() + ";" + packet.getZ() + ";" + packet.getYaw() + ";"
                                        + packet.getPitch() + ":" + timestamp);
                            }
                        });

                data.checkManager.runPacket(packet, timestamp);
                break;
            }
            case Packet.Server.ENTITY_TELEPORT: {
                WrappedOutEntityTeleportPacket packet = new WrappedOutEntityTeleportPacket(object, data.getPlayer());

                Atlas.getInstance().getWorldInfo(packet.getPlayer().getWorld()).getEntity(packet.entityId)
                        .ifPresent(entity -> {
                            EntityLocation eloc = data.getEntityLocation(entity);

                            //We don't need to do version checking here. Atlas handles this for us.
                            if(data.playerVersion.isOrAbove(ProtocolVersion.V1_9)) {
                                if (!(Math.abs(eloc.x - packet.x) >= 0.03125D)
                                        && !(Math.abs(eloc.y - packet.y) >= 0.015625D)
                                        && !(Math.abs(eloc.z - packet.z) >= 0.03125D)) {
                                    eloc.increment = 0;
                                    //We don't need to do version checking here. Atlas handles this for us.
                                    eloc.newX = eloc.x = packet.x;
                                    eloc.newY = eloc.y = packet.y;
                                    eloc.newZ = eloc.z = packet.z;
                                    eloc.newYaw = eloc.yaw = packet.yaw;
                                    eloc.newPitch = eloc.pitch = packet.pitch;
                                    eloc.interpolatedLocations.clear();
                                    eloc.interpolatedLocations
                                            .add(new KLocation(eloc.newX, eloc.newY, eloc.newZ,
                                                    eloc.newYaw, eloc.newPitch, Kauri.INSTANCE.keepaliveProcessor.tick));
                                } else {
                                    eloc.newX = packet.x;
                                    eloc.newY = packet.y;
                                    eloc.newZ = packet.z;
                                    eloc.newYaw = packet.yaw;
                                    eloc.newPitch = packet.pitch ;

                                    eloc.increment = 3;
                                    eloc.interpolateLocations();
                                }
                            } else {
                                //We don't need to do version checking here. Atlas handles this for us.
                                eloc.newX = packet.x;
                                eloc.newY = packet.y;
                                eloc.newZ = packet.z;
                                eloc.newYaw = packet.yaw;
                                eloc.newPitch = packet.pitch;

                                eloc.increment = 3;
                                eloc.interpolateLocations();
                            }

                            if(data.target != null && data.target.getEntityId() == packet.entityId) {
                                eloc.sentTeleport = true;
                                if(eloc.interpolatedLocations.size() > 1) {
                                    for (int i = 0; i < eloc.interpolatedLocations.size(); i++) {
                                        KLocation loc = eloc.interpolatedLocations.get(i);
                                        loc.timeStamp+= i;

                                        data.entityLocPastLocation.addLocation(loc);
                                    }
                                } else if(eloc.interpolatedLocations.size() > 0) {
                                    data.entityLocPastLocation.addLocation(eloc.interpolatedLocations.get(0));
                                }
                                data.playerInfo.lastTargetUpdate.reset();
                            }

                            //Clearing any old interpolated locations
                            eloc.interpolatedLocations.clear();
                        });

                if(data.sniffing) {
                    data.sniffedPackets.add(type + ":@:" + packet.entityId + ";" + packet.x + ";" + packet.y + ";"
                            + packet.z + ";" + packet.yaw + ";" + packet.pitch + ":" + timestamp);
                }

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

                data.runInstantAction(ka -> {
                    if(ka.isEnd()) data.teleportsToConfirm--;
                });
                data.playerInfo.posLocs.add(loc);

                data.playerInfo.phaseLoc = loc.clone();

                data.playerInfo.lastTeleportTimer.reset();

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