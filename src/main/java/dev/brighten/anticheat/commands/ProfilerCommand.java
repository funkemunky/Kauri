package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.profiling.ResultsType;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.utils.Pastebin;
import org.apache.commons.lang.time.DateFormatUtils;
import org.bukkit.command.CommandSender;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

@Init(commands = true)
public class ProfilerCommand {

    @Command(name = "kauri.profile", description = "run a profile on Kauri.", permission = {"kauri.profile"})
    public void onCommand(CommandAdapter cmd) {
        if(cmd.getArgs().length > 0) {
            switch(cmd.getArgs()[0].toLowerCase()) {
                case "average":
                case "avg": {
                    makePaste(cmd.getSender(), ResultsType.AVERAGE);
                    break;
                }
                case "tick": {
                    makePaste(cmd.getSender(), ResultsType.TICK);
                    break;
                }
                case "samples":
                case "sample": {
                    makePaste(cmd.getSender(), ResultsType.SAMPLES);
                    break;
                }
                case "toggle": {
                    Kauri.INSTANCE.profiler.enabled = !Kauri.INSTANCE.profiler.enabled;

                    if(!Kauri.INSTANCE.profiler.enabled) {
                        Kauri.INSTANCE.profiler.reset();
                    }

                    cmd.getSender().sendMessage(Color.Green + "Set profile status to: " + Kauri.INSTANCE.profiler.enabled);
                    break;
                }
                default: {
                    makePaste(cmd.getSender(), ResultsType.TOTAL);
                    break;
                }
            }
        } else {
            makePaste(cmd.getSender(), ResultsType.TOTAL);
        }
    }

    private void makePaste(CommandSender sender, ResultsType type) {
        List<String> body = new ArrayList<>();
        body.add(MiscUtils.lineNoStrike());
        double total = 0;
        Map<String, Double> results = Kauri.INSTANCE.profiler.results(type);


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
