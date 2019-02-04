package cc.funkemunky.anticheat.impl.checks.movement;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.event.Event;

import java.util.*;

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_POSITION_LOOK})
public class FlyC extends Check {
    public FlyC(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    private float lastMotion;

    @Override
    public Object onPacket(Object packet, String packetType, long timeStamp) {
        if(!getData().getPlayer().getAllowFlight() && getData().getPlayer().getVehicle() == null && getData().getBoundingBox().grow(2F, 2F, 2F).getCollidingBlocks(this.getData().getPlayer()).size() == 0) {
            val move = getData().getMovementProcessor();
            val motion = move.getDeltaY();

            if(move.getClimbTicks() == 0 && getData().getLastBlockPlace().hasPassed(8) && move.getHalfBlockTicks() == 0 && move.getLiquidTicks() == 0 && getData().getVelocityProcessor().getLastVelocity().hasPassed(10) && move.getWebTicks() == 0 && move.getAirTicks() > 4) {
                if(motion > lastMotion) {
                    flag(motion + ">-" + lastMotion, true, true);
                }
            }
            lastMotion = motion;
        }
        return packet;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
