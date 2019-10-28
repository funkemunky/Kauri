package dev.brighten.anticheat.check.api;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
public class CheckSettings {
    public boolean enabled, executable;
    public final String name, description;
    public final CheckType type;
    public final int punishVl;
}
