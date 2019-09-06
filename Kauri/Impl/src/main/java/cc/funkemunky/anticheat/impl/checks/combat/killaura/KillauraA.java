package cc.funkemunky.anticheat.impl.checks.combat.killaura;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.TickTimer;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.USE_ENTITY,
        Packet.Client.FLYING,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Killaura (Type A)", description = "Checks the intervalTime between certain packets and attacks.",
        type = CheckType.KILLAURA, cancelType = CancelType.COMBAT, maxVL = 150)
public class KillauraA extends Check {

    private long lastFlying = 0;
    private int verbose;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (packetType.equals(Packet.Client.USE_ENTITY)) {
            WrappedInUseEntityPacket use = new WrappedInUseEntityPacket(packet, getData().getPlayer());

            if (!use.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) return;

            /*Checks the intervalTime difference between a flying packet and a use packet.
            If legit, it should normally be around 50ms.
            KillauraA modules tend to be made using a motion event, and client developers usually forget to
            make sure that the motion and the attack packets are being sent in separate ticks */
            long elapsed = timeStamp - lastFlying;
            if (elapsed < 35 && !getData().isLagging()) {
                if (verbose++ > 12) {
                    flag("t: post; " + elapsed + "<-35", true, true, AlertTier.POSSIBLE);
                }
            } else if(getData().isLagging()) {
                verbose-= verbose > 0 ? 1 : 0;
            } else verbose = 0;

            debug("elapsed=" + elapsed + " verbose=" + verbose);

        } else lastFlying = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
