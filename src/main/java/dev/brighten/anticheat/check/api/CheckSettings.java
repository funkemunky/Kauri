package dev.brighten.anticheat.check.api;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Retention;

@AllArgsConstructor
@RequiredArgsConstructor
public class CheckSettings {
    public boolean enabled, executable;
    public final String name, description;
    public final CheckType type;
    public final int punishVl;
}
