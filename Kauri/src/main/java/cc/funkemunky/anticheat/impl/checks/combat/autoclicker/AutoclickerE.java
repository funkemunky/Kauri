package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.FLYING,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK,
        Packet.Client.ARM_ANIMATION,
        Packet.Client.BLOCK_DIG})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Autoclicker (Type E)", description = "An unreasonable amount of CPS while breaking a block.", type = CheckType.AUTOCLICKER, cancelType = CancelType.INTERACT, maxVL = 20)
public class AutoclickerE extends Check {

    private boolean sent;
    private int vl;
    private long lastArm, lastRange;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (MiscUtils.shouldReturnArmAnimation(getData())) return;
        if (packet instanceof WrappedInBlockDigPacket) {
            val digPacket = (WrappedInBlockDigPacket) packet;

            val digType = digPacket.getAction();

            switch (digType) {
                case START_DESTROY_BLOCK: {
                    sent = true;
                    break;
                }
                case ABORT_DESTROY_BLOCK: {
                    if (sent && lastRange > 10L) {
                        if (++vl >= 10) {
                            this.flag("V: " + vl, false, true, AlertTier.HIGH);
                        }
                    }
                    break;
                }
            }
        } else if (Packet.isPositionLook(packetType) || Packet.isPosition(packetType) || Packet.isLook(packetType) || packet instanceof WrappedInFlyingPacket) {
            sent = false;
        } else if (packetType.equals(Packet.Client.ARM_ANIMATION)) {
            val now = System.currentTimeMillis();
            val delay = now - this.lastArm;

            this.lastArm = now;
            this.lastRange = delay;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
