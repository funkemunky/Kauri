package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Aim (I)", description = "")
public class AimI extends Check {

    private int movements, lastMovements, total, invalid;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        ++movements;
    }

    @Packet
    public void onUseEntity(WrappedInUseEntityPacket packet) {
        if (packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) {
            final boolean proper = movements < 4 && lastMovements < 4;

            if (proper) {
                final boolean flag = movements == lastMovements;

                if (flag) {
                    ++invalid;
                }

                if (++total == 25) {

                    if (invalid > 20) {
                        vl++;
                        flag("");
                    }

                    debug("invalid=%v", invalid);

                    total = invalid = 0;
                }
            }

            lastMovements = movements;
            movements = 0;
        }
    }

}
