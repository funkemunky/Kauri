package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

import java.util.HashSet;
import java.util.Set;

@Init
@CheckInfo(name = "Aim (Type J)", type = CheckType.AIM)
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.LOOK})
public class AimJ extends Check {

    private double vl;
    private Verbose verbose = new Verbose();

    private Set<Long> dups = new HashSet<>();
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(move.getYawDelta() == 0 || getData().getLastAttack().hasPassed(20)) return;

        float offset = (move.getYawGCD() / (float) move.getOffset());
        float delta = MathUtils.getDelta(move.getYawDelta(), move.getLastYawDelta()) / offset % 1;

        if(move.getYawGCD() == move.getLastYawGCD() && delta == 0  && move.getYawDelta() > 0.45 && offset < 1) {
            double duplicates = verbose.getVerbose() - dups.size();
            if(verbose.flag(5, 800L) && duplicates > 6) {
                flag("holy shit", true, true, AlertTier.HIGH);
            }

            if(verbose.getVerbose()  < 2) {
                dups.clear();
            } else {
                dups.add(move.getYawGCD());
            }
            debug(Color.Green + "Flag: " + "vl= " + vl + " verbose=" + verbose.getVerbose() + " offset=" + offset + " delta="+ delta + " duplicates=" + duplicates + " yaw=" + move.getYawDelta());
        } else vl = vl > 0 ? vl - 0.5 : 0;

       //debug("offset=" + offset + " delta=" + delta + " gcd=" + move.getYawGCD());
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
