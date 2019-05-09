package cc.funkemunky.anticheat.impl.checks.movement.speed;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.PlayerUtils;
import cc.funkemunky.api.utils.ReflectionsUtil;
import lombok.val;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;


@Packets(packets = {Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
@Init
@CheckInfo(name = "Speed (Type C)", description = "Checks the in-air and on-ground deceleration of the client. More accurate.", type = CheckType.SPEED, maxVL = 125)
public class SpeedC extends Check {

    private boolean lastGround, lastLastGround;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        val ground = move.isServerOnGround();
        val sprinting = getData().getActionProcessor().isSprinting();
        val accel = move.getDeltaXZ() - move.getLastDeltaXZ();
        val delta = accel -  (sprinting ? 0.026f : 0.02f);

        if(delta > 1E-6 && !lastGround & !lastLastGround) {
            flag("accel=" + accel + " delta=" + delta, true, true);
        }

        lastLastGround = lastGround;
        lastGround = ground;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}

