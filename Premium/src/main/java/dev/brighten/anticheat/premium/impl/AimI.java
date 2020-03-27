package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (I)", developer = true, punishVL = 20, checkType = CheckType.AIM,
        description = "Checks for minecraft rotation exploits by clients. " +
                "By FlyCode with help from Itz_Lucky and funkemunky.")
public class AimI extends Check {

    private boolean looked;
    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if (packet.isLook()
                && data.playerInfo.lastAttack.hasNotPassed(20)) {

            double sensitivity = data.moveProcessor.sensXPercent;

            double pitch = data.playerInfo.to.pitch, yaw = data.playerInfo.to.yaw;

            double o = modulusRotation(sensitivity, pitch);

            //Nasty
            int l = String.valueOf(o).length();

            if (Math.abs(pitch) != 90.0
                    && ((sensitivity > 99 && (o > 0.0 && l > 0 && l < 8)) || o == 0.0f)) {
                vl++;
                flag("pitch=%v.3 sens=%v o=%v.4 l=%v", pitch, sensitivity, o, l);
            }
            debug("pitch=%v.3 sens=%v o=%v.4 l=%v", pitch, sensitivity, o, l);
        }
    }

    public static boolean isPrime(int n) {
        if (n <= 1) {
            return false;
        }
        for (int i = 2; i < Math.sqrt(n); i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    private double modulusRotation(double s, double pitch) {
        //Client calulations
        float f = (float) (s * 0.6F + 0.2F);
        float f2 = f * f * f * 1.2F;

        return (pitch % f2);
    }
}
