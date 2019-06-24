package cc.funkemunky.anticheat.impl.listeners;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckSettings;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.events.AtlasListener;
import cc.funkemunky.api.events.Listen;
import cc.funkemunky.api.events.ListenerPriority;
import cc.funkemunky.api.events.impl.PacketReceiveEvent;
import cc.funkemunky.api.events.impl.PacketSendEvent;
import cc.funkemunky.api.tinyprotocol.api.NMSObject;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.in.*;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutEntityMetadata;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutPositionPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutTransaction;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.tinyprotocol.packet.types.WrappedWatchableObject;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;

@Init
public class PacketListeners implements AtlasListener {

    @Listen(priority = ListenerPriority.HIGHEST)
    public void onEvent(PacketSendEvent event) {
        if (event.getPlayer() == null || !event.getPlayer().isOnline() || !Kauri.getInstance().getDataManager().getDataObjects().containsKey(event.getPlayer().getUniqueId()))
            return;

        Kauri.getInstance().getProfiler().start("event:PacketSendEvent");
        PlayerData data = Kauri.getInstance().getDataManager().getPlayerData(event.getPlayer().getUniqueId());

        if (data != null) {
            switch (event.getType()) {
                case Packet.Server.POSITION: {
                    WrappedOutPositionPacket position = new WrappedOutPositionPacket(event.getPacket(), event.getPlayer());

                    data.setLastServerPosStamp(event.getTimeStamp());
                    data.getTeleportLocations().add(new Vector(position.getX(), position.getY(), position.getZ()));
                    data.getVelocityProcessor().velocityX = data.getVelocityProcessor().velocityY = data.getVelocityProcessor().velocityZ = 0;
                    data.getVelocityProcessor().setAttackedSinceVelocity(false);
                    break;
                }
                case Packet.Server.KEEP_ALIVE:
                    data.setLastKeepAlive(event.getTimeStamp());
                    TinyProtocolHandler.sendPacket(event.getPlayer(), new WrappedOutTransaction(0, (short) 69, false).getObject());
                    break;
                case Packet.Server.TRANSACTION: {
                    WrappedOutTransaction packet = new WrappedOutTransaction(event.getPacket(), event.getPlayer());

                    if (packet.getAction() == (short) 69) {
                        data.setLastTransaction(event.getTimeStamp());
                    }
                    break;
                }
                case Packet.Server.ENTITY_VELOCITY: {
                    WrappedOutVelocityPacket packet = new WrappedOutVelocityPacket(event.getPacket(), event.getPlayer());

                    data.getVelocityProcessor().update(packet);
                    break;
                }
                case Packet.Server.ENTITY_METADATA: {
                    WrappedOutEntityMetadata packet = new WrappedOutEntityMetadata(event.getPacket(), event.getPlayer());

                    if(packet.getWatchableObjects().size() > 7) {
                        WrappedWatchableObject object7 = new WrappedWatchableObject(packet.getWatchableObjects().get(7)), object6 = new WrappedWatchableObject(packet.getWatchableObjects().get(6));

                        if(object7.getWatchedObject() instanceof Float) {
                            object7.setWatchedObject(1.0f);
                            object7.setPacket(NMSObject.Type.WATCHABLE_OBJECT, object7.getObjectType(), object7.getDataValueId(), object7.getWatchedObject());

                            packet.getWatchableObjects().set(7, object7.getObject());
                            WrappedOutEntityMetadata toSet = new WrappedOutEntityMetadata(packet.getEntityId(), packet.getWatchableObjects());
                            event.setPacket(toSet.getObject());
                        } else if(object6.getWatchedObject() instanceof Float) {
                            object6.setWatchedObject(1.0f);
                            object6.setPacket(NMSObject.Type.WATCHABLE_OBJECT, object6.getObjectType(), object6.getDataValueId(), object6.getWatchedObject());

                            packet.getWatchableObjects().set(6, object6.getObject());
                            WrappedOutEntityMetadata toSet = new WrappedOutEntityMetadata(packet.getEntityId(), packet.getWatchableObjects());
                            event.setPacket(toSet.getObject());
                        }
                    }
                    break;
                }
            }

            hopper(event.getPacket(), event.getType(), event.getTimeStamp(), data);
            debug(event.getType(), data);
            if (hopperPup(event.getPacket(), event.getType(), event.getTimeStamp(), data)) event.setCancelled(true);

        }
        Kauri.getInstance().getProfiler().stop("event:PacketSendEvent");
    }

