package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.Color;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.TagsBuilder;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Autoclicker (E)", description = "Combined autoclicker check.",
        checkType = CheckType.AUTOCLICKER, devStage = DevStage.BETA)
public class AutoclickerE extends Check {

    private float buffer;
    @Packet
    public void onClick(WrappedInArmAnimationPacket packet) {
        if(data.playerInfo.breakingBlock
                || data.playerInfo.lookingAtBlock
                || data.clickProcessor.isNotReady()
                || data.playerInfo.lastBrokenBlock.isNotPassed(5)
                || data.playerInfo.lastBlockDigPacket.isNotPassed(1)
                || data.playerInfo.lastBlockPlacePacket.isNotPassed(1))
            return;

        double skewness = Math.abs(data.clickProcessor.getSkewness());

        TagsBuilder tags = new TagsBuilder();

        if(data.clickProcessor.getOutliers() == 0
                && data.clickProcessor.getMean() < 2.5) {
            tags.addTag("outliers");
            buffer++;
        }

        if(data.clickProcessor.getStd() < 0.5 && data.clickProcessor.getOutliers() <= 1) {
            buffer+= 0.75;
            tags.addTag("deviation");
        }

        if(data.clickProcessor.getKurtosis() < 0
                && (data.clickProcessor.getOutliers() <= 1 || skewness < 0.1)
                && data.clickProcessor.getMean() < 2.5) {
            buffer+= 0.45f;
            tags.addTag("kurtosis");
        }

        if(skewness < 0.15 && data.clickProcessor.getMean() < 2.5) {
            buffer+= 0.45f;
            tags.addTag("skew");
        }

        if(tags.getSize() > 0) {
            if(buffer > 30) {
                vl++;
                buffer = 28;
                flag("tags=%s k=%.2f o=%s sk=%.2f a=%.1f", tags.build(), data.clickProcessor.getKurtosis(),
                        data.clickProcessor.getOutliers(), data.clickProcessor.getSkewness(),
                        data.clickProcessor.getMean());
            }
        } else if(buffer > 0) buffer-= 0.75f;

        debug("tags=%s mean=%.2f skew=%.2f kurt=%.2f std=%.3f outliers=%s buffer=%.1f",
                (tags.getSize() > 0 ? Color.Green : "") +tags.build() + Color.Gray, data.clickProcessor.getMean(),
                data.clickProcessor.getSkewness(), data.clickProcessor.getKurtosis(),
                data.clickProcessor.getStd(), data.clickProcessor.getOutliers(), buffer);
    }
}
