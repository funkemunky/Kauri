package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.math.MCSmooth;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
public class AimD extends Check {

    public AimD(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);
    }

    private final MCSmooth mouseFilterXAxis = new MCSmooth(), mouseFilterYAxis = new MCSmooth();

    private float lastYawChange, lastPitchChange;
    private int streak, vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val to = getData().getMovementProcessor().getTo();
        val from = getData().getMovementProcessor().getFrom();

        val yawChange = Math.abs(from.getYaw() - to.getYaw());
        val pitchChange = Math.abs(from.getPitch() - to.getPitch());

        val yawRaw = mouseFilterXAxis.smooth(yawChange, lastYawChange);
        val pitchRaw = mouseFilterYAxis.smooth(pitchChange, lastPitchChange);

        if (yawRaw < 0 || pitchRaw < 0) {
            if (++streak > 5) {
                if (++vl > 3) {
                    this.flag("Y &R P < 0", false, false);
                }
            } else {
                vl = 0;
            }
        } else {
            streak = 0;
        }

        debug(yawRaw + ", " + pitchRaw + ", " + getData().isCinematicMode());

        this.lastYawChange = yawChange;
        this.lastPitchChange = pitchChange;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
