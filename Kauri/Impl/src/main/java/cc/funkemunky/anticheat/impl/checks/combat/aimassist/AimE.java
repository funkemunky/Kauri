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
@CheckInfo(name = "AimA (Type E)", description = "Checks for low common denominators in other rotations - FlyCode.", type = CheckType.AIM, cancelType = CancelType.MOTION, executable = false)
public class AimE extends Check {

    private Verbose verbose = new Verbose();

    @Setting(name = "combatOnly")
    private boolean combatOnly = true;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (!MiscUtils.canDoCombat(combatOnly, getData())) return;

        val move = getData().getMovementProcessor();

        if (move.getYawGCD() < 1E6 && move.getYawDelta() > 0 && !getData().isCinematicMode()) {
            if (verbose.flag(100, 300L)) {
                flag("t: " + verbose.getVerbose() + " g=" + move.getYawGCD(), true, true, AlertTier.HIGH);
            }
        } else verbose.deduct();

        debug(verbose.getVerbose() + ", " + move.getYawGCD() + ", " + getData().isCinematicMode());
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
