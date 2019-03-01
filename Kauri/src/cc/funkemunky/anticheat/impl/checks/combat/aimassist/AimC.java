package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.LOOK, Packet.Client.LEGACY_LOOK, Packet.Client.LEGACY_POSITION_LOOK})
public class AimC extends Check {

    public AimC(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);
    }

    private int vl;

    @Setting(name = "threshold.vl.max")
    private int vlMax = 9;

    @Setting(name = "threshold.vl.subtract")
    public int subtract = 2;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        val yawDelta = move.getYawDelta();
        val yawAccel = MathUtils.getDelta(move.getYawDelta(), move.getLastYawDelta());
        val pitchAccel = MathUtils.getDelta(move.getPitchDelta(), move.getLastPitchDelta());

        if(yawAccel == 0 && pitchAccel == 0 && yawDelta > 0.1) {
            if(vl++ > vlMax) {
                flag("p+y acceleration = 0; vl=" + vl, true, true);
            }
        } else vl-= vl > 0 ? subtract : 0;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
