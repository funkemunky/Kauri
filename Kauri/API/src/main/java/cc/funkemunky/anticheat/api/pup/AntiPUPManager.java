package cc.funkemunky.anticheat.api.pup;

import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.database.DatabaseType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AntiPUPManager {
    private List<AntiPUP> antibot;
    public ExecutorService pupThread = Executors.newFixedThreadPool(2);

    public AntiPUPManager() {
        antibot = registerMethods();
        Atlas.getInstance().getDatabaseManager().createDatabase("VPN-Cache", DatabaseType.FLATFILE);
    }

    public List<AntiPUP> registerMethods() {
        List<AntiPUP> list = new ArrayList<>();

        return list;
    }

    public void addMethod(AntiPUP pup, List<AntiPUP> list) {
        list.add(pup);
    }

    public AntiPUP getMethodByName(String name) {
        Optional<AntiPUP> opPUP = antibot.stream().filter(pup -> pup.getName().equalsIgnoreCase(name)).findFirst();

        return opPUP.orElse(null);
    }

    public void loadMethodsIntoData(PlayerData data) {

    }
}
