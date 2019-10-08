package cc.funkemunky.anticheat.impl.checks.movement.groundspoof;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "GroundSpoof (Type A)", description = "Spoofing as though the player has never touched the ground.", type = CheckType.MOVEMENT, cancelType = CancelType.MOTION)
@Init
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
public class GroundSpoofA extends Check {

    private Verbose verbose = new Verbose();

    @Setting(name = "threshold.vl.maxVl")
    private static int vlThreshold = 6;

    @Setting(name = "threshold.vl.toAdd")
    private static int addVl = 1;

    @Setting(name = "threshold.vl.deduct")
    private static double deductVl = 0.5;


    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(move.isCancelFlight()
                || getData().takingVelocity(10)
                || move.isBlocksOnTop())
            return;

        if((move.getDeltaY() == 0 && !move.isClientOnGround())
                || (move.getDeltaY() != 0 && move.isClientOnGround())) {
            if(verbose.flag(6, 650L)) {
                flag("deltaY=" + MathUtils.round(move.getDeltaY(), 4)
                        + " ground=" + move.isClientOnGround(), true, true,
                        verbose.getVerbose() > 10 ? AlertTier.HIGH : AlertTier.LIKELY);
            }
        }

        debug("client=" + move.isClientOnGround()  + " vl="
                + verbose.getVerbose() + " deltaY=" + move.getDeltaY());
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
