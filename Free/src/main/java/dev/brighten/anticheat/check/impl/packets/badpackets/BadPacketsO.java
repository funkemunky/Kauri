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
               || ++flying > 3) {
          if(++buffer > 15) {
              vl++;
              flag("t=1 flying=%v", flying);
          }
       } else buffer = 0;
    }

    @Packet
    public void onTrans(WrappedInTransactionPacket packet, long current) {
        val optional = Kauri.INSTANCE.keepaliveProcessor.getKeepById(packet.getAction());

        if(!optional.isPresent() || data.lagInfo.lastPacketDrop.isNotPassed(7)) return;

        int deltaShot = Kauri.INSTANCE.keepaliveProcessor.tick - optional.get().start;
        if(deltaShot > 60) {
            vl++;
            flag("t=2 tick=%v", deltaShot);
        }
        flying = 0;
        lastId = optional.get().start;
    }
}
