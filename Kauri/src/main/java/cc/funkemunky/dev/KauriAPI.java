package cc.funkemunky.dev;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.dev.exceptions.CheckDoesNotExistException;

public class KauriAPI {
    public static KauriAPI INSTANCE;

    public KauriAPI() {
        INSTANCE = this;
    }

    public void toggleCheck(String check, ToggleType toggleType, boolean enabled) throws CheckDoesNotExistException {

    }

}
