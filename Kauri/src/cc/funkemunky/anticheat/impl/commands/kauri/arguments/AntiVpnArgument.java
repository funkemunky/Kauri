package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.utils.ItemBuilder;
import cc.funkemunky.anticheat.api.utils.VPNResponse;
import cc.funkemunky.anticheat.api.utils.menu.button.Button;
import cc.funkemunky.anticheat.api.utils.menu.type.impl.ChestMenu;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AntiVpnArgument extends FunkeArgument {
    @ConfigSetting(path = "antivpn.privacyMode", name = "enabled")
    private boolean privacyMode = false;

    @ConfigSetting(path = "antivpn.privacyMode", name = "overrideForPermission")
    private boolean override = false;

    @ConfigSetting(path = "antivpn.privacyMode", name = "overridePermission")
    private String overridePerm = "kauri.antivpn.privacyOverride";

    public AntiVpnArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);

        setPlayerOnly(true);
        addTabComplete(2, "analyze");
    }

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        if (args.length == 1) {
            sender.sendMessage(MiscUtils.line(Color.Dark_Gray));
            sender.sendMessage(Color.Gold + Color.Bold + "AntiVPN Help");
            sender.sendMessage(Color.translate("&7/%label% &fanalyze <player>".replaceAll("%label%", command.getLabel().toLowerCase())));
            sender.sendMessage(MiscUtils.line(Color.Dark_Gray));
        } else if (args[1].equalsIgnoreCase("analyze")) {
            Player player = Bukkit.getPlayer(args[2]);

            if (player == null || !player.isOnline()) {
                sender.sendMessage(Color.Red + "The player \"" + args[2] + "\" is not online!");
                return;
            }

            VPNResponse apiResponse = Kauri.getInstance().getVpnUtils().getResponse(player);

            ChestMenu menu = new ChestMenu(player.getName() + "'s IP Information", 1);

            boolean showInfo = !privacyMode || (override && sender.hasPermission(overridePerm));
            if (apiResponse != null && apiResponse.isSuccess()) {
                menu.setItem(1, getButton(Color.Aqua + "Using VPN", Material.REDSTONE, Color.Gray + apiResponse.isProxy()));
                menu.setItem(3, getButton(Color.Aqua + "Location", Material.MAP, Color.Gray + (!showInfo ? "[redacted]" : apiResponse.getCity() + ", " + apiResponse.getCountryName())));
                menu.setItem(5, getButton(Color.Aqua + "IP", Material.WATCH, Color.Gray + (!showInfo ? "[redacted]" : apiResponse.getIp())));
                menu.setItem(7, getButton(Color.Aqua + "ISP", Material.PAPER, Color.Gray + (!showInfo ? "[redacted]" : apiResponse.getIsp())));
            } else {
                for (int i = 0; i < 9; i++) {
                    menu.addItem(getButton(Color.Red + Color.Bold + "FAILED", Material.REDSTONE_BLOCK, ""));
                }
            }

            menu.showMenu((Player) sender);
        }
    }

    private Button getButton(String key, Material material, String... lore) {

        ItemStack itemStack = new ItemBuilder(material).name(Color.Aqua + key).lore(lore).build();

        return new Button(false, itemStack);
    }
}
