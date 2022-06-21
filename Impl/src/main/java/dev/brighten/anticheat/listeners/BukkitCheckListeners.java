package dev.brighten.anticheat.listeners;

import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.api.KauriAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;

@Init
public class BukkitCheckListeners implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        //Packet exemption check
        if(KauriAPI.INSTANCE.getPacketExemptedPlayers().contains(event.getPlayer().getUniqueId())) return;

        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data != null) {
            data.playerInfo.breakingBlock = event.getAction().equals(Action.LEFT_CLICK_BLOCK);
            data.checkManager.runEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageByEntityEvent event) {
        if(event.getDamager() instanceof Player) {
            //Packet exemption check
            if(KauriAPI.INSTANCE.getPacketExemptedPlayers().contains(event.getDamager().getUniqueId())) return;

            ObjectData data = Kauri.INSTANCE.dataManager.getData((Player) event.getDamager());

            if(data != null) {
                data.checkManager.runEvent(event);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        //Packet exemption check
        if(KauriAPI.INSTANCE.getPacketExemptedPlayers().contains(event.getPlayer().getUniqueId())) return;

        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data != null) {
            data.checkManager.runEvent(event);
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onEvent(PlayerTeleportEvent event) {
        //Packet exemption check
        if(KauriAPI.INSTANCE.getPacketExemptedPlayers().contains(event.getPlayer().getUniqueId())) return;

        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data != null) {
            data.moveProcessor.moveTo(event.getTo());
            if(data.checkManager != null)
            data.checkManager.runEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEvent(PlayerRespawnEvent event) {
        //Packet exemption check
        if(KauriAPI.INSTANCE.getPacketExemptedPlayers().contains(event.getPlayer().getUniqueId())) return;

        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data != null) {
            data.moveProcessor.moveTo(event.getRespawnLocation());
            if(data.checkManager != null)
            data.checkManager.runEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlock(BlockPlaceEvent event) {
        //Packet exemption check
        if(KauriAPI.INSTANCE.getPacketExemptedPlayers().contains(event.getPlayer().getUniqueId())) return;

        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data != null) {
            data.checkManager.runEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBookEdit(PlayerEditBookEvent event) {
        //Packet exemption check
        if(KauriAPI.INSTANCE.getPacketExemptedPlayers().contains(event.getPlayer().getUniqueId())) return;

        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data != null) {
            data.checkManager.runEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEvent(SignChangeEvent event) {
        //Packet exemption check
        if(KauriAPI.INSTANCE.getPacketExemptedPlayers().contains(event.getPlayer().getUniqueId())) return;

        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data != null) {
            data.checkManager.runEvent(event);
        }
    }
}
