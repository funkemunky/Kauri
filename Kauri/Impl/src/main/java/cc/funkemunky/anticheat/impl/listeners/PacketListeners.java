package cc.funkemunky.anticheat.impl.listeners;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.impl.config.CheckSettings;
import cc.funkemunky.api.events.AtlasListener;
import cc.funkemunky.api.events.Listen;
import cc.funkemunky.api.events.ListenerPriority;
import cc.funkemunky.api.events.impl.PacketReceiveEvent;
import cc.funkemunky.api.events.impl.PacketSendEvent;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.in.*;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutPositionPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutTransaction;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

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
                    data.setTeleportTest(System.currentTimeMillis());
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
            }

            if(!event.getType().equalsIgnoreCase(Packet.Server.CHAT)) {
                hopperPup(event.getPacket(), event.getType(), event.getTimeStamp(), data);
                hopper(event.getPacket(), event.getType(), event.getTimeStamp(), data);
            }
        }
        Kauri.getInstance().getProfiler().stop("event:PacketSendEvent");
    }

    @Listen(priority = ListenerPriority.NORMAL)
    public void onEvent(PacketReceiveEvent event) {
        if (event.getPlayer() == null || !Kauri.getInstance().getDataManager().getDataObjects().containsKey(event.getPlayer().getUniqueId()))
            return;

        Kauri.getInstance().getProfiler().start("event:PacketReceiveEvent");
        PlayerData data = Kauri.getInstance().getDataManager().getPlayerData(event.getPlayer().getUniqueId());

        Player player = Bukkit.getPlayer(event.getPlayer().getUniqueId());

        if (data != null) {
            switch (event.getType()) {
                //AimE use transaction packets for checking transPing rather than keepAlives since there really isn't anyone who would spoof the times of these.
                case Packet.Client.TRANSACTION: {
                    WrappedInTransactionPacket packet = new WrappedInTransactionPacket(event.getPacket(), player);

                    if (packet.getAction() == (short) 69) {
                        data.setLastTransPing(data.getTransPing());
                        data.setTransPing(event.getTimeStamp() - data.getLastTransaction());

                        //We use transPing for checking lag since the packet used is little known.
                        //AimE have not seen anyone create a spoof for it or even talk about the possibility of needing one.
                        //Large jumps in latency most of the intervalTime mean lag.
                        data.setLagging(Math.abs(data.getTransPing() - data.getLastTransPing()) > 40);

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
                case Packet.Client.FLYING: {
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
                            data.getLastBlockBreak().reset();
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
                case Packet.Client.ARM_ANIMATION: {
                    WrappedInArmAnimationPacket packet = new WrappedInArmAnimationPacket(event.getPacket(), event.getPlayer());

                    data.getSwingProcessor().onUpdate(packet, event.getTimeStamp());
                    break;
                }
                case Packet.Client.USE_ENTITY: {
                    WrappedInUseEntityPacket packet = new WrappedInUseEntityPacket(event.getPacket(), player);

                    if (packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) {
                        val entity = packet.getEntity();
                        if (entity instanceof LivingEntity) {
                            data.getLastAttack().reset();
                            if(data.getTarget() != null && !data.getTarget().getUniqueId().equals(entity.getUniqueId())) data.getEntityPastLocation().getPreviousLocations().clear();
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
            hopperPup(event.getPacket(), event.getType(), event.getTimeStamp(), data);
        }
        Kauri.getInstance().getProfiler().stop("event:PacketReceiveEvent");
    }

    private void hopper(Object packet, String packetType, long timeStamp, PlayerData data) {
        if ((!CheckSettings.bypassEnabled || !data.getPlayer().hasPermission(CheckSettings.bypassPermission)) && !Kauri.getInstance().getCheckManager().isBypassing(data.getUuid())) {
            Kauri.getInstance().getExecutorService().execute(() -> data.getChecks().stream()
                        .filter(check -> check.isEnabled() && check.getPackets().contains(packetType))
                        .forEach(check -> {
                            Kauri.getInstance().getProfiler().start("checks:" + check.getName());
                            check.onPacket(packet, packetType, timeStamp);
                            Kauri.getInstance().getProfiler().stop("checks:" + check.getName());
                        }));
        }
    }

    private void debug(String packetType, PlayerData data) {
        if (!packetType.contains("Chat") && !packetType.contains("Chunk") && !packetType.contains("Equip")) {
            Kauri.getInstance().getCheckManager().getDebuggingPackets().stream().filter(debugData -> debugData.getDebuggingPlayer().equals(data.getUuid()) && (debugData.getSpecificPacketDebug().equals("*") || packetType.contains(debugData.getSpecificPacketDebug()))).forEach(debugData -> {
                debugData.getPlayer().sendMessage(Color.translate("&8[&cPacketDebug&8] &7" + packetType));
            });
        }
    }

    private void hopperPup(Object packet, String packetType, long timestamp, PlayerData data) {
        Kauri.getInstance().getExecutorService().execute(() ->
                data.getAntiPUP().stream()
                    .filter(pup -> pup.isEnabled() && pup.packets.contains(packetType))
                    .anyMatch(pup -> pup.onPacket(packet, packetType, timestamp)));
    }
}