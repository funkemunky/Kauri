package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.*;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;

@CheckInfo(name = "Aim (Type L)", description = "Checks for impossible packet rates sent by anything that aims.", type = CheckType.AIM, cancellable = false, maxVL = 20)
@Init
@BukkitEvents(events = {PlayerMoveEvent.class})
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.LOOK, Packet.Client.LEGACY_LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION})
public class AimL extends Check {

    private MCSmooth smoothUtil = new MCSmooth();
    private float lastSmooth;
    private boolean set;

    private Verbose verbose = new Verbose();
    
    private int safe, bad;

    private long lastTimeStamp, lastSet, last;
    
    private Map<Float, Integer> data = new HashMap<>();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {

        switch(packetType) {
            case Packet.Client.POSITION:
            case Packet.Client.LEGACY_POSITION:
                lastTimeStamp = timeStamp;
                break;
            case Packet.Client.LOOK:
            case Packet.Client.LEGACY_LOOK:
                lastTimeStamp = timeStamp;
                reset();
                break;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {
        PlayerMoveEvent e = (PlayerMoveEvent) event;

        /*if ((System.currentTimeMillis() - user.lastFullblockMoved) > 400L) {
            last = System.currentTimeMillis();
        }*/
        long timeStamp = System.currentTimeMillis();
        if (set && timeStamp - lastSet > 2000L) {
            reset();
        }
        if (getData().getLastAttack().hasNotPassed(10) && timeStamp - last >= 3000L) {
            if (set) {
                if (timeStamp - lastSet > 2000L) {
                    reset();
                }
            } else {
                set = true;
                lastSet = System.currentTimeMillis();
            }
            if (data.size() == safe && bad < 1 && safe > 32 && getData().getPlayer().getLocation().distance(getData().getTarget().getLocation()) > 1.00 && verbose.flag(10, 450L)) {
               flag("test", true, true);
            }
            float weight = (float) MathUtils.round(Math.abs(MathUtils.yawTo180F(e.getFrom().getYaw() - e.getTo().getYaw())), 3);
            if (!data.containsKey(weight)) {
                data.put(weight, 1);
                safe++;
            } else {
                bad++;
            }
        }

        debug(verbose.getVerbose() + " vl");
    }

    private void reset() {
        set = false;
        bad = safe = 0;
        data.clear();
        verbose.setVerbose(0);
    }
}
