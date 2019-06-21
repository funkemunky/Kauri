package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.MouseFilter;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Init
@CheckInfo(name = "Aim (Type M)", type = CheckType.AIM, developer = true)
@Packets(packets = {Packet.Client.LOOK, Packet.Client.POSITION_LOOK})
public class AimM extends Check {


    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(getData().getTarget() == null) return;

        debug("f=" + move.getFrom().getYaw() + " t=" + move.getTo().getYaw());
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
