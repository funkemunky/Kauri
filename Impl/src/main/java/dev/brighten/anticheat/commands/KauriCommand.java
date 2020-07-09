package dev.brighten.anticheat.commands;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Init(commands = true)
public class KauriCommand {

    private static List<Player> testers = new ArrayList<>();

    @Command(name = "kauri", description = "The Kauri main command.", display = "Kauri", aliases = {"anticheat"},
            permission = "kauri.command",
            noPermissionMessage = "&cThis server is running Kauri by funkemunky, Elevated, and Abigail.")
    public void onCommand(CommandAdapter cmd) {
        Atlas.getInstance().getCommandManager(Kauri.INSTANCE).runHelpMessage(cmd,
                cmd.getSender(),
                Atlas.getInstance().getCommandManager(Kauri.INSTANCE).getDefaultScheme());
    }

    @Command(name = "kauri.test", description = "Add yourself to test messaging.",
            permission = "kauri.command.test", display = "test", playerOnly = true)
    public void onTest(CommandAdapter cmd) {
        if(testers.contains(cmd.getPlayer())) {
            if(testers.remove(cmd.getPlayer())) {
                cmd.getSender().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                        .msg("tester-remove-success", "&cRemoved you from test messaging for developers."));
            } else cmd.getSender().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                    .msg("tester-remove-error", "&cThere was an error removing you from test messaging."));
        } else {
            testers.add(cmd.getPlayer());
            cmd.getSender().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                    .msg("testers-added", "&aYou have been added to the test messaging list for developers."));
        }
    }

    @Command(name = "kauri.updatecmds")
    public void onUpdate(CommandAdapter cmd) {
        new WrappedClass(cmd.getPlayer().getClass()).getMethod("updateCommands").invoke(cmd.getPlayer());
        cmd.getSender().sendMessage("updated");
    }

   /* @Command(name = "kchecksum", permission = "kauri.command.admin.checksum")
    public void onChecksum(CommandAdapter cmd) {
        Method c = J;
        String className = c.getName();
        String classAsPath = ;
        InputStream stream = c.getClassLoader().getResourceAsStream(classAsPath);

        try {
            byte[] array = MiscUtils.toByteArray(stream);

            String hash = GeneralHash.getSHAHash(array, GeneralHash.SHAType.SHA1);

            cmd.getSender().sendMessage("Checksum: " + hash);
            System.out.println("checksum: " + hash);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

   public static List<Player> getTesters() {
       testers.stream().filter(Objects::isNull).forEach(testers::remove);

       return testers;
   }
}
