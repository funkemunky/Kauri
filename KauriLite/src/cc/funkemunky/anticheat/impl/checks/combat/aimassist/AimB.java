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
@CheckInfo(name = "Aim (Type B)", description = "Makes sure the aim acceleration is legitimate.", type = CheckType.AIM, cancelType = CancelType.MOTION, maxVL = 80)
public class AimB extends Check {

    private int vl;

    @Setting(name = "threshold.vl.max")
    private int vlMax = 9;

    @Setting(name = "threshold.vl.subtract")
    private int subtract = 2;

    @Setting(name = "combatOnly")
    private boolean combatOnly = true;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        val yawDelta = move.getYawDelta();
        val yawAccel = MathUtils.getDelta(move.getYawDelta(), move.getLastYawDelta());
        val pitchAccel = MathUtils.getDelta(move.getPitchDelta(), move.getLastPitchDelta());

        if(!MiscUtils.canDoCombat(combatOnly, getData())) return;


        if (yawAccel == 0 && pitchAccel == 0 && getData().getPlayer().getVehicle() == null && Math.abs(move.getTo().getPitch()) < 80 && yawDelta > 0.1) {
            if (vl++ > vlMax) {
                flag("p+y acceleration = 0; vl=" + vl, true, true);
            }
        } else vl -= vl > 0 ? subtract : 0;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
