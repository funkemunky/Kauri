package cc.funkemunky.anticheat.impl.checks.movement.groundspoof;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION})
@Init
@CheckInfo(name = "GroundSpoof (Type B)", description = "Looks for any false in-air anomalies in the onGround boolean.", type = CheckType.MOVEMENT, developer = true, executable = false)
public class GroundSpoofB extends Check {

    @Setting(name = "threshold.vl.max")
    private int vlMax = 10;

    @Setting(name = "threshold.vl.resetTime")
    private long resetTime = 350L;

    @Setting(name = "threshold.distanceFromGround")
    private double distanceFG = 1.5;

    @Setting(name = "threshold.ticksInAir")
    private int ticksInAir = 4;

    private Verbose verbose = new Verbose();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(getData().isGeneralCancel()) return;

        if(move.getDistanceToGround() > distanceFG && move.getAirTicks() > ticksInAir && !move.isServerOnGround() && move.isClientOnGround()) {
            if(verbose.flag(vlMax, resetTime)) {
                flag(move.getDistanceToGround() + ">-" + distanceFG + ";" + move.getAirTicks() + ">-" + ticksInAir, true, true);
            }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
