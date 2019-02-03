package cc.funkemunky.anticheat.impl.checks.movement;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.BukkitEvents;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.BlockUtils;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;

@BukkitEvents(events = {PlayerMoveEvent.class})
public class FlyD extends Check {

    public FlyD(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    private double lastYChange;
    private int vl;

    @Override
    public Object onPacket(Object packet, String packetType, long timeStamp) {
        return packet;
    }

    @Override
    public void onBukkitEvent(Event event) {
        if (event instanceof PlayerMoveEvent) {
            val moveEvent = (PlayerMoveEvent)event;

            val from = moveEvent.getFrom();
            val to = moveEvent.getTo();

            val move = this.getData().getMovementProcessor();

            val yChange = to.getY() - from.getY();
            val predictedY = (lastYChange - 0.08D) * 0.9800000190734863D;

            if (move.getLiquidTicks() > 0 || !moveEvent.getPlayer().getAllowFlight() && moveEvent.getPlayer().getVehicle() != null || move.getClimbTicks() > 0 || move.isOnSlimeBefore() || move.getWebTicks() > 0) {
                return;
            }

            if (!move.isServerOnGround() && getData().getBoundingBox().grow(1.5F, 1.5F, 1.5F).getCollidingBlocks(this.getData().getPlayer()).size() > 0) {
                val offset = Math.abs(yChange - predictedY);

                if (Math.abs(yChange - predictedY) > 0.00000000001) {
                    if ((vl++) > 3) {
                        this.flag("O -> " + offset, false, true);
                    }
                } else {
                    vl = Math.max(vl - 1, 0);
                }

                debug("VL: " + vl + "DIF: " + Math.abs(yChange - predictedY));
            }

            this.lastYChange = yChange;
        }
    }
}
