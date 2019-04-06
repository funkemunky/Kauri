package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.Pastebin;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
import lombok.val;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class LagArgument extends FunkeArgument {
    public LagArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);

        addAlias("tps");
        addAlias("transPing");

        addTabComplete(2, "player", "server");
    }

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        if (args.length == 1) {
            sender.sendMessage(MiscUtils.line(Color.Dark_Gray));
            sender.sendMessage(MiscUtils.line(Color.Dark_Gray));
            sender.sendMessage(Color.translate("&6&lServer Info"));
            sender.sendMessage(Color.translate("&8» &eTPS&7: &f" + MathUtils.round(Kauri.getInstance().getTps(), 2)));
            sender.sendMessage(Color.translate("&8» &eMS&7: &f" + Kauri.getInstance().getTickElapsed()));

            val freeMem = MathUtils.round(Runtime.getRuntime().freeMemory() / (1024D * 1024D * 1024D), 2);
            val totalMem = MathUtils.round(Runtime.getRuntime().totalMemory() / (1024D * 1024D * 1024D), 2);
            sender.sendMessage(Color.translate("&8» &eMemory&7: &f" + freeMem + "GB/" + totalMem + "GB"));
            sender.sendMessage("");
            sender.sendMessage(Color.translate("&6&lKauri Resources"));
            float totalPCT = 0;
            long totalTime = MathUtils.elapsed(Kauri.getInstance().getProfileStart());
            for (String string : Kauri.getInstance().getProfiler().total.keySet()) {
                double stringTotal = TimeUnit.NANOSECONDS.toMillis(Kauri.getInstance().getProfiler().total.get(string));
                double pct = stringTotal / totalTime;
                totalPCT += (pct);
            }
            sender.sendMessage(Color.translate("&8» &ePercent Usage&7: &f" + totalPCT));
            float cps = Kauri.getInstance().getProfiler().totalCalls / (float) totalTime;
            sender.sendMessage(Color.translate("&8» &eCalls Per Second&7: &f" + Color.White + MathUtils.round(cps, 1)));
            sender.sendMessage(MiscUtils.line(Color.Dark_Gray));
        } else {
            switch (args[1].toLowerCase()) {
                case "profile": {
                    List<String> body = new ArrayList<>();
                    body.add(MiscUtils.lineNoStrike());
                    float totalPCT = 0;
                    long totalTime = MathUtils.elapsed(Kauri.getInstance().getProfileStart());
                    for (String string : Kauri.getInstance().getProfiler().total.keySet()) {
                        body.add(string);
                        double stringTotal = TimeUnit.NANOSECONDS.toMillis(Kauri.getInstance().getProfiler().total.get(string));
                        int calls = Kauri.getInstance().getProfiler().calls.get(string);
                        double pct = stringTotal / totalTime;
                        body.add("Latency: " + stringTotal / calls + "ms");
                        body.add("Calls: " + calls);
                        body.add("STD: " + Kauri.getInstance().getProfiler().stddev.get(string));
                        body.add("PCT: " + MathUtils.round(pct, 8));
                        totalPCT += (pct);
                    }
                    body.add("Total PCT: " + MathUtils.round(totalPCT, 4) + "%");
                    body.add("Total Time: " + totalTime + "ms");
                    body.add("Total Calls: " + Kauri.getInstance().getProfiler().totalCalls);
                    body.add(MiscUtils.lineNoStrike());

                    StringBuilder builder = new StringBuilder();
                    for (String aBody : body) {
                        builder.append(aBody).append(";");
                    }

                    builder.deleteCharAt(body.size() - 1);

                    String bodyString = builder.toString().replaceAll(";", "\n");

                    try {
                        sender.sendMessage(Color.Green + "Results: " + Pastebin.makePaste(bodyString, "Kauri Profile: " + DateFormatUtils.format(System.currentTimeMillis(), ", ", TimeZone.getTimeZone("604")), Pastebin.Privacy.UNLISTED));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "server":
                    sender.sendMessage(MiscUtils.line(Color.Dark_Gray));
                    sender.sendMessage(Color.translate("&6&lServer Info"));
                    sender.sendMessage(Color.translate("&8» &eTPS&7: &f" + MathUtils.round(Kauri.getInstance().getTps(), 2)));
                    sender.sendMessage(Color.translate("&8» &eMS&7: &f" + Kauri.getInstance().getTickElapsed()));

                    val freeMem = MathUtils.round(Runtime.getRuntime().freeMemory() / (1024D * 1024D * 1024D), 2);
                    val totalMem = MathUtils.round(Runtime.getRuntime().totalMemory() / (1024D * 1024D * 1024D), 2);
                    sender.sendMessage(Color.translate("&8» &eMemory&7: &f" + freeMem + "GB/" + totalMem + "GB"));
                    sender.sendMessage("");
                    sender.sendMessage(Color.translate("&6&lKauri Resources"));
                    float totalPCT = 0;
                    long totalTime = MathUtils.elapsed(Kauri.getInstance().getProfileStart());
                    for (String string : Kauri.getInstance().getProfiler().total.keySet()) {
                        double stringTotal = TimeUnit.NANOSECONDS.toMillis(Kauri.getInstance().getProfiler().total.get(string));
                        double pct = stringTotal / totalTime;
                        totalPCT += (pct);
                    }
                    sender.sendMessage(Color.translate("&8» &ePercent Usage&7: &f" + totalPCT));
                    float cps = Kauri.getInstance().getProfiler().totalCalls / (float) totalTime;
                    sender.sendMessage(Color.translate("&8» &eCalls Per Second&7: &f" + Color.White + MathUtils.round(cps, 1)));
                    sender.sendMessage(MiscUtils.line(Color.Dark_Gray));
                    break;
                case "player":
                    if (args.length >= 3) {
                        Player player = Bukkit.getPlayer(args[2]);

                        if (player != null) {
                            PlayerData data = Kauri.getInstance().getDataManager().getPlayerData(player.getUniqueId());

                            if (data != null) {
                                sender.sendMessage(MiscUtils.line(Color.Dark_Gray));
                                sender.sendMessage(Color.translate("&6&lLatency"));
                                sender.sendMessage(Color.translate("&8» &eBukkit&7: &f" + data.getPing() + "ms"));
                                sender.sendMessage(Color.translate("&8» &eKauri&7: &f" + data.getTransPing() + "ms"));
                                sender.sendMessage("");
                                sender.sendMessage(Color.translate("&6&lStability"));
                                sender.sendMessage(Color.translate("&8» &eLast Packet Drop&7: &f" + DurationFormatUtils.formatDurationWords(data.getLastLag().getPassed() * 50, true, true)) + " ago.");
                                sender.sendMessage(MiscUtils.line(Color.Dark_Gray));
                            } else {
                                sender.sendMessage(Color.Red + "There was an error trying to find " + player.getName() + "'s data object.");
                            }
                        } else {
                            sender.sendMessage(Color.Red + "The player \"" + args[2] + "\" is not online!");
                        }
                    } else {
                        sender.sendMessage(getParent().getCommandMessages().getErrorColor() + getParent().getCommandMessages().getInvalidArguments());
                    }
                    break;
                default:
                    sender.sendMessage(getParent().getCommandMessages().getErrorColor() + getParent().getCommandMessages().getInvalidArguments());
                    break;
            }
        }
    }
}
