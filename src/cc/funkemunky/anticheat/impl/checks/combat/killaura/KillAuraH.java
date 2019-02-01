package cc.funkemunky.anticheat.impl.checks.combat.killaura;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import lombok.val;
import net.minecraft.server.v1_8_R3.PacketPlayInArmAnimation;
import org.bukkit.event.Event;

public class KillAuraH extends Check {

    public KillAuraH(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    private boolean swing;
    private int vl;

    @Override
    public Object onPacket(Object packet, String packetType, long timeStamp) {
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
        } else if (packet instanceof PacketPlayInArmAnimation) {
            this.swing = true;
        }
        return packet;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
