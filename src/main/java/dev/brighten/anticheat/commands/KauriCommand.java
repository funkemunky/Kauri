package dev.brighten.anticheat.commands;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.utils.GeneralHash;
import dev.brighten.anticheat.utils.MiscUtils;

import java.io.*;
import java.lang.reflect.Method;

@Init(commands = true)
public class KauriCommand {

    @Command(name = "kauri", description = "The Kauri main command.", display = "Kauri", aliases = {"anticheat"},
            permission = "kauri.command")
    public void onCommand(CommandAdapter cmd) {
        Atlas.getInstance().getCommandManager().runHelpMessage(cmd,
                cmd.getSender(),
                Atlas.getInstance().getCommandManager().getDefaultScheme());
    }

    @Command(name = "kchecksum", permission = "kauri.admin.checksum")
    public void onChecksum(CommandAdapter cmd) {
        /*Method c = J;
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
        }*/
    }
}
