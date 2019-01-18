package cc.funkemunky.anticheat.api.data;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.data.processors.ActionProcessor;
import cc.funkemunky.anticheat.api.data.processors.MovementProcessor;
import cc.funkemunky.anticheat.api.data.processors.VelocityProcessor;
import cc.funkemunky.anticheat.api.utils.MCSmooth;
import cc.funkemunky.anticheat.api.utils.TickTimer;
import cc.funkemunky.api.utils.BoundingBox;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class PlayerData {
    public Location setbackLocation;
    public int lagTicks;
    private UUID uuid, debuggingPlayer;
    private Check debuggingCheck;
    private Player player;
    private List<Check> checks = Lists.newArrayList();
    private CancelType cancelType = CancelType.NONE;
    private boolean ableToFly, creativeMode, invulnerable, flying, onSlimeBefore, isRiptiding = false, generalCancel, breakingBlock,
            cinematicMode, lagging;
    private Vector lastVelocityVector;
    private BoundingBox boundingBox;
    private TickTimer lastMovementCancel = new TickTimer(4),
            lastServerPos = new TickTimer(8),
            lastLag = new TickTimer(10),
            lastLogin = new TickTimer(60),
            lastBlockPlace = new TickTimer(30);
    private float walkSpeed, flySpeed;
    private long transPing, lastTransaction, lastTransPing, ping, lastPing, lastKeepAlive;
    private MCSmooth yawSmooth = new MCSmooth(), pitchSmooth = new MCSmooth();

    /* Processors */
    private MovementProcessor movementProcessor;
    private ActionProcessor actionProcessor;
    private VelocityProcessor velocityProcessor;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.player = Bukkit.getPlayer(uuid);
        lastLogin.reset();

        movementProcessor = new MovementProcessor();
        actionProcessor = new ActionProcessor();
        velocityProcessor = new VelocityProcessor();

        Kauri.getInstance().getCheckManager().loadChecksIntoData(this);
    }
}
