package cc.funkemunky.anticheat.impl.checks.player.badpackets;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInAbilitiesPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutAbilitiesPacket;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.ABILITIES,
        Packet.Server.ABILITIES,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LEGACY_POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "BadPackets (Type A)", description = "Prevents the client from spoofing the ability to fly.", type = CheckType.BADPACKETS, maxVL = 40)
public class BadPacketsA extends Check {
    @Setting(name = "vlMax")
    private int vlMax = 3;

    private boolean serverSent, clientSent, lastCreative;
    private int vl;

    public BadPacketsA() {

    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (packetType.equalsIgnoreCase(Packet.Server.ABILITIES)) {
            WrappedOutAbilitiesPacket abilities = new WrappedOutAbilitiesPacket(packet, getData().getPlayer());

            if (abilities.isAllowedFlight() && !getData().isCreativeMode()) {
                serverSent = true;
            }
        } else if (packetType.equalsIgnoreCase(Packet.Client.ABILITIES)) {
            WrappedInAbilitiesPacket abilities = new WrappedInAbilitiesPacket(packet, getData().getPlayer());

            if (abilities.isAllowedFlight() && !getData().isCreativeMode() && !serverSent) {
                if (vl++ > vlMax) {
                    flag("fake news abilities packet", true, true);
                }
            } else {
                vl -= vl > 0 ? 1 : 0;
            }

            clientSent = true;

            if (!abilities.isAllowedFlight()) {
                clientSent = serverSent = false;
            }

            if (abilities.isCreativeMode() != lastCreative) {
                clientSent = serverSent = false;
            }

            if ((lastCreative = abilities.isCreativeMode())) {
                clientSent = serverSent = false;
            }

        } else {
            if (!serverSent && !getData().isCreativeMode() && clientSent) {
                flag("fake news abilities packet", true, true);
            }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
