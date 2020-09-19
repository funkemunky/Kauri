package dev.brighten.api;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum KauriVersion {
    FREE("Free", 0),
    FULL("Full", 45),
    ARA("Ara", 8.99f);
    
    public final String name;
    public final float price;
    
    public static String getVersion() {
        return KauriAPI.INSTANCE.kauriPlugin.getPlugin().getDescription().getVersion();
    }
    
    public static KauriVersion getPlan() {
        if(KauriAPI.INSTANCE.kauriPlugin.fetchField("usingPremium"))
            return FULL;

        if(KauriAPI.INSTANCE.kauriPlugin.fetchField("usingAra"))
            return ARA;

        return FREE;
    }
}
