package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.LOOK})
@Init
@CheckInfo(name = "Aim (Type H)", description = "Checks for low common denominators in other rotations - FlyCode.", type = CheckType.AIM, cancelType = CancelType.MOTION, executable = false)
public class AimH extends Check {

    private Verbose verbose = new Verbose();

    @Setting(name = "combatOnly")
    private boolean combatOnly = true;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (!MiscUtils.canDoCombat(combatOnly, getData())) return;

        val yawDifference = getData().getMovementProcessor().getYawDelta();

        val offset = 16777216L;
        val yawGCD = MiscUtils.gcd((long) ((yawDifference) * offset), (long) ((getData().getMovementProcessor().getLastYawDelta()) * offset));

        if (yawGCD < 1E6 && yawDifference > 0 && !getData().isCinematicMode()) {
            if (verbose.flag(100, 200L)) {
                flag("t: " + verbose.getVerbose() + " l: " + String.valueOf(yawGCD).length(), true, true, AlertTier.HIGH);
            }
        } else verbose.deduct();

        debug(verbose.getVerbose() + ", " + String.valueOf(yawGCD).length() + ", " + getData().isCinematicMode());
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
