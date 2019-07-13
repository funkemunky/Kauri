package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.impl.config.CheckSettings;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public abstract class Check {
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
    private List<String> packets = new ArrayList<>();
    private List<Class> events = new ArrayList<>();

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
