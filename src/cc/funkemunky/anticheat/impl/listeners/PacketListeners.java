package cc.funkemunky.anticheat.impl.listeners;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.event.custom.PacketRecieveEvent;
import cc.funkemunky.api.event.custom.PacketSendEvent;
import cc.funkemunky.api.event.system.EventMethod;
import cc.funkemunky.api.event.system.Listener;
import cc.funkemunky.api.tinyprotocol.api.NMSObject;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.in.*;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutTransaction;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.Init;
import org.bukkit.entity.LivingEntity;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Init
public class PacketListeners implements Listener {

    @EventMethod
    public void onEvent(PacketSendEvent event) {
        if (event.getPlayer() == null || !event.getPlayer().isOnline()) return;
        Kauri.getInstance().getProfiler().start("event:PacketSendEvent");
        PlayerData data = Kauri.getInstance().getDataManager().getPlayerData(event.getPlayer().getUniqueId());

        if (data != null) {
            switch (event.getType()) {
                case Packet.Server.POSITION: {
                    data.getLastServerPos().reset();
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

            Map.Entry<Object, Boolean> result = hopper(event.getPacket(), event.getType(), event.getTimeStamp(), data);

            if(result != null) {
                event.setCancelled(result.getValue());
                event.setPacket(result.getKey());
            }
        }
        Kauri.getInstance().getProfiler().stop("event:PacketSendEvent");
    }

    @EventMethod
    public void onEvent(PacketRecieveEvent event) {
        if (event.getPlayer() == null) return;
        Kauri.getInstance().getProfiler().start("event:PacketReceiveEvent");
        PlayerData data = Kauri.getInstance().getDataManager().getPlayerData(event.getPlayer().getUniqueId());

        if (data != null) {
            switch (event.getType()) {
                //I use transaction packets for checking transPing rather than keepAlives since there really isn't anyone who would spoof the times of these.
                case Packet.Client.TRANSACTION: {
                    WrappedInTransactionPacket packet = new WrappedInTransactionPacket(event.getPacket(), event.getPlayer());

                    if (packet.getAction() == (short) 69) {
                        data.setLastTransPing(data.getTransPing());
                        data.setTransPing(event.getTimeStamp() - data.getLastTransaction());

                        //We use transPing for checking lag since the packet used is little known.
                        //I have not seen anyone create a spoof for it or even talk about the possibility of needing one.
                        //Large jumps in latency most of the time mean lag.
                        data.setLagging(Math.abs(data.getTransPing() - data.getLastTransPing()) > 35);

                        if(data.isLagging()) data.getLastLag().reset();
                    }
                    break;
                }
                case Packet.Client.ENTITY_ACTION: {
                    WrappedInEntityActionPacket packet = new WrappedInEntityActionPacket(event.getPacket(), event.getPlayer());

                    data.getActionProcessor().update(packet);
                    break;
                }
                case Packet.Client.CLOSE_WINDOW: {
                    WrappedInCloseWindowPacket packet = new WrappedInCloseWindowPacket(event.getPacket(), event.getPlayer());

                    data.getActionProcessor().update(packet);
                    break;
                }
                case Packet.Client.KEEP_ALIVE:
                    data.setLastPing(data.getPing());
                    data.setPing(event.getTimeStamp() - data.getLastKeepAlive());
                    break;
                case Packet.Client.ABILITIES: {
                    WrappedInAbilitiesPacket packet = new WrappedInAbilitiesPacket(event.getPacket(), event.getPlayer());

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
                    WrappedInFlyingPacket packet = new WrappedInFlyingPacket(event.getPacket(), event.getPlayer());

                    data.getMovementProcessor().update(data, packet);
                    data.getVelocityProcessor().update(packet);
                    break;
                }
                case Packet.Client.BLOCK_DIG: {
                    WrappedInBlockDigPacket blockDig = new WrappedInBlockDigPacket(event.getPacket(), event.getPlayer());

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
                    WrappedInBlockPlacePacket packet = new WrappedInBlockPlacePacket(event.getPacket(), event.getPlayer());

                    if (packet.getItemStack() != null && packet.getPosition() != null && packet.getPosition().getX() != -1 && packet.getPosition().getY() != -1 && packet.getPosition().getZ() != -1) {
                        data.getLastBlockPlace().reset();
                    }
                    break;
                }
                case Packet.Client.USE_ENTITY:
                    WrappedInUseEntityPacket packet = new WrappedInUseEntityPacket(event.getPacket(), event.getPlayer());

                    if(packet.getEntity() instanceof LivingEntity) {
                        data.getLastAttack().reset();
                    }
                    break;
            }
            Map.Entry<Object, Boolean> result = hopper(event.getPacket(), event.getType(), event.getTimeStamp(), data);

            if(result != null) {
                event.setCancelled(result.getValue());
                event.setPacket(result.getKey());
            }
        }
        Kauri.getInstance().getProfiler().stop("event:PacketReceiveEvent");
    }

    private Map.Entry<Object, Boolean> hopper(Object packet, String packetType, long timeStamp, PlayerData data) {
        FutureTask<Map.Entry<Object, Boolean>> task = new FutureTask<>(() -> {
            for (Check check : data.getChecks()) {
                if(check.isEnabled() && check.getClass().isAnnotationPresent(Packets.class) && Arrays.asList(check.getClass().getAnnotation(Packets.class).packets()).contains(packetType)) {
                    Kauri.getInstance().getProfiler().start("check:" + check.getName());
                    Object object = check.onPacket(packet, packetType, timeStamp);

                    if(object instanceof NMSObject) {
                        NMSObject nObject = (NMSObject) object;

                        if(nObject.isCancelled() || packet != nObject.getObject()) {
                            new AbstractMap.SimpleEntry<>(nObject.getObject(), nObject.isCancelled());
                        }
                    }
                    Kauri.getInstance().getProfiler().stop("check:" + check.getName());
                }
            }
            return null;
        });

        Atlas.getInstance().getThreadPool().submit(task);

        try {
            return task.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
