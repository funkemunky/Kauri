package dev.brighten.anticheat.utils.lunar.util.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ServerRule {
    VOICE_ENABLED("voiceEnabled"),
    MINIMAP_STATUS("minimapStatus"),
    SERVER_HANDLES_WAYPOINTS("serverHandlesWaypoints"),
    COMPETITIVE_GAMEMODE("competitiveGame");

    /* name of enum instance */
    private final String name;
}
