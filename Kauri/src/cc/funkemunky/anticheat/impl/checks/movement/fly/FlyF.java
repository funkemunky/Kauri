package cc.funkemunky.anticheat.impl.checks.movement.fly;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_POSITION_LOOK})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Fly (Type F)", description = "A more advanced acceleration check which predicts what it should be.", type = CheckType.FLY, cancelType = CancelType.MOTION, executable = false, developer = true)
public class FlyF extends Check {
    public FlyF() {

    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if (MiscUtils.cancelForFlight(getData(), 10, true)) return;
        val acceleration = Math.abs(move.getClientYAcceleration());
        val jumpEffect = getData().getPlayer().getActivePotionEffects().stream().filter(potion -> potion.getType().getName().contains("JUMP")).findFirst().orElse(null);
        val jumpMagnitude = jumpEffect == null ? 0 : jumpEffect.getAmplifier() + 1;
        val maxJump = 0.42f + (jumpMagnitude * 0.11);
        val max = move.getAirTicks() < 3 || move.getDistanceToGround() < 0.5 ? maxJump : 0.788f;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
