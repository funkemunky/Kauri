package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

import java.util.Deque;
import java.util.LinkedList;

@Packets(packets = {
        Packet.Client.LOOK,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK,})
@Init
@CheckInfo(name = "Aim (Type H)", description = "Looks for a common ratio of pitch movement in AimBots.", type = CheckType.AIM, executable = false, developer = true)
public class AimH extends Check {

    private final Deque<Float> pitchDeque = new LinkedList<>();
    private int vl;

    @Setting(name = "combatOnly")
    private boolean combatOnly = true;

    @Setting(name = "threshold.vl.max")
    private int vlMax = 2;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val from = this.getData().getMovementProcessor().getFrom();
        val to = this.getData().getMovementProcessor().getTo();

        if(!MiscUtils.canDoCombat(combatOnly, getData())) return;

        val yawChange = Math.abs(from.getYaw() - to.getYaw());
        val pitchChange = Math.abs(from.getPitch() - to.getPitch());

        pitchDeque.add(pitchChange);

        val pitchAverage = pitchDeque.stream().mapToDouble(Float::doubleValue).average().orElse(0.0F);
        val pitchRatio = pitchAverage / pitchChange;

        if (pitchRatio > 100.F && yawChange > 2.f) {
            if (++vl > vlMax) {
                this.flag("P: " + pitchRatio, true, true);
            }
        } else {
            vl = 0;
        }

        if (pitchDeque.size() == 20) {
            pitchDeque.clear();
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
