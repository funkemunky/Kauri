package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.event.Event;
@Packets(packets = {
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
//@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Aim (Type K)", description = "Looks for weird offests using angular calculation.", maxVL = 50, cancelType = CancelType.MOTION, type = CheckType.AIM, developer = true)
public class AimK extends Check {

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(getData().getTarget() != null) {
            val offset = MathUtils.getRotations(toLocation(move.getTo()), toLocation(getData().getEntityPastLocation().getPreviousLocation(getData().getPing())));
            val yaw = MathUtils.yawTo180F(move.getTo().getYaw());
            val yawOffset = MathUtils.yawTo180F(offset[0]);
            val shit = yawOffset / move.getTo().getYaw() + move.getYawDelta();



            debug("offset=" + yawOffset + " yaw=" + yaw + " delta=" + move.getYawDelta() + " shit=" + shit);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private Location toLocation(CustomLocation loc) {
        return loc.toLocation(getData().getPlayer().getWorld());
    }
}
