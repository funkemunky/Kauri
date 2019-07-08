package cc.funkemunky.anticheat.checks;

import cc.funkemunky.anticheat.data.PlayerData;

public abstract class Check {
    public String name;
    public CheckType type;
    public int vl, maxVL;

    public void flag(boolean ban, boolean cancel, String... information) {
        //TODO Flaggy thingys.
    }

    public void init(PlayerData data) {

    }
}
