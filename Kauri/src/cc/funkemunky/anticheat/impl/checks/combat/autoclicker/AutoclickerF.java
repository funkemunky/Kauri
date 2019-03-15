package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
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
public class AutoclickerF extends Check {

    private int swingTicks, flyingTicks, lastFlyingTicks, outliner, lastOutliner;

    public AutoclickerF(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);
    }

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
            }
        } else {
            flyingTicks++;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
