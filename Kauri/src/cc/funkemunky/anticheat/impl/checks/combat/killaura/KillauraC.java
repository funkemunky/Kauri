package cc.funkemunky.anticheat.impl.checks.combat.killaura;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInEntityActionPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.PlayerUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;

@Packets(packets = {Packet.Client.USE_ENTITY, Packet.Client.ENTITY_ACTION})
public class KillauraC extends Check {
    private Verbose verbose = new Verbose();
    private boolean isSprinting;

    public KillauraC(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);
    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (packetType.equalsIgnoreCase(Packet.Client.ENTITY_ACTION)) { //We don't use the player object since it is always 1 tick behind.
            WrappedInEntityActionPacket action = new WrappedInEntityActionPacket(packet, getData().getPlayer());

            switch (action.getAction()) {
                case START_SPRINTING: {
                    isSprinting = true;
                    break;
                }
                case STOP_SPRINTING: {
                    isSprinting = false;
                    break;
                }
            }
        } else {
            WrappedInUseEntityPacket use = new WrappedInUseEntityPacket(packet, getData().getPlayer());

            if (use.getEntity() instanceof Player && use.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) { //A player only stops sprinting when hitting a player.
                double deltaXZ = MathUtils.getHorizontalDistance(getData().getMovementProcessor().getTo().toLocation(use.getPlayer().getWorld()), getData().getMovementProcessor().getFrom().toLocation(use.getPlayer().getWorld()));
                if (!getData().isGeneralCancel() && (deltaXZ > getBaseSpeed() && isSprinting)) {
                    if (verbose.flag(10, 850L)) { //We add a verbose or redundancy.
                        flag(deltaXZ + ">-" + getBaseSpeed(), true, true);
                    }
                }
            }
        }
        return;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private float getBaseSpeed() {
        return 0.25f + (PlayerUtils.getPotionEffectLevel(getData().getPlayer(), PotionEffectType.SPEED) * 0.062f) + ((getData().getPlayer().getWalkSpeed() - 0.2f) * 1.6f);
    }
}
