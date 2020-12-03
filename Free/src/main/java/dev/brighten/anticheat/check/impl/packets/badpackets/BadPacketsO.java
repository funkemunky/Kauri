package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.check.api.Setting;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "BadPackets (O)", description = "Designed to patch disablers for Kauri.",
        checkType = CheckType.BADPACKETS, punishVL = 50, vlToFlag = 4)
public class BadPacketsO extends Check {

    @Setting(name  = "kickPlayer")
    private static boolean kickPlayer = true;

    private int flying;
    private int lastId = Integer.MAX_VALUE;
    private int buffer;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        val response = Kauri.INSTANCE.keepaliveProcessor.getResponse(data);
       if(!response.isPresent()
               || Kauri.INSTANCE.keepaliveProcessor.tick - response.get().start > 60) {
          if(++buffer > 15) {
              vl++;
              flag("flying=%v", flying);
          }
       } else buffer = 0;
    }

    @Packet
    public void onTrans(WrappedInTransactionPacket packet) {
        val optional = Kauri.INSTANCE.keepaliveProcessor.getKeepById(packet.getAction());

        if(!optional.isPresent()) return;

        int delta = optional.get().start - lastId;
        if(delta > 3) {
            vl++;
            flag("t=1 d=%v", delta);
        }
        if(Kauri.INSTANCE.keepaliveProcessor.tick - optional.get().start > 60) {
            vl++;
            flag("t=2");
        }
        flying = 0;
        lastId = optional.get().start;
    }
}
