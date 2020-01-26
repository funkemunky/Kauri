package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.Verbose;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.Deque;
import java.util.LinkedList;

@CheckInfo(name = "Aim (E)", description = "Patches auras that attempt to use Minecraft code for rotations.",
        checkType = CheckType.AIM, punishVL = 25)
public class AimE extends Check {

    private Verbose verbose = new Verbose(60, 30);

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook() && data.playerInfo.lastAttack.hasNotPassed(40)) {

            val delta = Math.abs(data.playerInfo.yawGCD - data.playerInfo.pitchGCD);

            if(delta < 7000
                    && data.playerInfo.yawGCD < 1E6
                    && (data.moveProcessor.deltaX > 30 || data.moveProcessor.deltaY > 30)
                    && data.playerInfo.deltaYaw > 1 && Math.abs(data.playerInfo.deltaPitch) > 1) {
                if(verbose.flag(data.moveProcessor.deltaX > 40 && data.moveProcessor.deltaY > 40
                        ? 1 : 0.5, 10)) {
                    vl++;
                    flag("delta=" + delta);
                }
                debug(Color.Green + "flag: " + verbose.value());
            }
            debug("ygcd=%1 pgcd=%2 yaw=%3 pitch=%4",
                    data.playerInfo.yawGCD, data.playerInfo.pitchGCD,
                    data.moveProcessor.deltaX, data.moveProcessor.deltaY);
        }
    }
}
