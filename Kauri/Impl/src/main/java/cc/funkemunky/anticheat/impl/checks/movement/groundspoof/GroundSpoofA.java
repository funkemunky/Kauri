package cc.funkemunky.anticheat.impl.checks.movement.groundspoof;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "GroundSpoof (Type A)", description = "Spoofing as though the player has never touched the ground.", type = CheckType.MOVEMENT, cancelType = CancelType.MOTION)
@Init
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
public class GroundSpoofA extends Check {

    private Verbose verbose = new Verbose();

    @Setting(name = "threshold.vl.max")
    private static int vlThreshold = 6;

    @Setting(name = "threshold.vl.add")
    private static int addVl = 1;

    @Setting(name = "threshold.vl.deduct")
    private static double deductVl = 0.5;


    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(move.isCancelFlight()
                || move.isBlocksOnTop())
            return;

        if(move.isServerOnGround() != move.isClientOnGround()) {
            if(verbose.flag(vlThreshold, 1200L, addVl)) {
                flag("client=" + move.isClientOnGround() + " server=" + move.isServerOnGround(),
                        true,
                        true,
                        verbose.getVerbose() > 20 ? AlertTier.HIGH : AlertTier.LIKELY);
            }
        } else verbose.deduct(deductVl);

        debug("client=" + move.isClientOnGround() + " server=" + move.isServerOnGround() + " vl="
                + verbose.getVerbose() + " dy=" + move.getDeltaY() + " y=" + move.getTo().getY());
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
