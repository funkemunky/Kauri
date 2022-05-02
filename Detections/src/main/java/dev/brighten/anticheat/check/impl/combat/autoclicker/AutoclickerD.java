package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.Pattern;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Autoclicker (D)", description = "Checks for clicking oscillation.",
        checkType = CheckType.AUTOCLICKER, punishVL = 15, devStage = DevStage.BETA, vlToFlag = 4)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerD extends Check {

    private long totalClickTime, lastClickTime;
    private int clicks, oscillationTime, oscillationLevel, lowest, highest;
    private Pattern pattern = new Pattern();

    @Packet
    public void onClick(WrappedInArmAnimationPacket packet, long timeStamp) {
        if(!data.playerInfo.breakingBlock
                && !data.playerInfo.lookingAtBlock
                && data.playerInfo.lastBrokenBlock.isPassed(5)
                && data.playerInfo.lastBlockDigPacket.isPassed(1)
                && data.playerInfo.lastBlockPlacePacket.isPassed(1)) {
            clicks++;
            long diff = timeStamp - lastClickTime;

            if ((totalClickTime += diff) > 990) {

                if (clicks >= 3 && diff <= 200.0f) {
                    int time = oscillationTime + 1;
                    int lowest = this.lowest;
                    int highest = this.highest;

                    if (lowest == -1) {
                        lowest = clicks;
                    } else if (clicks < lowest) {
                        lowest = clicks;
                    }
                    if (highest == -1) {
                        highest = clicks;
                    } else if (clicks > highest) {
                        highest = clicks;
                    }

                    int oscillation = highest - lowest;
                    int oscLevel = oscillationLevel;
                    if (time >= 9) {
                        if (highest >= 8) {
                            if (highest >= 9 && oscillation <= 5) {
                                oscLevel += 2;
                            }
                            if (oscillation > 3 && oscLevel > 0) {
                                --oscLevel;
                            }
                        } else if (oscLevel > 0) {
                            --oscLevel;
                        }
                        time = 0;
                        highest = -1;
                        lowest = -1;
                    }
                    if (oscillation > 2) {
                        time = 0;
                        oscLevel = 0;
                        highest = -1;
                        lowest = -1;
                    }
                    if (oscLevel >= 10) {
                        vl++;
                        flag("osc=" + oscLevel);
                    }
                    debug("osc=%s level=%s high=%s low=%s", oscillation, oscLevel, highest, lowest);
                    this.lowest = lowest;
                    this.highest = highest;
                    this.oscillationTime = time;
                    this.oscillationLevel = oscLevel;

                }
                totalClickTime = 0;
                clicks = 1;
            }
            lastClickTime = timeStamp;
        }
    }
}