    @Listen(priority = ListenerPriority.LOW)
    public void onEvent(PacketReceiveEvent event) {
        if (event.getPlayer() == null || !Kauri.getInstance().getDataManager().getDataObjects().containsKey(event.getPlayer().getUniqueId()))
            return;

        Kauri.getInstance().getProfiler().start("event:PacketReceiveEvent");
        PlayerData data = Kauri.getInstance().getDataManager().getPlayerData(event.getPlayer().getUniqueId());

        Player player = Bukkit.getPlayer(event.getPlayer().getUniqueId());

        if (data != null) {
            switch (event.getType()) {
                //AimH use transaction packets for checking transPing rather than keepAlives since there really isn't anyone who would spoof the times of these.
                case Packet.Client.TRANSACTION: {
                    WrappedInTransactionPacket packet = new WrappedInTransactionPacket(event.getPacket(), player);

                    if (packet.getAction() == (short) 69) {
                        data.setLastTransPing(data.getTransPing());
                        data.setTransPing(event.getTimeStamp() - data.getLastTransaction());

                        //We use transPing for checking lag since the packet used is little known.
                        //AimH have not seen anyone create a spoof for it or even talk about the possibility of needing one.
                        //Large jumps in latency most of the intervalTime mean lag.
                        data.setLagging(Math.abs(data.getTransPing() - data.getLastTransPing()) > 35);

                        if (data.isLagging()) data.getLastLag().reset();
                    }
                    break;
                }
                case Packet.Client.ENTITY_ACTION: {
                    WrappedInEntityActionPacket packet = new WrappedInEntityActionPacket(event.getPacket(), player);

                    data.getActionProcessor().update(packet);
                    break;
                }
                case Packet.Client.CLOSE_WINDOW: {
                    WrappedInCloseWindowPacket packet = new WrappedInCloseWindowPacket(event.getPacket(), player);

                    data.getActionProcessor().update(packet);
                    break;
                }
                case Packet.Client.KEEP_ALIVE: {
                    data.setLastPing(data.getPing());
                    data.setPing(event.getTimeStamp() - data.getLastKeepAlive());
                    break;
                }
                case Packet.Client.ABILITIES: {
                    WrappedInAbilitiesPacket packet = new WrappedInAbilitiesPacket(event.getPacket(), player);

                    data.setAbleToFly(packet.isAllowedFlight());
                    data.setCreativeMode(packet.isCreativeMode());
                    data.setInvulnerable(packet.isInvulnerable());
                    data.setFlying(packet.isFlying());
                    data.setWalkSpeed(packet.getWalkSpeed());
                    data.setFlySpeed(packet.getFlySpeed());
                    break;
                }
                case Packet.Client.POSITION:
                case Packet.Client.POSITION_LOOK:
                case Packet.Client.LOOK:
                case Packet.Client.LEGACY_POSITION:
                case Packet.Client.FLYING:
                case Packet.Client.LEGACY_POSITION_LOOK:
                case Packet.Client.LEGACY_LOOK: {
                    WrappedInFlyingPacket packet = new WrappedInFlyingPacket(event.getPacket(), player);

                    data.getMovementProcessor().update(data, packet);
                    data.getVelocityProcessor().update(packet);
                    if (data.getMovementProcessor().getTo() == null) return;
                    break;
                }
                case Packet.Client.BLOCK_DIG: {
                    WrappedInBlockDigPacket blockDig = new WrappedInBlockDigPacket(event.getPacket(), player);

                    switch (blockDig.getAction()) {
                        case START_DESTROY_BLOCK:
                            data.setBreakingBlock(true);
                            break;
                        case ABORT_DESTROY_BLOCK:
                        case STOP_DESTROY_BLOCK:
                            data.setBreakingBlock(false);
                            break;
                    }
                    break;
                }
                case Packet.Client.BLOCK_PLACE: {
                    WrappedInBlockPlacePacket packet = new WrappedInBlockPlacePacket(event.getPacket(), player);

                    if (packet.getItemStack() != null && packet.getPosition() != null && packet.getPosition().getX() != -1 && packet.getPosition().getY() != -1 && packet.getPosition().getZ() != -1) {
                        Location location = new Location(packet.getPlayer().getWorld(), packet.getPosition().getX(), packet.getPosition().getY(), packet.getPosition().getZ());

                        val block = BlockUtils.getBlock(location);
                        if (location.distance(packet.getPlayer().getLocation()) < 15 && block != null && (block.getType().isSolid() || BlockUtils.isLiquid(block))) {
                            data.getLastBlockPlace().reset();
                        }
                    }
                    break;
                }
                case Packet.Client.USE_ENTITY: {
                    WrappedInUseEntityPacket packet = new WrappedInUseEntityPacket(event.getPacket(), player);

                    if (packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) {
                        val entity = packet.getEntity();
                        if (entity instanceof LivingEntity) {
                            data.getLastAttack().reset();
                            data.setTarget((LivingEntity) entity);

                            if (entity instanceof Player) {
                                PlayerData dataEntity = Kauri.getInstance().getDataManager().getPlayerData(entity.getUniqueId());

                                if (dataEntity != null) {
                                    dataEntity.setAttacker(packet.getPlayer());
                                }
                            }
                        }
                    }
                    data.getVelocityProcessor().update(packet);
                    break;
                }
            }
            debug(event.getType(), data);
            hopper(event.getPacket(), event.getType(), event.getTimeStamp(), data);
            if (hopperPup(event.getPacket(), event.getType(), event.getTimeStamp(), data)) event.setCancelled(true);
        }
        Kauri.getInstance().getProfiler().stop("event:PacketReceiveEvent");
    }

