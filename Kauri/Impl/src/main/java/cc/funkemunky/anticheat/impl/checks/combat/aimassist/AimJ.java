package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Init
@CheckInfo(name = "Aim (Type J)", type = CheckType.AIM)
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.LOOK})
public class AimJ extends Check {

    private Verbose secondary = new Verbose();
    private float lastDelta;
    private int vl;
    private long lastFlag;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(move.getYawDelta() == 0 || getData().getLastAttack().hasPassed(20)) return;

        float offset = (move.getYawGCD() / (float) move.getOffset());
        float delta = MathUtils.getDelta(move.getYawDelta(), move.getLastYawDelta()) / offset % 1;
        if(move.getYawDelta() > 1
                && move.getLastYawDelta() > 1
                && move.getYawDelta() < 13
                && move.getLastYawDelta() < 13
                && MathUtils.getDelta(move.getYawDelta(), move.getLastYawDelta()) < 0.5
                && move.getYawGCD() == move.getLastYawGCD()) {
            vl++;
            if(vl >= 3) {
                if(secondary.flag(5, 5000L)) {
                    flag("g=" + move.getYawGCD() + " y1=" + move.getYawDelta() + " y2=" + move.getLastYawDelta(), true, true, AlertTier.HIGH);
                }
                vl = 0;
            }
            debug(Color.Green + "Flag: " + "yaw=" + move.getYawDelta() + " lastYaw=" + move.getLastYawDelta() + " gcd=" + (move.getYawGCD() / (float) move.getOffset()) + " vl=" + vl + " secondary=" + secondary.getVerbose() + " lastFlag=" + (timeStamp - lastFlag));

            lastFlag = timeStamp;
        } else if(timeStamp - lastFlag >= 250) {
            vl = 0;
        }

        lastDelta = delta;

        //debug("offset=" + offset + " delta=" + delta + " gcd=" + move.getYawGCD());
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
