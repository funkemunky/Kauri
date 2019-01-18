package cc.funkemunky.anticheat.impl.checks.player;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInAbilitiesPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutAbilitiesPacket;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.ABILITIES, Packet.Server.ABILITIES})
public class BadPacketsA extends Check {
    @Setting
    private int vlMax = 3;
    private boolean sent;
    private int vl;
    public BadPacketsA(String name, CancelType cancelType, int maxVL) {
        super(name, cancelType, maxVL);
    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (packetType.equalsIgnoreCase(Packet.Server.ABILITIES)) {
            WrappedOutAbilitiesPacket abilities = new WrappedOutAbilitiesPacket(packet, getData().getPlayer());

            if (abilities.isAllowedFlight()) {
                sent = true;
            }
        } else {
            WrappedInAbilitiesPacket abilities = new WrappedInAbilitiesPacket(packet, getData().getPlayer());

            if (abilities.isAllowedFlight() && !sent) {
                if (vl++ > vlMax) {
                    flag("fake news abilities packet", true, true);
                }
            } else {
                vl -= vl > 0 ? 1 : 0;
            }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
