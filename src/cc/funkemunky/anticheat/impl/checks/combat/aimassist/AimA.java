package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.FLYING,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
public class AimA extends Check {

    private float lastYaw, lastPitch, lastWrapped, lastChange;
    private int vl;
    public AimA(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    @Override
    public Object onPacket(Object packet, String packetType, long timeStamp) {
        switch (packetType) {
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.LOOK:
            case Packet.Client.LEGACY_POSITION_LOOK:
            case Packet.Client.LEGACY_LOOK: {
                val yaw = this.getData().getPlayer().getLocation().getYaw();
                val pitch = this.getData().getPlayer().getLocation().getPitch();

                val yawChange = Math.abs(yaw - lastYaw);
                val pitchChange = Math.abs(pitch - lastPitch);

                val wrappedCombined = MiscUtils.wrapAngleTo180_float(yawChange + pitchChange);

                val wrappedChange = Math.abs(wrappedCombined - lastWrapped);

                if (wrappedCombined > 1.5 && !getData().isCinematicMode() && wrappedChange < 0.3F && wrappedChange > 0.001F && wrappedChange != lastChange) {
                    if (++vl > 4) {
                        flag(wrappedCombined + " -> " + lastWrapped + " -> " + (double) Math.round(wrappedChange), true, false);
                    }
                } else {
                    vl -= vl > 0 ? 1 : 0;
                }

                debug(vl + ": " + wrappedCombined + ", " + wrappedChange + ", " + lastChange + ", " + getData().getMovementProcessor().getOptifineTicks());

                lastPitch = pitch;
                lastYaw = yaw;
                lastWrapped = wrappedCombined;
                lastChange = wrappedChange;
                break;
            }
        }

        return packet;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
