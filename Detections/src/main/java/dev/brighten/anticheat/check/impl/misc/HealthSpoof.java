package dev.brighten.anticheat.check.impl.misc;

import cc.funkemunky.api.com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.check.api.Setting;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.List;

@CheckInfo(name = "HealthSpoof", description = "Spoofs the health of players.", checkType = CheckType.GENERAL,
        executable = false, maxVersion = ProtocolVersion.V1_12_2)
public class HealthSpoof extends Check {

    private static boolean newer = ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_9);

    @Setting(name = "crasher")
    private static boolean crasher = false;

    @Setting(name = "healthToSpoof")
    private static double health = 1;

    @Packet
    public boolean onMetadata(WrapperPlayServerEntityMetadata packet) {
        if(packet.getEntityId() == data.getPlayer().getEntityId()) return true;

        List<EntityData> data = packet.getEntityMetadata();

        val optional =data.stream().filter(wobj -> wobj.getIndex() == 6).findFirst();

        if(optional.isPresent()) {
            EntityData object = optional.get();

            if(object.getValue() instanceof Float
                    && ((float)object.getValue()) != 1.f) {
                object.setValue(crasher ? Float.NaN : (float)health);
                return false;
            }
        }
        return true;
    }
}
