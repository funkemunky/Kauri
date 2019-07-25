package cc.funkemunky.anticheat.api.lunar.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class User {

    /* Global Data */
    private final UUID uniqueId;
    private final String name;

    /* Session Data */
    private boolean lunarClient;

}