package dev.brighten.anticheat.utils.lunar.util.type;

import lombok.Getter;

@Getter
public enum Emote {

    WAVE(0),
    HANDS_UP(1),
    FLOSS(2),
    DAB(3),
    T_POSE(4),
    SHRUG(5),
    FACEPALM(6);

    private int emoteId;

    Emote(int emoteId) {
        this.emoteId = emoteId;
    }

    public static Emote getById(int emoteId) {
        for (Emote emote : values()) {
            if (emote.getEmoteId() == emoteId) {
                return emote;
            }
        }
        return null;
    }

    public static Emote getByName(String input) {
        for (Emote emote : values()) {
            if (emote.name().equalsIgnoreCase(input)) {
                return emote;
            }
        }
        return null;
    }

}
