package dev.brighten.anticheat.check.impl.movement.general;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.anticheat.utils.TickTimer;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.util.Vector;

@CheckInfo(name = "OmniSprint", description = "Checks for sprinting in illegal directions.",
        checkType = CheckType.GENERAL, vlToFlag = 15, punishVL = 50, developer = true)
@Cancellable
public class OmniSprint extends Check {

    private TickTimer lastKeySwitch;
    private String lastKey = "";
    private float buffer;

    @Override
    public void setData(ObjectData data) {
        super.setData(data);
        lastKeySwitch = new TickTimer(1);
    }

    @Packet
    public void onMove(WrappedInFlyingPacket packet) {
        if(!lastKey.equals(data.predictionService.key)) lastKeySwitch.reset();
        if(isPosition(packet)
                && data.playerInfo.lastTeleportTimer.hasPassed(4)
                && !data.playerInfo.serverPos
                && !data.playerInfo.generalCancel
                && data.playerInfo.climbTimer.hasPassed(2)
                && data.playerInfo.liquidTimer.hasPassed(2)
                && data.playerInfo.sprinting) {
            if(data.predictionService.moveForward <= 0
                    && (data.predictionService.moveForward != 0 || data.predictionService.moveStrafing != 0)
                    && lastKeySwitch.hasPassed(0)) {
                if(++buffer > 4) {
                    vl++;
                    flag("key=%v", data.predictionService.key);
                }
            } else buffer-= buffer > 0 ? 0.25f : 0;

            debug("keys=%v g=%v lastK=%v",
                    data.predictionService.key, data.playerInfo.serverGround, lastKeySwitch.getPassed());
        }
        lastKey = data.predictionService.key;
    }

}
