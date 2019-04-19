package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.LOOK, Packet.Client.LEGACY_LOOK, Packet.Client.LEGACY_POSITION_LOOK})
@Init
@CheckInfo(name = "Aim (Type J)", description = "Checks for low common denominators - FlyCode.", type = CheckType.AIM, cancelType = CancelType.MOTION)
public class AimJ extends Check {

    private Verbose verbose = new Verbose();

    @Setting(name = "combatOnly")
    private boolean combatOnly = true;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (!MiscUtils.canDoCombat(combatOnly, getData())) return;

        val offset = 16777216L;
        val pitchGCD = MiscUtils.gcd((long) ((getData().getMovementProcessor().getPitchDelta()) * offset), (long) ((getData().getMovementProcessor().getLastPitchDelta()) * offset));

        if (String.valueOf(pitchGCD).length() <= 5 && !getData().isCinematicMode()) {
            if (verbose.flag(60, 250L)) {
                flag("t: " + verbose.getVerbose() + " l: " + String.valueOf(pitchGCD).length(), true, true);
            }
        } else verbose.deduct();

        debug(verbose.getVerbose() + ", " + String.valueOf(pitchGCD).length() + ", " + getData().isCinematicMode());
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
