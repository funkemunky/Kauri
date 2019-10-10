package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.TickTimer;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.LOOK})
@Init
@CheckInfo(name = "Aim (Type J)", description = "Checks for overrandomization.", type = CheckType.AIM, maxVL = 60)
public class AimJ extends Check {

    private TickTimer lastEquals = new TickTimer(40);
    private Verbose verbose = new Verbose();
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(move.getPitchGCD() == move.getLastPitchGCD()) {
            lastEquals.reset();
        } else if(lastEquals.getPassed() > 25) {
            if(verbose.flag(50, 8000L)) {
                flag("passed=" + lastEquals.getPassed() + " gcd=" + move.getPitchGCD(), true, true, AlertTier.HIGH);
            }
            debug("passed=" + lastEquals.getPassed() + " gcd=" + move.getPitchGCD() + " vl=" + verbose.getVerbose());
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
