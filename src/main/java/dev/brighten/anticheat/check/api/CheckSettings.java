package dev.brighten.anticheat.check.api;

public class CheckSettings {
    public boolean enabled, executable;

    public CheckSettings(boolean enabled, boolean executable) {
        this.enabled = enabled;
        this.executable = executable;
    }

    public CheckSettings() {

    }
}
