package cc.funkemunky.anticheat.api.pup;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.anticheat.impl.pup.bot.ConsoleClient;
import cc.funkemunky.anticheat.impl.pup.crashers.ArmSwing;
import cc.funkemunky.anticheat.impl.pup.crashers.Boxer;
import cc.funkemunky.anticheat.impl.pup.crashers.YLevel;
import cc.funkemunky.anticheat.impl.pup.vpn.AntiVPN;

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

        addMethod(new AntiVPN("AntiVPN", true), list);
        addMethod(new ConsoleClient("ConsoleClient", true), list);
        addMethod(new ArmSwing("ArmSwing", true), list);
        addMethod(new Boxer("Boxer", true), list);
        addMethod(new YLevel("YLevel", true), list);

        for (AntiPUP pup : list) {
            Arrays.stream(pup.getClass().getDeclaredFields()).filter(field -> {
                field.setAccessible(true);

                return field.isAnnotationPresent(Setting.class);
            }).forEach(field -> {
                try {
                    field.setAccessible(true);

                    String path = "antipup." + pup.getName() + ".settings." + field.getName();
                    if (Kauri.getInstance().getConfig().get(path) != null) {
                        Object val = Kauri.getInstance().getConfig().get(path);

                        if (val instanceof Double && field.get(pup) instanceof Float) {
                            field.set(pup, (float) (double) val);
                        } else {
                            field.set(pup, val);
                        }
                    } else {
                        Kauri.getInstance().getConfig().set("antipup." + pup.getName() + ".settings." + field.getName(), field.get(pup));
                        Kauri.getInstance().saveConfig();
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });

            if (Kauri.getInstance().getConfig().get("antipup." + pup.getName() + ".enabled") == null) {
                Kauri.getInstance().getConfig().set("antipup." + pup.getName() + ".enabled", pup.isEnabled());
            } else {
                pup.setEnabled(Kauri.getInstance().getConfig().getBoolean("antipup." + pup.getName() + ".enabled"));
            }
        }

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
