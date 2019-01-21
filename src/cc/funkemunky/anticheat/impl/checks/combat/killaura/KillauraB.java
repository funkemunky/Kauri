package cc.funkemunky.anticheat.impl.checks.combat.killaura;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.USE_ENTITY})
public class KillauraB extends Check {

    private int verbose;

    public KillauraB(String name, CancelType cancelType, int maxVL) {
        super(name, cancelType, maxVL);
    }

    @Override
    public Object onPacket(Object packet, String packetType, long timeStamp) {
        WrappedInUseEntityPacket use = new WrappedInUseEntityPacket(packet, getData().getPlayer());

        if (use.getEntity() instanceof LivingEntity) { //We check if it's a LivingEntity since the MathUtils#getOffsetFromEntity requires it.
            //We only get the yaw offset from the center of the player since it simplifies the check (and vertically made auras aren't common).
            Player player = getData().getPlayer();
            double offset = player.getEyeLocation().toVector().subtract(((LivingEntity) use.getEntity()).getEyeLocation().toVector()).normalize().angle(player.getEyeLocation().getDirection());

            if (player.getLocation().distance(use.getEntity().getLocation()) > 1.8 && offset < 2.5) {
                if (verbose++ > 8) {
                    flag(offset + "<-2.5", true, false);
                }
            } else {
                verbose = 0;
            }
            //Bukkit.broadcastMessage("offset: " + offset);
        }
        return packet;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
