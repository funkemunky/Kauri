package cc.funkemunky.anticheat.impl.checks.combat.killaura;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.MathUtils;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.USE_ENTITY,
        Packet.Client.FLYING,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
public class KillauraA extends Check {

    private long lastFlying = 0;
    private int verbose;
    private boolean dontFlag;

    public KillauraA(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);
    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (packetType.equals(Packet.Client.USE_ENTITY)) {
            WrappedInUseEntityPacket use = new WrappedInUseEntityPacket(packet, getData().getPlayer());
            /*Checks the time difference between a flying packet and a use packet. If legit, it should normally be around 50ms.
            KillauraA modules tend to be made using a motion event, and client developers usually forget to make sure that the motion
            and the attack packets are being sent in separate ticks */
            long elapsed = MathUtils.elapsed(lastFlying);
            if (elapsed < 20 && !dontFlag) {
                if (verbose++ > 12) {
                    flag("t: post; " + elapsed + "<-10", true, true);
                }
            } else {
                verbose = 0;
            }

        } else {
            if (System.currentTimeMillis() - lastFlying < 5) {
                dontFlag = true;
            } else {
                dontFlag = false;
            }
            lastFlying = System.currentTimeMillis();
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
