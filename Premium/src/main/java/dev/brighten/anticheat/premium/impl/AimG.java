package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutPositionPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutTransaction;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import cc.funkemunky.api.utils.objects.evicting.EvictingMap;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;
import org.bukkit.Location;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@CheckInfo(name = "Aim (G)", description = "Spooky rotation check",
        checkType = CheckType.AIM, punishVL = 30, developer = true, planVersion = KauriVersion.ARA)
public class AimG extends Check {

    private Map<Short, Float> ids = new EvictingMap<>(20);
    private int yesTick = -1, buffer;
    private boolean waited;
    private float currentFloat;
    @Packet
    public void onFlying(WrappedInTransactionPacket packet, int current) {
        if(ids.containsKey(packet.getAction())) {
            waited = false;
            yesTick = current;
            currentFloat = ids.get(packet.getAction());

            //debug("p=%s", data.playerInfo.to.pitch);
        }
    }

    private float teleportPitch = -1f;
    @Packet
    public void oonFlying(WrappedInFlyingPacket packet, int current) {
        if(yesTick != -1) {
            if(yesTick + 1 > current) {
                //return;
            }
            if(current != yesTick) {
                //debug("c=%s yt=%s", current, yesTick);
                yesTick = -1;
                currentFloat = 0;
                return;
            }

            teleportPitch = packet.getPitch();

            yesTick = -1;
            debug("teleport=%s cf=%s", current, currentFloat);
            currentFloat = 0;
        } else if(packet.isLook() && teleportPitch != -1f) {
            final double my = ((packet.getPitch() - teleportPitch) / data.moveProcessor.pitchMode)
                    % (Math.abs(data.playerInfo.lDeltaPitch) / data.moveProcessor.pitchMode);

            final double deltaY = Math.abs(Math.floor(my) - my);
            debug("dY=%s pgcd=%s", deltaY, data.playerInfo.pitchGCD);
            teleportPitch = -1f;
        }
    }

    @Packet
    public void onTrans(WrappedOutTransaction packet) {
        if(Kauri.INSTANCE.keepaliveProcessor.tick % 10 == 0) {
            float random = ThreadLocalRandom.current().nextInt(100, 800) / 100000f
                    * (ThreadLocalRandom.current().nextBoolean() ? 1 : -1);
            WrappedOutPositionPacket pos = new WrappedOutPositionPacket(new Location(
                    packet.getPlayer().getWorld(), 0, 0, 0, 0, random),
                    WrappedOutPositionPacket.EnumPlayerTeleportFlags.values());

            TinyProtocolHandler.sendPacket(packet.getPlayer(), pos);

            ids.put(packet.getAction(), random);
        }
    }
}