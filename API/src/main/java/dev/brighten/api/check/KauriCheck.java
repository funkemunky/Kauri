package dev.brighten.api.check;

public interface KauriCheck {
    String getName();
    String getDescription();
    boolean isEnabled();
    boolean isExecutable();
    DevStage getDevStage();
    CheckType getCheckType();
    float getVl();
    float getPunishVl();
    void setEnabled(boolean enabled);
    void setExecutable(boolean executable);
    void setVl(float vl);
    void setPunishVl(float pvl);
}
