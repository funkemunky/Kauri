package cc.funkemunky.anticheat.impl.checks.player.badpackets;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInAbilitiesPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutAbilitiesPacket;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.ABILITIES,
        Packet.Server.ABILITIES,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "BadPackets (Type A)", description = "Prevents the client from spoofing the ability to fly.",
        type = CheckType.BADPACKETS, maxVL = 40)
public class BadPacketsA extends Check {
    private boolean serverSent, lastAllowedFlight;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (packetType.equalsIgnoreCase(Packet.Server.ABILITIES)) {
            WrappedOutAbilitiesPacket abilities = new WrappedOutAbilitiesPacket(packet, getData().getPlayer());

            if (abilities.isAllowedFlight()) {
                serverSent = true;
            } else serverSent = false;
        } else if (packetType.equalsIgnoreCase(Packet.Client.ABILITIES)) {
            WrappedInAbilitiesPacket abilities = new WrappedInAbilitiesPacket(packet, getData().getPlayer());

            if (abilities.isAllowedFlight() != lastAllowedFlight
                    && getData().getLastLogin().hasPassed(40)
                    && abilities.isAllowedFlight()
                    && (timeStamp - getData().getLastRespawn()) > 100 + getData().getTransPing()
                    && !getData().getMovementProcessor().isServerPos()
                    && !getData().isCreativeMode()
                    && !serverSent) {
                flag("fake news abilities packet", true, true, AlertTier.HIGH);
                getData().getPlayer().setAllowFlight(false);
            }

            lastAllowedFlight = abilities.isAllowedFlight();
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
