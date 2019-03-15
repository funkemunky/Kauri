package cc.funkemunky.anticheat.api.data.processors;

import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.TickTimer;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInCloseWindowPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInEntityActionPacket;
import lombok.Getter;

@Getter
public class ActionProcessor {
    private boolean sprinting, openInventory, sneaking, flyFall, horseJump, isUsingItem;
    private TickTimer lastUseItem = new TickTimer(10);

    public void update(WrappedInEntityActionPacket packet) {
        switch (packet.getAction()) {
            case START_SPRINTING:
                sprinting = true;
                break;
            case STOP_SPRINTING:
                sprinting = false;
                break;
            case OPEN_INVENTORY:
                openInventory = true;
                break;
            case START_SNEAKING:
                sneaking = true;
                break;
            case STOP_SNEAKING:
                sneaking = false;
                break;
            case START_FALL_FLYING:
                flyFall = true;
                break;
            case START_RIDING_JUMP:
                horseJump = true;
                break;
            case STOP_RIDING_JUMP:
                horseJump = false;
                break;
        }
    }

    public void update(WrappedInCloseWindowPacket packet) {
        openInventory = false;
    }

    public void update(PlayerData data) {
        if ((isUsingItem = Atlas.getInstance().getBlockBoxManager().getBlockBox().isUsingItem(data.getPlayer()))) {
            lastUseItem.reset();
        }
    }
}
