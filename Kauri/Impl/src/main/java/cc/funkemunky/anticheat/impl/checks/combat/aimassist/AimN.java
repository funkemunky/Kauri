package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.SkiddedUtils;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

//@Init
@CheckInfo(name = "Aim (Type N)", type = CheckType.AIM, developer = true)
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.LOOK})
public class AimN extends Check {

    private double lastMulti;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(getData().getTarget() != null) {
            Vector rot = SkiddedUtils.getRotation(move.getTo().toLocation(getData().getPlayer().getWorld()),getData().getTarget().getLocation());
            double rotDelta = (SkiddedUtils.clamp180(move.getFrom().getYaw()) - rot.getX());
            double multi = rotDelta / move.getYawDelta();
            double delta = MathUtils.getDelta(multi, lastMulti);
            double inverse = multi / move.getYawDelta();

            if(delta < 0.015) {
                debug(Color.Green + "Flag");
            }

            debug("mult=" + multi + " inverse=" + inverse);
            lastMulti = multi;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
