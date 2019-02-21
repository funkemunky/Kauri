package cc.funkemunky.anticheat.impl.checks.combat.killaura;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.USE_ENTITY,
        Packet.Client.ARM_ANIMATION,
        Packet.Client.FLYING,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
public class KillauraH extends Check {

    public KillauraH(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    private boolean swing;
    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (packet instanceof WrappedInUseEntityPacket) {
            val useEntity = (WrappedInUseEntityPacket)packet;

            if (useEntity.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) {
                if (!swing) {
                    if (++vl >= 4) {
                        this.flag("FALSE", false, true);
                    }
                } else {
                    vl = 0;
                }
            }
        } else if (packet instanceof WrappedInFlyingPacket) {
            this.swing = false;
        } else if (packet instanceof WrappedInArmAnimationPacket) {
            this.swing = true;
        }
        return;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
