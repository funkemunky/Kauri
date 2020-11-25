package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.Color;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.TagsBuilder;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (E)", description = "Combined autoclicker check.",
        checkType = CheckType.AUTOCLICKER, developer = true, planVersion = KauriVersion.ARA)
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
        } else if(data.clickProcessor.getStd() < 0.5 && data.clickProcessor.getOutliers() <= 1) {
            buffer+= 0.75;
            tags.addTag("deviation");
        } else if(data.clickProcessor.getKurtosis() < 0
                && (data.clickProcessor.getOutliers() <= 1 || skewness < 0.1)
                && data.clickProcessor.getMean() < 2.5) {
            buffer+= 0.75;
            tags.addTag("kurtosis");
        } else if(skewness < 0.15 && data.clickProcessor.getMean() < 2.5) {
            buffer+= 0.5f;
            tags.addTag("skew");
        }

        if(tags.getSize() > 0) {
            if(buffer > 20) {
                vl++;
                buffer = 15;
                flag("tags=%v", tags.build());
            }
        } else if(buffer > 0) buffer-= 0.5f;

        debug("tags=%v mean=%v.2 skew=%v.2 kurt=%v.2 std=%v.3 outliers=%v buffer=%v.1",
                (tags.getSize() > 0 ? Color.Green : "") +tags.build() + Color.Gray, data.clickProcessor.getMean(),
                data.clickProcessor.getSkewness(), data.clickProcessor.getKurtosis(),
                data.clickProcessor.getStd(), data.clickProcessor.getOutliers(), buffer);
    }
}
