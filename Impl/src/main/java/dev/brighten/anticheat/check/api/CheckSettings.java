package dev.brighten.anticheat.check.api;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@AllArgsConstructor
@RequiredArgsConstructor
public class CheckSettings {
    public boolean enabled, executable, cancellable;
    public final String name, description;
    public final CheckType type;
    public final DevStage devStage;
    public final CancelType cancelMode;
    public int punishVl, vlToFlag;
    public final ProtocolVersion minVersion, maxVersion;
    public List<String> executableCommands;
}
