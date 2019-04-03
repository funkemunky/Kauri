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

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.LOOK, Packet.Client.LEGACY_LOOK, Packet.Client.LEGACY_POSITION_LOOK})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Aim (Type D)", description = "Checks for impossible pitch acceleration.", type = CheckType.MOVEMENT, cancelType = CancelType.MOTION, maxVL = 50)
public class AimD extends Check {

    public AimD() {

    }

    private int vl;

    @Setting(name = "threshold.minYawDelta")
    private double minYawDelta = 1.8;

    @Setting(name = "threshold.verbose.max")
    private int vlMax = 30;

    @Setting(name = "threshold.verbose.subtract")
    private int subtract = 4;

    @Setting(name = "combatOnly")
    private boolean combatOnly = true;


    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        val yawDelta = move.getYawDelta();
        val pitchAcceleration = MathUtils.getDelta(move.getPitchDelta(), move.getLastPitchDelta());

        if(!MiscUtils.canDoCombat(combatOnly, getData())) return;


        if (pitchAcceleration == 0 && getData().getPlayer().getVehicle() == null && Math.abs(move.getTo().getPitch()) < 80 && yawDelta > minYawDelta) {
            if (vl++ > vlMax) {
                flag("yaw: " + MathUtils.round(yawDelta, 3), true, true);
            }
        } else vl -= vl > 0 ? subtract : 0;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
