package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.event.PlayerCancelEvent;
import cc.funkemunky.anticheat.api.event.PlayerCheatEvent;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.event.system.Listener;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.JsonMessage;
import cc.funkemunky.api.utils.MiscUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class Check implements Listener, org.bukkit.event.Listener {
    private String name;
    private CheckType type;
    private String description;
    private CancelType cancelType;
    private PlayerData data;
    private int maxVL, banWaveThreshold;
    private boolean enabled, executable, cancellable, developer, isBanWave;
    private long lastAlert;
    private List<String> execCommand = new ArrayList<>();
    private Map<String, Object> settings = new HashMap<>();
    private String alertMessage = "";
    private int vl = 0;
    private ProtocolVersion minimum, maximum;

    public Check() {
        alertMessage = CheckSettings.alertMessage;
    }

    protected void flag(String information, boolean cancel, boolean ban, AlertTier tier) {

    }
    private void banUser() {

    }

    public void loadFromConfig() {

    }

    public void debug(String debugString) {

    }

    public abstract void onPacket(Object packet, String packetType, long timeStamp);

    public abstract void onBukkitEvent(Event event);
}
