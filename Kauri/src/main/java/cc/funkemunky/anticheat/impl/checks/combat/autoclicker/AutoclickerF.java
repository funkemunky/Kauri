package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.ARM_ANIMATION,
        Packet.Client.FLYING,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Autoclicker (Type F)", description = "Compares the CPS of an autoclicker a certain frequency.", type = CheckType.AUTOCLICKER, cancelType = CancelType.INTERACT, maxVL = 8)
public class AutoclickerF extends Check {

    private int swingTicks, flyingTicks, lastFlyingTicks, outliner, lastOutliner;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (MiscUtils.shouldReturnArmAnimation(getData())) return;
        if (packetType.equals(Packet.Client.ARM_ANIMATION)) {
            swingTicks++;

            if (swingTicks == 5) {

                if (flyingTicks == lastFlyingTicks) {
                    outliner++;
                }

                if (outliner == lastOutliner) {
                    outliner = 0;
                }

                if (flyingTicks > 22) {
                    outliner /= 4;
                }

                if (outliner > 6) {
                    this.flag("L: " + outliner, false, true);
                }

                lastFlyingTicks = flyingTicks;
                lastOutliner = outliner;
                flyingTicks = 0;
                swingTicks = 0;

                debug("OUT: " + outliner);
            }
        } else {
            flyingTicks++;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
