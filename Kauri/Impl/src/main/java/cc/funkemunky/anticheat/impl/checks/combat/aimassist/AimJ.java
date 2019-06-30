package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MathUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@Init
@CheckInfo(name = "Aim (Type J)", type = CheckType.AIM)
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.LOOK})
public class AimJ extends Check {

    private double lastSigMoid;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        double sigmoid = MathUtils.sigmoid(move.getYawDelta());
        double round = MathUtils.round(sigmoid);
        double normalFuckingValue = MathUtils.normalize(round, sigmoid, lastSigMoid);

        debug("" + normalFuckingValue);
        lastSigMoid = sigmoid;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
