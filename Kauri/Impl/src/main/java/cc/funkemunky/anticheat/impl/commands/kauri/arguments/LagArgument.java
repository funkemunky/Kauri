package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.Message;
import cc.funkemunky.anticheat.api.utils.Messages;
import cc.funkemunky.anticheat.api.utils.Pastebin;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.profiling.ResultsType;
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
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

public class LagArgument extends FunkeArgument {
    public LagArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);

        addAlias("tps");
        addAlias("transPing");

        addTabComplete(2, "player", "server");
    }

    @Message(name = "command.lag.serverInfo.title")
    private String serverInfo = "&6&lServer Info";

    @Message(name = "command.lag.serverInfo.tps")
    private String tps = "&8» &eTPS&7: &f%tps%";

    @Message(name = "command.lag.serverInfo.tickTime")
    private String tickTime = "&8» &eMS&7: &f%ms%";

    @Message(name = "command.lag.serverInfo.memory")
    private String memory = "&8» &eMemory&7: &f%free%GB/%total%GB";

    @Message(name = "command.lag.lineColor")
    private String lineColor = Color.Dark_Gray;

    @Message(name = "command.lag.kauri.title")
    private String kauriRes = "&6&lKauri Resources";

    @Message(name = "command.lag.kauri.pctUsage")
    private String pctUsage = "&8» &ePercent Usage&7: &f%pct%%";

    @Message(name = "command.lag.kauri.callsPerSecond")
    private String callsPS = "&8» &eCalls Per Second&7: &f%calls%";

    @Message(name = "command.lag.profile")
    private String profile = "&7Results: &f%link%";

    @Message(name = "command.lag.player.title")
    private String playerTitle = "&f&l%player%&7&l's Lag";

    @Message(name = "command.lag.player.latency")
    private String playerLatency = "&6&lLatency";

    @Message(name = "command.lag.player.bukkitPing")
    private String playerBukkitPing = "&8» &eBukkit&7: &f%ping%ms";

    @Message(name = "command.lag.player.kauriPing")
    private String playerKauriPing = "&8» &eKauri&7: &f%ping%ms";

    @Message(name = "command.lag.player.positionalPing")
    private String playerPositionalPing = "&8» &ePositional&7: &f%ping%ms";

    @Message(name = "command.lag.player.stability")
    private String playerStability = "&6&lStability";

    @Message(name = "command.lag.player.lastPacketDrop")
    private String playerPacketDrop = "&8» &eLast Packet Drop&7: &f%lastDrop% ago.";

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        if (args.length == 1) {
            sendServerInfo(sender);
        } else {
            switch (args[1].toLowerCase()) {
                case "profile": {
                    List<String> body = new ArrayList<>();
                    body.add(MiscUtils.lineNoStrike());
                    val results = Kauri.getInstance().getProfiler().results(ResultsType.TOTAL);

                    AtomicReference<Double> totalMS = new AtomicReference<>((double) 0);

                    results.keySet().stream()
                            .sorted(Comparator.comparing(key -> results.get(key) / Kauri.getInstance().getProfiler().calls.get(key), Comparator.reverseOrder()))
                            .forEachOrdered(key -> {
                                body.add(key + ":");
                                double ms = results.get(key) / Kauri.getInstance().getProfiler().calls.get(key);
                                totalMS.updateAndGet(v -> (v + ms));
                                body.add("PCT: "  + MathUtils.round(ms / 50D * 100, 4));
                                body.add("MS: " + ms + "ms");
                                body.add("Calls:" + Kauri.getInstance().getProfiler().calls.get(key));
                            });
                    body.add("Total PCT: " +  MathUtils.round(totalMS.get() / 50 * 100, 4) + "%");
                    body.add("Total Time: " + totalMS + "ms");
                    body.add("Total Calls: " + Kauri.getInstance().getProfiler().totalCalls);
                    body.add(MiscUtils.lineNoStrike());

                    StringBuilder builder = new StringBuilder();
                    for (String aBody : body) {
                        builder.append(aBody).append(";");
                    }

                    builder.deleteCharAt(body.size() - 1);

                    String bodyString = builder.toString().replaceAll(";", "\n");

                    try {
                        sender.sendMessage(Color.translate(profile.replace("%link%", Pastebin.makePaste(bodyString, "Kauri Profile: " + DateFormatUtils.format(System.currentTimeMillis(), ", ", TimeZone.getTimeZone("604")), Pastebin.Privacy.UNLISTED))));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "realprofile": {
                    List<String> body = new ArrayList<>();
                    body.add(MiscUtils.lineNoStrike());
                    val results = Kauri.getInstance().getProfiler().results(ResultsType.TOTAL);

                    AtomicReference<Double> totalMS = new AtomicReference<>((double) 0);
                    long totalTime = System.currentTimeMillis() - Kauri.getInstance().getProfileStart();
                    results.keySet().stream()
                            .sorted(Comparator.comparing(key -> results.get(key) / Kauri.getInstance().getProfiler().calls.get(key), Comparator.reverseOrder()))
                            .forEachOrdered(key -> {
                                body.add(key + ":");
                                double msTotal = results.get(key);
                                double totalCalls = Kauri.getInstance().getProfiler().calls.get(key);
                                double calls = totalCalls / (totalTime / 50f);
                                double ms =  msTotal / (totalTime / 50f);
                                totalMS.updateAndGet(v -> (v + ms));
                                body.add("PCT: "  + MathUtils.round(ms / 50D * 100, 4));
                                body.add("MS: " + ms + "ms");
                                body.add("Calls:" + MathUtils.round(calls, 2));
                            });
                    body.add("Total PCT: " +  MathUtils.round(totalMS.get() / 50 * 100, 4) + "%");
                    body.add("Total Time: " + totalMS + "ms");
                    body.add("Total Calls: " + MathUtils.round( Kauri.getInstance().getProfiler().totalCalls / (totalTime / 50f), 2));
                    body.add("Total Time: " + DurationFormatUtils.formatDurationWords(totalTime, true, true));
                    body.add(MiscUtils.lineNoStrike());

                    StringBuilder builder = new StringBuilder();
                    for (String aBody : body) {
                        builder.append(aBody).append(";");
                    }

                    builder.deleteCharAt(body.size() - 1);

                    String bodyString = builder.toString().replaceAll(";", "\n");

                    try {
                        sender.sendMessage(Color.translate(profile.replace("%link%", Pastebin.makePaste(bodyString, "Kauri Profile: " + DateFormatUtils.format(System.currentTimeMillis(), ", ", TimeZone.getTimeZone("604")), Pastebin.Privacy.UNLISTED))));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "server":
                    sendServerInfo(sender);
                    break;
                case "player":
                    if (args.length >= 3) {
                        Player player = Bukkit.getPlayer(args[2]);

                        if (player != null) {
                            PlayerData data = Kauri.getInstance().getDataManager().getPlayerData(player.getUniqueId());

                            if (data != null) {
                                sender.sendMessage(MiscUtils.line(lineColor));
                                sender.sendMessage(Color.translate(playerTitle.replace("%player%", player.getName())));
                                sender.sendMessage("");
                                sender.sendMessage(Color.translate(playerLatency));
                                sender.sendMessage(Color.translate(playerBukkitPing.replace("%ping%", String.valueOf(data.getPing()))));
                                sender.sendMessage(Color.translate(playerKauriPing.replace("%ping%", String.valueOf(data.getTransPing()))));
                                sender.sendMessage(Color.translate(playerPositionalPing.replace("%ping%", String.valueOf(data.getTeleportPing()))));
                                sender.sendMessage("");
                                sender.sendMessage(Color.translate(playerStability));
                                sender.sendMessage(Color.translate(playerPacketDrop.replace("%lastDrop%", DurationFormatUtils.formatDurationWords(System.currentTimeMillis() - data.getLastPacketDrop(), true, true))));
                                sender.sendMessage(MiscUtils.line(lineColor));
                            } else {
                                sender.sendMessage(Color.translate(Messages.errorData));
                            }
                        } else {
                            sender.sendMessage(Color.translate(Messages.errorPlayerOffline));
                        }
                    } else {
                        sender.sendMessage(Color.translate(Messages.invalidArguments));
                    }
                    break;
                default:
                    sender.sendMessage(Color.translate(Messages.invalidArguments));
                    break;
            }
        }
    }

    private void sendServerInfo(CommandSender sender) {
        sender.sendMessage(MiscUtils.line(lineColor));
        sender.sendMessage(Color.translate(serverInfo));
        sender.sendMessage(Color.translate(tps.replace("%tps%", String.valueOf(MathUtils.round(Kauri.getInstance().getTps(), 2)))));
        sender.sendMessage(Color.translate(tickTime.replace("%ms%", String.valueOf(MathUtils.round(Kauri.getInstance().getTpsMS(), 4)))));

        val freeMem = MathUtils.round(Runtime.getRuntime().freeMemory() / (1024D * 1024D * 1024D), 2);
        val totalMem = MathUtils.round(Runtime.getRuntime().totalMemory() / (1024D * 1024D * 1024D), 2);
        sender.sendMessage(Color.translate(memory.replace("%free%", String.valueOf(freeMem)).replace("%total%", String.valueOf(totalMem))));
        sender.sendMessage("");
        sender.sendMessage(Color.translate(kauriRes));
        long totalTime = System.currentTimeMillis() - Kauri.getInstance().getProfileStart();
        double totalMS = Kauri.getInstance().getProfiler().samples.keySet().stream().mapToDouble(key -> Kauri.getInstance().getProfiler().samples.get(key) / 1000000D).sum();
        sender.sendMessage(Color.translate(pctUsage.replace("%pct%", ((totalMS / totalTime / (50D / totalTime)) / 50) * 100 + "")));
        float cps = Kauri.getInstance().getProfiler().totalCalls / (float) totalTime;
        sender.sendMessage(Color.translate(callsPS.replace("%calls%", String.valueOf(totalMS / totalTime / (50D / totalTime)))));
        sender.sendMessage(MiscUtils.line(lineColor));
    }
}
