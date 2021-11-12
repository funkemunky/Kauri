package dev.brighten.api.check;

import dev.brighten.api.KauriVersion;

public interface KauriCheck {
    String getName();
    String getDescription();
    boolean isEnabled();
    boolean isExecutable();
    DevStage getDevStage();
    KauriVersion getPlan();
    CheckType getCheckType();
    float getVl();
    float getPunishVl();
    void setEnabled(boolean enabled);
    void setExecutable(boolean executable);
    void setVl(float vl);
    void setPunishVl(float pvl);
}
