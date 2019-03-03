package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import java.util.*;

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.LOOK, Packet.Client.LEGACY_LOOK, Packet.Client.LEGACY_POSITION_LOOK})
public class AimF extends Check {

    public AimF(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);
    }

    private List<Float> smoothingList = new ArrayList<>();
    private float lastSmoothing, smoothing;
    private Verbose verbose = new Verbose();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val target = getData().getTarget();
        if(target == null) return;
        val move = getData().getMovementProcessor();
        val angledYaw = MathUtils.getRotations(move.getTo().toLocation(getData().getPlayer().getWorld()), target.getLocation())[0];
        val yawDelta = move.getTo().getYaw() - move.getFrom().getYaw();
        lastSmoothing = smoothing;
        smoothing = angledYaw / yawDelta + MathUtils.yawTo180F(move.getFrom().getYaw());

        val delta = MathUtils.getDelta(lastSmoothing, smoothing);

        if(delta < 5 && verbose.flag(15, 250L)) {
            debug(Color.Green + "Flagged: " + delta);
        }

        debug("VERBOSE: " + verbose.getVerbose() + " SMOOTHING: " + smoothing + "  YAWDELTA: " + yawDelta + " ANGLE: " + angledYaw + " DELTA: " + delta);
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
