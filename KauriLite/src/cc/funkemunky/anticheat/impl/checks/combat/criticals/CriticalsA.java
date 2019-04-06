package cc.funkemunky.anticheat.impl.checks.combat.criticals;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "Criticals (Type A)", developer = true, executable = false, type = CheckType.COMBAT, cancelType = CancelType.COMBAT)
@Init
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_POSITION_LOOK})
public class CriticalsA extends Check {

    private Verbose verbose = new Verbose();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(getData().getLastAttack().hasNotPassed(0)) {
            if(move.isServerOnGround()
                    && move.getDeltaY() != 0
                    && Math.abs(move.getDeltaY()) < 0.1
                    && move.getHalfBlockTicks() == 0
                    && move.getLiquidTicks() == 0
                    && move.getWebTicks() == 0
                    && verbose.flag(2, 750L)) {
                flag("y: " + move.getDeltaY(), true, true);
            }
            debug("DELTAY: " + move.getDeltaY() + " SGROUND: " + move.isServerOnGround() + " CGROUND: " + move.isClientOnGround());
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
