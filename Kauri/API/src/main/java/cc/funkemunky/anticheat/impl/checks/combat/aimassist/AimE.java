package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Packets(packets = {
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
@Init
@CheckInfo(name = "Aim (Type E)", description = "Finds any suspiciously consistent variables.", type = CheckType.AIM, executable = false)
public class AimE extends Check {

    private List<Double> gcdValues = new ArrayList<>();
    private double lastRange;
    private Verbose verbose = new Verbose();

    @Setting(name = "combatOnly")
    private boolean combatOnly = true;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        val offset = 16777216L;
        val gcd = MiscUtils.gcd((long) (move.getYawDelta() * offset), (long) (move.getLastYawDelta() * offset));

        if (!MiscUtils.canDoCombat(combatOnly, getData())) return;

        if (Math.abs(move.getTo().getPitch()) < 86.0f && move.getYawDelta() > 0.2 && gcd > 121072L) {
            if (gcdValues.size() >= 5) {
                gcdValues.sort(Comparator.naturalOrder());
                double range = gcdValues.get(gcdValues.size() - 1) - gcdValues.get(0);

                double delta = MathUtils.getDelta(lastRange, range);

                if ((delta < 5 || range < 0.1) && verbose.flag(1, 1750L)) {
                    flag("delta: " + delta + " range: " + range, true, true, AlertTier.POSSIBLE);
                }
                debug(Color.Green + "Range: " + range);
                lastRange = range;
                gcdValues.clear();
            } else {
                gcdValues.add(gcd / 10000D);
            }
        }

        debug("YAW: " + gcd + " OPTIFINE: " + getData().isCinematicMode());
    }


    @Override
    public void onBukkitEvent(Event event) {

    }
}
