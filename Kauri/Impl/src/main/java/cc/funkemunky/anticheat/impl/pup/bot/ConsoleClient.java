package cc.funkemunky.anticheat.impl.pup.bot;

import cc.funkemunky.anticheat.api.pup.AntiPUP;
import cc.funkemunky.anticheat.api.pup.PuPType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;

@Packets(packets = {Packet.Client.CUSTOM_PAYLOAD})
public class ConsoleClient extends AntiPUP {

    @Setting(name = "kickMessage")
    private String message = "&cConsole clients are not allowed.\n&7&oNot a console client? &fDon't freeze your game!";

    public ConsoleClient(String name, PuPType type, boolean enabled) {
        super(name, type, enabled);
    }

    private int vl = 0;

    @Override
    public boolean onPacket(Object packet, String packetType, long timeStamp) {
        return false;
    }
}
