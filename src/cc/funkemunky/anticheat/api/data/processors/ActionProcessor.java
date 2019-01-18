package cc.funkemunky.anticheat.api.data.processors;

import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInCloseWindowPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInEntityActionPacket;
import lombok.Getter;

@Getter
@Setting
public class ActionProcessor {
    private boolean sprinting, openInventory, sneaking, flyFall, horseJump;

    public void update(WrappedInEntityActionPacket packet) {
        switch(packet.getAction()) {
            case START_SPRINTING:
                sprinting = true;
            case STOP_SPRINTING:
                sprinting = false;
            case OPEN_INVENTORY:
                openInventory = true;
                break;
            case START_SNEAKING:
                sneaking = true;
                break;
            case STOP_SNEAKING:
                sneaking = false;
            case START_FALL_FLYING:
                flyFall = true;
            case START_RIDING_JUMP:
                horseJump = true;
            case STOP_RIDING_JUMP:
                horseJump = false;
        }
    }

    public void update(WrappedInCloseWindowPacket packet) {
        openInventory = false;
    }
}
