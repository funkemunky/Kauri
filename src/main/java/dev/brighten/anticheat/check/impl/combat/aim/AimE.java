package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.EntityProcessor;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.anticheat.utils.MiscUtils;
import org.bukkit.entity.LivingEntity;
import sun.java2d.pipe.SpanClipRenderer;

import javax.swing.text.html.parser.Entity;
import java.util.Comparator;

@CheckInfo(name = "Aim (Type E)")
public class AimE extends Check {

    float lastYaw, lastYD;
    @Packet
    public void flying(WrappedInFlyingPacket packet) {
        if(packet.isLook() && data.target != null) {


            float yawToEntity = MiscUtils.getYawChangeToEntity(data.getPlayer(), data.target, data.playerInfo.from, data.playerInfo.to);
            float yawDelta = MathUtils.getAngleDelta(yawToEntity, lastYaw);

            long gcd = MiscUtils.gcd((long) (yawDelta * MovementProcessor.offset), (long) (lastYD * MovementProcessor.offset));
            debug("yawTo=" + yawToEntity + " gcd=" + gcd);
            lastYD = yawDelta;
            lastYaw = yawToEntity;
        }
    }
}