    private void hopper(Object packet, String packetType, long timeStamp, PlayerData data) {
        if ((!CheckSettings.bypassEnabled || !data.getPlayer().hasPermission(CheckSettings.bypassPermission)) && !Kauri.getInstance().getCheckManager().isBypassing(data.getUuid())) {
            Kauri.getInstance().getCheckExecutor().execute(() -> {
                if(data.getPacketChecks().containsKey(packetType)) {
                    data.getPacketChecks().getOrDefault(packetType, new ArrayList<>()).parallelStream().filter(Check::isEnabled).forEach(check ->
                    {
                        Kauri.getInstance().getProfiler().start("check:" + check.getName() + "#" + packetType);
                        check.onPacket(packet, packetType, timeStamp);
                        Kauri.getInstance().getProfiler().stop("check:" + check.getName()  + "#" + packetType);
                    });
                }
            });
        }
    }

    private void debug(String packetType, PlayerData data) {
        if (!packetType.contains("Chat") && !packetType.contains("Chunk") && !packetType.contains("Equip")) {
            Atlas.getInstance().getThreadPool().execute(() -> {
                Kauri.getInstance().getDataManager().getDataObjects().values().stream().filter(debugData -> debugData.isDebuggingPackets() && debugData.getDebuggingPlayer().equals(data.getUuid()) && (debugData.getSpecificPacketDebug().equals("*") || packetType.contains(debugData.getSpecificPacketDebug()))).forEach(debugData -> {
                    debugData.getPlayer().sendMessage(Color.translate("&8[&cPacketDebug&8] &7" + packetType));
                });
            });
        }
    }

    private boolean hopperPup(Object packet, String packetType, long timestamp, PlayerData data) {
        return data.getAntiPupMethods().getOrDefault(packetType, new ArrayList<>()).stream().anyMatch(pup -> pup.onPacket(packet, packetType, timestamp));
    }
}