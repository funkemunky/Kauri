package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.ARM_ANIMATION})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Autoclicker (Type A)", description = "A unique fast click check that detects jumps in CPS much faster.", type = CheckType.AUTOCLICKER, cancelType = CancelType.INTERACT)
public class AutoclickerA extends Check {

    @Setting(name = "maxCPS")
    private int maxCPS = 20;

    @Setting(name = "banCPS")
    private int banCPS = 30;

    @Setting(name = "verboseThreshold")
    private int verboseThreshold = 6;

    @Setting(name = "verboseDeduct")
    private double deduct = 0.25;

    private int ticks;
    private long lastTimeStamp;
    private double vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(MiscUtils.shouldReturnArmAnimation(getData())) return;

        if(timeStamp - lastTimeStamp > 1000L) {
            if(ticks > banCPS) {
                flag("cps: " + ticks, true, true);
            } else
            if(ticks > maxCPS) {
                flag("cps: " + ticks, true, false);
            }
            ticks = 0;
        } else ticks++;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
