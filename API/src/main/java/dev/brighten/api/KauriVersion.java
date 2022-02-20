package dev.brighten.api;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum KauriVersion {
    FREE("Free", 0, false),
    FULL("Full", 20, false),
    ARA("Ara", 12.99f, true);
    
    public final String name;
    public final float price;
    public final boolean monthly;
    
    public static String getVersion() {
        return KauriAPI.INSTANCE.kauriPlugin.getPlugin().getDescription().getVersion();
    }
    
    public static KauriVersion getPlan() {
        if(KauriAPI.INSTANCE.kauriPlugin.fetchField("usingAra"))
            return ARA;

        if(KauriAPI.INSTANCE.kauriPlugin.fetchField("usingPremium"))
            return FULL;

        return FREE;
    }
}
