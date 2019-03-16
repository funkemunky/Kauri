package cc.funkemunky.anticheat.impl.commands;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.utils.ItemBuilder;
import cc.funkemunky.anticheat.api.utils.VPNResponse;
import cc.funkemunky.anticheat.api.utils.menu.button.Button;
import cc.funkemunky.anticheat.api.utils.menu.type.impl.ChestMenu;
import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Init(commands = true)
public class AntiVPNCommand {

    @ConfigSetting(name = "antivpn.privacyMode.enabled")
    private boolean privacyMode = false;

    @ConfigSetting(name = "antivpn.privacyMode.overrideForPermission")
    private boolean override = false;

    @ConfigSetting(name = "antivpn.privacyMode.overridePermission")
    private String overridePerm = "kauri.antivpn.privacyOverride";


    @Command(name = "antivpn", description = "Antivpn command.", permission = "kauri.antivpn")
    public void onCommandMain(CommandAdapter args) {
        args.getSender().sendMessage(MiscUtils.line(Color.Dark_Gray));
        args.getSender().sendMessage(Color.Gold + Color.Bold + "AntiVPN Help");
        args.getSender().sendMessage(Color.translate("&7/%label% &freload".replaceAll("%label%", args.getLabel().toLowerCase())));
        args.getSender().sendMessage(Color.translate("&7/%label% &fwhitelist <player>".replaceAll("%label%", args.getLabel().toLowerCase())));
        args.getSender().sendMessage(Color.translate("&7/%label% &fanalyze <player>".replaceAll("%label%", args.getLabel().toLowerCase())));
        args.getSender().sendMessage(MiscUtils.line(Color.Dark_Gray));
    }

    @Command(name = "antivpn.analyze", playerOnly = true, description = "Analyze a player's connection", permission = "kauri.antivpn.analyze", aliases = {"antivpn.info", "antivpn.view", "kauri.antivpn.analyze", "kauri.antivpn.info", "kauri.antivpn.view"})
    public void onCommandAnalyze(CommandAdapter args) {
        Player player = Bukkit.getPlayer(args.getArgs()[0]);

        if (player == null || !player.isOnline()) {
            args.getSender().sendMessage(Color.Red + "The player \"" + args.getArgs()[1] + "\" is not online!");
            return;
        }

        VPNResponse apiResponse = Kauri.getInstance().getVpnUtils().getResponse(player);

        ChestMenu menu = new ChestMenu(player.getName() + "'s IP Information", 1);

        boolean showInfo = !privacyMode || (override && args.getSender().hasPermission(overridePerm));
        if (apiResponse.isStatus()) {
            menu.setItem(1, getButton(Color.Aqua + "Using VPN", Material.REDSTONE, Color.Gray + apiResponse.isUsingProxy()));
           // menu.setItem(3, getButton(Color.Aqua + "Location", Material.MAP, Color.Gray + (!showInfo ? "[redacted]" : apiResponse.getCity() + ", " + apiResponse.getCountryName())));
            //menu.setItem(5, getButton(Color.Aqua + "HostName", Material.WATCH, Color.Gray + (!showInfo ? "[redacted]" : apiResponse.getHostName())));
            //menu.setItem(7, getButton(Color.Aqua + "ISP", Material.PAPER, Color.Gray + (!showInfo ? "[redacted]" : apiResponse.getISP())));
        } else {
            for (int i = 0; i < 9; i++) {
                menu.addItem(getButton(Color.Red + Color.Bold + "FAILED", Material.REDSTONE_BLOCK, ""));
            }
        }

        menu.showMenu(args.getPlayer());
    }

    private Button getButton(String key, Material material, String... lore) {

        ItemStack itemStack = new ItemBuilder(material).name(Color.Aqua + key).lore(lore).build();

        return new Button(false, itemStack);
    }
}
