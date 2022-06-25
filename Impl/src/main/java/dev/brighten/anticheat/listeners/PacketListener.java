package dev.brighten.anticheat.listeners;

import cc.funkemunky.api.com.github.retrooper.packetevents.event.PacketListenerAbstract;
import cc.funkemunky.api.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import cc.funkemunky.api.com.github.retrooper.packetevents.event.PacketSendEvent;
import cc.funkemunky.api.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.client.*;
import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.server.*;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.processing.PacketProcessor;
import dev.brighten.anticheat.processing.thread.ThreadHandler;
import dev.brighten.api.KauriAPI;
import org.bukkit.entity.Player;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.stream.IntStream;

public class PacketListener extends PacketListenerAbstract {
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if(event.getPlayer() == null) return;

        ObjectData data = Kauri.INSTANCE.dataManager.getData((Player)event.getPlayer());

        if(data == null || data.checkManager == null) {
            System.out.println("Null: " + (data == null) + "," + (data.checkManager == null));
            return;
        }

        //Packet exemption check
        if(KauriAPI.INSTANCE.getPacketExemptedPlayers().contains(data.uuid)) return;

        PacketReceiveEvent copy = event.clone();

        if(data.checkManager.runEvent(event)) {
            event.setCancelled(true);
            System.out.println("Cancelled packet: " + event.getPacketType().getName());
        }

        if(PacketProcessor.simLag && event.getPacketType() == PacketType.Play.Client.PLAYER_FLYING) {
            IntStream.range(0, PacketProcessor.amount).forEach(i -> {
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
                Kauri.INSTANCE.packetProcessor.processClient(data, copy);
            } catch(Exception e) {
                e.printStackTrace();
            }
            copy.cleanUp();
        });
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if(event.getPlayer() == null) return;
        ObjectData data = Kauri.INSTANCE.dataManager.getData((Player)event.getPlayer());

        if(data == null || data.checkManager == null) return;

        //Packet exemption check
        if(KauriAPI.INSTANCE.getPacketExemptedPlayers().contains(data.uuid)) return;

        PacketSendEvent copy = event.clone();

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
        } else if(event.getPacketType() == PacketType.Play.Server.WINDOW_CONFIRMATION) {

        } else if(event.getPacketType() == PacketType.Play.Server.ENTITY_VELOCITY) {
            WrapperPlayServerEntityVelocity packet = new WrapperPlayServerEntityVelocity(event);

            if(data.checkManager.runPacketCancellable(packet, event.getTimestamp())) {
                event.setCancelled(true);
            }
        }

        ThreadHandler.INSTANCE.getThread(data).runTask(() -> {
            if (data.checkManager == null) return;
            try {
                Kauri.INSTANCE.packetProcessor.processServer(data, copy);
            } catch(Exception e) {
                e.printStackTrace();
            }
            copy.cleanUp();
        });
    }
}
