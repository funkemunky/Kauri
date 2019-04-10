package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Aim (Type A)", type = CheckType.AIM, cancelType = CancelType.MOTION, description = "Checks for the consistency in aim overall", executable = false, maxVL = 80)
public class AimA extends Check {

    private float lastWrapped, lastChange;
    private int vl;

    @Setting(name = "combatOnly")
    private boolean combatOnly = true;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {

        if (!MiscUtils.canDoCombat(combatOnly, getData())) return;

        val yawChange = getData().getMovementProcessor().getYawDelta();
        val pitchChange = getData().getMovementProcessor().getPitchDelta();

        val wrappedCombined = MathUtils.yawTo180F(yawChange + pitchChange);

        val wrappedChange = Math.abs(wrappedCombined - lastWrapped);

        if (wrappedCombined > 1.5 && !getData().isCinematicMode() && wrappedChange < 0.3F && wrappedChange > 0.001F && wrappedChange != lastChange) {
            if (++vl > 4) {
                flag(wrappedCombined + " -> " + lastWrapped + " -> " + (double) Math.round(wrappedChange), true, false);
            }
        } else {
            vl -= vl > 0 ? 1 : 0;
        }

        debug(vl + ": " + wrappedCombined + ", " + wrappedChange + ", " + lastChange + ", " + getData().getMovementProcessor().getOptifineTicks());

        lastWrapped = wrappedCombined;
        lastChange = wrappedChange;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
