package cc.funkemunky.anticheat.api.pup;

import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.Packets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AntiPUPManager {
    private List<AntiPUP> antibot;

    public AntiPUPManager() {
        antibot = registerMethods();
    }

    public List<AntiPUP> registerMethods() {
        List<AntiPUP> list = new ArrayList<>();


        return list;
    }

    public AntiPUP getMethodByName(String name) {
        Optional<AntiPUP> opPUP = antibot.stream().filter(pup -> pup.getName().equalsIgnoreCase(name)).findFirst();

        return opPUP.orElse(null);
    }

    public void loadMethodsIntoData(PlayerData data) {
        List<AntiPUP> methodList = registerMethods();

        methodList.stream().filter(method -> method.getClass().isAnnotationPresent(Packets.class)).forEach(method -> {
            Packets packets = method.getClass().getAnnotation(Packets.class);

            Arrays.stream(packets.packets()).forEach(packet -> {
                List<AntiPUP> methods = data.getAntiPupMethods().getOrDefault(packet, new ArrayList<>());

                methods.add(method);

                data.getAntiPupMethods().put(packet, methods);
            });
        });
    }
}
