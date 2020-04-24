package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.handlers.ForgeHandler;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import dev.brighten.anticheat.Kauri;

import java.util.stream.Collectors;

@Init(commands = true)
public class UsersCommand {

    @Command(name = "kauri.users", description = "Shows the online users.", display = "users",
            permission = "kauri.command.users")
    public void onCommand(CommandAdapter cmd) {
        Kauri.INSTANCE.executor.execute(() -> {
            cmd.getSender().sendMessage(MiscUtils.line(Color.Dark_Gray));
            cmd.getSender().sendMessage(Color.Yellow + "Forge Users:");
            cmd.getSender().sendMessage(Kauri.INSTANCE.dataManager.dataMap.values().stream()
                    .filter(data -> data.modData != null)
                    .map(data -> data.getPlayer().getName())
                    .collect(Collectors.joining(Color.Gray + ", " + Color.White)));
            cmd.getSender().sendMessage("");
            cmd.getSender().sendMessage(Color.Yellow + "Lunar Client Users:");
            cmd.getSender().sendMessage(Kauri.INSTANCE.dataManager.dataMap.values().stream()
                    .filter(data -> data.usingLunar)
                    .map(data -> data.getPlayer().getName())
                    .collect(Collectors.joining(Color.Gray + ", " + Color.White)));
            cmd.getSender().sendMessage("");
            cmd.getSender().sendMessage(Color.Yellow + "Misc Users:");
            cmd.getSender().sendMessage(Kauri.INSTANCE.dataManager.dataMap.values().stream()
                    .filter(data -> data.modData == null && !data.usingLunar)
                    .map(data -> data.getPlayer().getName())
                    .collect(Collectors.joining(Color.Gray + ", " + Color.White)));
            cmd.getSender().sendMessage(MiscUtils.line(Color.Dark_Gray));
        });
    }
}
