package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (G)", description = "Checks for outliers in clicks. (FFX Autoclicker 6).",
        checkType = CheckType.AUTOCLICKER, vlToFlag = 40, punishVL = 80)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerG extends Check {

    private int clicks, outliers, flyingCount;
    private boolean release;
    private double buffer;

    @Packet
    public void check(WrappedInFlyingPacket packet) {
        ++this.flyingCount;
    }

    @Packet
    public void check(WrappedInBlockDigPacket packet) {
        if (packet.getAction() == WrappedInBlockDigPacket.EnumPlayerDigType.RELEASE_USE_ITEM) {
            this.release = true;
        }
    }

    @Packet
    public void check(WrappedInArmAnimationPacket packet) {
        if (!data.playerInfo.breakingBlock
                && !data.playerInfo.lookingAtBlock
                && data.playerInfo.lastBlockPlace.hasPassed(4)) {
            if (this.flyingCount < 10) {
                if (this.release) {
                    this.release = false;
                    this.flyingCount = 0;
                    return;
                }
                if (this.flyingCount > 3) {
                    ++this.outliers;
                } else if (this.flyingCount == 0) {
                    return;
                }
                if (++this.clicks == 40) {
                    if (this.outliers == 0) {
                        if (++buffer >= 7.0) {
                            if(data.clickProcessor.getStd() > 30 && data.clickProcessor.getKurtosis() < 6) vl++;
                            flag("o=%v buffer=%v.1 std=%v.2", outliers, buffer, data.clickProcessor.getStd());
                        }
                    } else buffer-= buffer > 0 ? 1.5 : 0;
                    debug("outliers=" + outliers + " vl=" + vl);
                    this.outliers = 0;
                    this.clicks = 0;
                }
            }
            this.flyingCount = 0;
            vl-= vl > 0 ? 0.002 : 0;
        }
    }
}
