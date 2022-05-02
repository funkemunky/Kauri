package dev.brighten.anticheat.check.impl.misc;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutEntityMetadata;
import cc.funkemunky.api.tinyprotocol.packet.types.WrappedWatchableObject;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.check.api.Setting;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "HealthSpoof", description = "Spoofs the health of players.", checkType = CheckType.GENERAL,
        executable = false, maxVersion = ProtocolVersion.V1_12_2)
public class HealthSpoof extends Check {

    private static boolean newer = ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_9);

    @Setting(name = "crasher")
    private static boolean crasher = false;

    @Setting(name = "healthToSpoof")
    private static double health = 1;

    @Packet
    public boolean onMetadata(WrappedOutEntityMetadata packet) {
        if(packet.getEntityId() == data.getPlayer().getEntityId()) return true;
        val wobjects = packet.getWatchableObjects()
                .stream().map(WrappedWatchableObject::new)
                .collect(Collectors.toList());

        val optional = wobjects.stream().filter(wobj -> wobj.getDataValueId() == 6).findFirst();

        if(optional.isPresent()) {
            val object = optional.get();

            if(object.getWatchedObject() instanceof Float
                    && ((float)object.getWatchedObject()) != 1.f) {
                //debug
                List<String> strings = new ArrayList<>();
                for (int i = 0; i < wobjects.size(); i++) {
                    val o = wobjects.get(i);

                    strings.add("[" + i + ": " + o.getWatchedObject() + ":"
                            + o.getDataValueId() + ":" + o.getFirstInt() + "]");
                }

                object.setWatchedObject(crasher ? Float.NaN : (float)health);
                List<Object> objects = new ArrayList<>();

                for (int i = 0; i < wobjects.size(); i++) {
                    val wobj = wobjects.get(i);

                    if(!newer) wobj.setPacket(wobj.getFirstInt(),
                                wobj.getDataValueId(), wobj.getWatchedObject());
                    else wobj.setPacket(wobj.getDataWatcherObject(), wobj.getWatchedObject());

                    objects.add(i, wobj.getObject());
                }
                WrappedOutEntityMetadata npacket = new WrappedOutEntityMetadata(packet.getEntityId(), objects);

                TinyProtocolHandler.sendPacket(data.getPlayer(), npacket.getObject());
                return false;
            }
        }
        return true;
    }
}
