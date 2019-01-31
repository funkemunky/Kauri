package cc.funkemunky.anticheat.impl.checks.movement;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import org.bukkit.event.Event;

import java.util.*;

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_POSITION_LOOK})
public class FlyC extends Check {
    public FlyC(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    private Set<Float> set = new HashSet<>();
    private int ticks;

    @Override
    public Object onPacket(Object packet, String packetType, long timeStamp) {
        if(!getData().getMovementProcessor().isServerOnGround() && getData().getMovementProcessor().getDistanceToGround() > 1.2 && getData().getMovementProcessor().getClimbTicks() == 0 && getData().getMovementProcessor().getLiquidTicks() == 0 && !getData().isGeneralCancel() && !getData().getMovementProcessor().isOnHalfBlock()) {
            if(ticks++ == 10) {
                debug("SIZE: " + set.size());

                if(set.size() < 7) {
                    flag(set.size() + "<-7", true, true);
                }
                set.clear();
                ticks = 0;
            } else {
                set.add(getData().getMovementProcessor().getDeltaY());
            }
        }
        return packet;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
