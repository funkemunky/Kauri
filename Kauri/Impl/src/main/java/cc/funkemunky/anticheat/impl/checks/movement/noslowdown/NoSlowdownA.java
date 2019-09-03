package cc.funkemunky.anticheat.impl.checks.movement.noslowdown;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInHeldItemSlotPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutHeldItemSlot;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Init
@CheckInfo(name = "NoSlowdown (Type A)", description = "Looks for players using items but not slowing down.")
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.HELD_ITEM_SLOT})
public class NoSlowdownA extends Check {

    private Verbose verbose = new Verbose();
    private long lastTimeStamp;
    private int slot;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equals(Packet.Client.HELD_ITEM_SLOT)) {
            WrappedInHeldItemSlotPacket slot = new WrappedInHeldItemSlotPacket(packet, getData().getPlayer());

            this.slot = slot.getSlot();
            debug("slot=" + this.slot);
        } else {
            val move = getData().getMovementProcessor();
            val action = getData().getActionProcessor();
            val baseSpeed = move.getBaseSpeed() - 0.02f;

            if (timeStamp - lastTimeStamp < 5 || getData().isGeneralCancel() || !getData().takingVelocity(15)) return;

            if (action.isUsingItem() && move.getDeltaXZ() > baseSpeed) {
                if (verbose.flag(20, 500L)) {
                    flag(MathUtils.round(move.getDeltaXZ(), 3) + ">- " + baseSpeed, true, false, verbose.getVerbose() > 40 ? AlertTier.LIKELY : AlertTier.POSSIBLE);
                } else if(verbose.getVerbose() > 10 && verbose.getVerbose() < 13) {
                    WrappedOutHeldItemSlot slot = new WrappedOutHeldItemSlot(this.slot);

                    TinyProtocolHandler.sendPacket(getData().getPlayer(), slot.getObject());
                    debug("sent packet");
                }
                debug(verbose.getVerbose() + ": " + move.getDeltaXZ() + ">-" + baseSpeed);
            } else verbose.deduct();
            lastTimeStamp = timeStamp;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
