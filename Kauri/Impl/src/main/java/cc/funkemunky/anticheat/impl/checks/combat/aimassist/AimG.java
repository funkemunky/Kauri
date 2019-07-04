package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.MathUtils;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;

@Init
@CheckInfo(name = "Aim (Type G)", type = CheckType.AIM)
@Packets(packets = {Packet.Client.LOOK, Packet.Client.POSITION_LOOK})
public class AimG extends Check {
    private List<Double> yaws = new ArrayList<>();
    private List<Double> angle = new ArrayList<>();

    private double last;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(getData().getTarget() == null) return;

        float rot = (float) MathUtils.getAimbotOffset(move.getTo(), new CustomLocation(getData().getTarget().getLocation()));
        float dist = MathUtils.getDistanceBetweenAngles(move.getYawDelta(), rot);

        val offset = 16777216L;
        val gcd = MiscUtils.gcd((long) (rot * offset), (long) (move.getYawDelta() * offset)) / 16777216D;
        val shit = rot * gcd;
        val shit2 = move.getYawDelta() / gcd;

        debug("rot=" + rot);
        last = dist;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
