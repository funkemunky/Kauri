package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.event.Event;

import java.util.Deque;
import java.util.LinkedList;

@CheckInfo(name = "Autoclicker (Type I)", description = "Something.", type = CheckType.AUTOCLICKER, cancelType = CancelType.INTERACT)
@Init
@Packets(packets = {Packet.Client.ARM_ANIMATION})
public class AutoclickerI extends Check {

    private long lastTimeStamp, elapsed, ms;

    private int vl, ticks;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {

        ms = timeStamp - lastTimeStamp;

        double cps = 1000D / ms;

        if(cps < 7 || cps > 19) {
            lastTimeStamp = timeStamp;
            return;
        }

        if(ticks++ > 60) {
            long time = MathUtils.elapsed(elapsed);

            if(time < 7800) {
                flag("time: " + time +"ms", true, true);
            }
            debug("MS: " + time + " DUR: " + DurationFormatUtils.formatDurationWords(time, true, true));
            ticks = 0;
            elapsed = timeStamp;
        }
        debug("CPS: " + cps);

        lastTimeStamp = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
