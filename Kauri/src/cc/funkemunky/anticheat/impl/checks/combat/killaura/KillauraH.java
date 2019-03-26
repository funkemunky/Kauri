package cc.funkemunky.anticheat.impl.checks.combat.killaura;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
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
@CheckInfo(name = "Killaura (Type H)", description = "Detects if clients are swinging impossibly.", type = CheckType.KILLAURA, cancelType = CancelType.COMBAT, executable = false, cancellable = false, maxVL = 15, developer = true)
public class KillauraH extends Check {

    public KillauraH() {

    }

    private boolean swing;
    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (packetType.equals(Packet.Client.USE_ENTITY)) {
            val useEntity = new WrappedInUseEntityPacket(packet, getData().getPlayer());

            if (useEntity.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) {
                if (!swing) {
                    if (++vl >= 4) {
                        this.flag("FALSE", false, true);
                    }
                } else {
                    vl = 0;
                }
            }
        } else if (packetType.contains("Position") || packetType.contains("Look") || packetType.equals(Packet.Client.FLYING)) {
            this.swing = false;
        } else if (!MiscUtils.shouldReturnArmAnimation(getData())) {
            this.swing = true;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
