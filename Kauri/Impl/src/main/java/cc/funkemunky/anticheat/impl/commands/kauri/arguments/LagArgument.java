package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.Message;
import cc.funkemunky.anticheat.api.utils.Messages;
import cc.funkemunky.anticheat.api.utils.Pastebin;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.profiling.ResultsType;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
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
import java.util.stream.Collectors;

public class LagArgument extends FunkeArgument {
    public LagArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);

        addAlias("tps");
        addAlias("transPing");

        addTabComplete(2, "player", "server", "profile");
    }

    @Message(name = "command.lag.serverInfo.title")
    private static String serverInfo = "&6&lServer Info";

    @Message(name = "command.lag.serverInfo.tps")
    private static String tps = "&8» &eTPS&7: &f%tps%";

    @Message(name = "command.lag.serverInfo.tickTime")
    private static String tickTime = "&8» &eMS&7: &f%ms%";

    @Message(name = "command.lag.serverInfo.memory")
    private static String memory = "&8» &eMemory&7: &f%free%GB/%total%GB";

    @Message(name = "command.lag.lineColor")
    private static String lineColor = Color.Dark_Gray;

    @Message(name = "command.lag.kauri.title")
    private static String kauriRes = "&6&lKauri Resources";

    @Message(name = "command.lag.kauri.pctUsage")
    private static String pctUsage = "&8» &ePercent Usage&7: &f%pct%%";

    @Message(name = "command.lag.kauri.callsPerSecond")
    private static String callsPS = "&8» &eCalls Per Second&7: &f%calls%";

    @Message(name = "command.lag.profile")
    private static String profile = "&7Results: &f%link%";

    @Message(name = "command.lag.player.title")
    private static String playerTitle = "&f&l%player%&7&l's Lag";

    @Message(name = "command.lag.player.latency")
    private static String playerLatency = "&6&lLatency";

    @Message(name = "command.lag.player.bukkitPing")
    private static String playerBukkitPing = "&8» &eBukkit&7: &f%ping%ms";

    @Message(name = "command.lag.player.kauriPing")
    private static String playerKauriPing = "&8» &eKauri&7: &f%ping%ms";

    @Message(name = "command.lag.player.positionalPing")
    private static String playerPositionalPing = "&8» &ePositional&7: &f%ping%ms";

    @Message(name = "command.lag.player.stability")
    private static String playerStability = "&6&lStability";

    @Message(name = "command.lag.player.lastPacketDrop")
    private static String playerPacketDrop = "&8» &eLast Packet Drop&7: &f%lastDrop% ago.";

    @Message(name = "command.lag.player.protocolVersion")
    private static String protocolVersion = "&8» &eMinecraft Version&7: &f%version% (%versionNumber%)";

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        if (args.length == 1) {
            sendServerInfo(sender);
        } else {
            switch (args[1].toLowerCase()) {
                case "profile": {
                    if(args.length > 2) {
                        switch(args[2].toLowerCase()) {
                            case "average":
                            case "avg": {
                                makePaste(sender, ResultsType.AVERAGE);
                                break;
                            }
                            case "tick": {
                                makePaste(sender, ResultsType.TICK);
                                break;
                            }
                            case "samples":
                            case "sample": {
                                makePaste(sender, ResultsType.SAMPLES);
                                break;
                            }
                            default: {
                                makePaste(sender, ResultsType.TOTAL);
                                break;
                            }
                        }
                    } else {
                        makePaste(sender, ResultsType.TOTAL);
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
                                sender.sendMessage("");
                                int protocol = TinyProtocolHandler.getProtocolVersion(player);
                                sender.sendMessage(Color.translate(protocolVersion.replace("%version%", ProtocolVersion.getVersion(protocol).getServerVersion()).replace("%versionNumber%", String.valueOf(protocol))));
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
        val results = Kauri.getInstance().getProfiler().results(ResultsType.SAMPLES);
        double totalMS = results.keySet().stream().mapToDouble(results::get).sum() / 1000000;
        sender.sendMessage(Color.translate(pctUsage.replace("%pct%", (totalMS / 50 * 100) + "")));
        float cps = Kauri.getInstance().getProfiler().totalCalls / (float) totalTime;
        sender.sendMessage(Color.translate(callsPS.replace("%calls%", String.valueOf(totalMS / totalTime / (50D / totalTime)))));
        sender.sendMessage(MiscUtils.line(lineColor));
    }

    private void makePaste(CommandSender sender, ResultsType type) {
        List<String> body = new ArrayList<>();
        body.add(MiscUtils.lineNoStrike());
        double total = 0;
        val results = Kauri.getInstance().getProfiler().results(type);


        for (String key : results.keySet().stream().sorted(Comparator.comparing(results::get, Comparator.reverseOrder())).collect(Collectors.toList())) {
            //Converting nanoseconds to millis to be more readable.
            double amount = results.get(key) / 1000000D;

            total+= amount;
            body.add(key + ": " + amount + "ms");
        }
        body.add(" ");
        body.add("Total: " + total + "ms");
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
    }
}
