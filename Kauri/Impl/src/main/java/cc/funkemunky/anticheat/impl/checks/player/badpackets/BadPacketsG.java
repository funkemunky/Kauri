package cc.funkemunky.anticheat.impl.checks.player.badpackets;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.DynamicRollingAverage;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.FLYING,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "BadPackets (Type G)", description = "Checks frequency of incoming packets. More detection, but less reliable.", type = CheckType.BADPACKETS, maxVL = 200, executable = false)
public class BadPacketsG extends Check {

    @Setting(name = "usingPaperSpigot")
    public boolean usingPaper = false;

    @Setting(name = "lenience")
    public float deltaBalance = 0.04f;

    @Setting(name = "threshold.vl.max")
    private int maxVL = 50;

    private long lastFlying;
    private int vl;
    private DynamicRollingAverage average = new DynamicRollingAverage(20);

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val data = this.getData();

        if (data.getLastLogin().hasNotPassed(15) || data.isServerPos()) {
            lastFlying = timeStamp;
            return;
        }

        if(!getData().isLagging() && timeStamp - lastFlying > 5) {
            this.average.add(timeStamp - lastFlying);
        } else {
            lastFlying = timeStamp;
            return;
        }

        if(Atlas.getInstance().getCurrentTicks() % 2 == 0) {
            val max = Math.min(7.065, Math.sqrt((50 - (1000 / Kauri.getInstance().getTps())) + 50));
            val stdDev = Math.sqrt(this.average.getAverage());

            if (!MathUtils.approxEquals(deltaBalance, max, stdDev) && getData().getTransPing() < 150 && !getData().isLagging() && stdDev < max && getData().getLastLag().hasPassed(10)) {
                if (vl++ > maxVL) {
                    this.flag("S: " + stdDev, false, true, vl > 60 ? AlertTier.HIGH : AlertTier.LIKELY);
                    vl = 0;
                    average.clearValues();
                }
            } else vl -= vl > 0 ? 3 : 0;

            debug("MS:" + (timeStamp - lastFlying) + "STD: " + stdDev + " VL: " + vl + " max=" + max + " size=" + average.isReachedSize());
        }
        this.lastFlying = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
