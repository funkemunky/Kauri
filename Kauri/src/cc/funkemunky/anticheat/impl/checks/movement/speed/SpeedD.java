package cc.funkemunky.anticheat.impl.checks.movement.speed;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.ReflectionsUtil;
import lombok.val;
import org.bukkit.block.Block;
import org.bukkit.event.Event;


@Packets(packets = {Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Speed (Type D)", description = "Checks the in-air and on-ground deceleration of the client. Less accurate.", type = CheckType.SPEED, maxVL = 125, executable = false, developer = true)
public class SpeedD extends Check {
    public SpeedD() {

    }

    private float lastMotion;
    private long lastTimeStamp;
    private Verbose verbose = new Verbose();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        val to = move.getTo();
        val from = move.getFrom();
        Block below = BlockUtils.getBlock(to.clone().toLocation(getData().getPlayer().getWorld()).subtract(0, 1, 0));

        val deltaXZ = (float) cc.funkemunky.anticheat.api.utils.MiscUtils.hypot(to.getX() - from.getX(), to.getZ() - from.getZ());
        val friction = !move.isServerOnGround() || !below.getType().isSolid() ? 0.68f : ReflectionsUtil.getFriction(below);
        val resistance = move.isServerOnGround() ? friction * 0.91f : 0.91f;
        val predicted = lastMotion * resistance;
        val delta = deltaXZ - predicted;

        val max = move.isServerOnGround() || move.getAirTicks() < 3 ? 0.24 : 0.03;

        if (getData().getLastBlockPlace().hasPassed(8) && getData().getVelocityProcessor().getLastVelocity().hasPassed(6) && getData().getLastServerPos().hasPassed(1) && !getData().isGeneralCancel() && timeStamp > lastTimeStamp + 5 && delta > max) {
            if ((delta > max + 0.4) || verbose.flag(3, 400L)) {
                flag(delta + ">-" + max + ";" + move.isServerOnGround(), true, true);
            }
        }

        debug("DIFFERENCE: " + delta + " GROUND: " + move.isServerOnGround());

        lastMotion = deltaXZ;
        lastTimeStamp = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}

