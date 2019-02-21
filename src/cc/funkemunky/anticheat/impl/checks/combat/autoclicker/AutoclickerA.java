package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.ARM_ANIMATION})
public class AutoclickerA extends Check {

    @Setting
    private int maxCPS = 20;

    @Setting
    private int banCPS = 30;

    @Setting
    private int verboseThreshold = 6;

    private long lastTimeStamp;
    private int vl;

    public AutoclickerA(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (MiscUtils.shouldReturnArmAnimation(getData())) return;;

        val cps = (double) lastTimeStamp / timeStamp;

        if (cps > maxCPS) {
            if (vl++ > verboseThreshold) {
                flag(cps + ">-" + maxCPS, false, cps > banCPS);
            }
        } else {
            vl -= vl > 0 ? 1 : 0;
        }

        lastTimeStamp = timeStamp;
        return;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
