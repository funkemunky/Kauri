package dev.brighten.anticheat.discord;

import cc.funkemunky.api.utils.*;
import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import dev.brighten.api.check.KauriCheck;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Init(priority = Priority.LOWEST)
public class DiscordAPI {

    public static DiscordAPI INSTANCE;
    private static Pattern urlCheck;

    private WebhookClient client;
    private WebhookEmbed.EmbedAuthor author;
    private final Map<UUID, Long> lastDiscordSend = new HashMap<>();

    public DiscordAPI() {
        INSTANCE = this;
    }

    @ConfigSetting(path = "discord", name = "webhookUrl")
    private static String url = "Insert URL here";

    @ConfigSetting(path = "discord", name = "enabled")
    private static boolean enabled = false;

    @ConfigSetting(path = "discord", name = "msgDelayInMillis")
    private static long delay = 2000L;

    @ConfigSetting(path = "discord", name = "devAlerts")
    private static boolean devAlerts = false;

    public void load() {
        if(!enabled) {
            MiscUtils.printToConsole("&7Discord webhooks not enabled. Cancelling...");
            return;
        }
        load:
        {
            MiscUtils.printToConsole("&7Checking for valid url...");

            if(!urlCheck.matcher(url).matches()) {
                MiscUtils.printToConsole("&7URL &e%s &7not valid! Stopping Discord webhook loading process...",
                        url);
                break load;
            }

            MiscUtils.printToConsole("&7Initializing webhook api...");
            client = new WebhookClientBuilder(url).setThreadFactory(job -> {
                Thread thread = new Thread(job);

                thread.setName("KauriDiscord");
                thread.setDaemon(true);

                return thread;
            }).setWait(true).build();

            author = new WebhookEmbed.EmbedAuthor("Kauri", "https://i.imgur.com/QkJPEor.jpg",
                    "https://i.imgur.com/QkJPEor.jpg");
        }
        MiscUtils.printToConsole("&7Completed loading!");
    }

    public void unload() {
        MiscUtils.printToConsole("&cUnloading Discord Webhook...");
        client.close();
        client = null;
        MiscUtils.printToConsole("&7Completed!");
    }

    private static final int banRed = 0xD9471A, noBanOrange = 0xE8A83A, flagYellow = 0xFFEB33;

    public void sendBan(Player player, KauriCheck check, boolean exempt) {
        if(client == null || (!devAlerts && check.isDeveloper())) return;

        lastDiscordSend.compute(player.getUniqueId(), (key, lastTime) -> {
            long now = System.currentTimeMillis();
            if(lastTime == null || now - lastTime > delay) {
                client.send(new WebhookEmbedBuilder().setAuthor(author)
                        .setColor(check.isExecutable() || exempt ? banRed : noBanOrange)
                        .setTitle(new WebhookEmbed.EmbedTitle(player.getName(), ""))
                        .setDescription(check.isExecutable() ?
                                (exempt ? "Player would have been banned but is exempted from banning."
                                        : "Player was banned by Kauri.")
                                : "Player would have been banned by Kauri but the check is not set to ban players.")
                        .addField(field("Detection", check.getName()))
                        .addField(field("Type", check.getCheckType().name()))
                        .addField(field("Descrption", check.getDescription())).build());

                return now;
            }
            return lastTime;
        });
    }

    public void sendFlag(Player player, KauriCheck check, boolean dev, float vl) {
        if(client == null || (!devAlerts && dev)) return;
        lastDiscordSend.compute(player.getUniqueId(), (key, lastTime) -> {
            long now = System.currentTimeMillis();
            if(lastTime == null || now - lastTime > delay) {
                client.send(new WebhookEmbedBuilder().setAuthor(author).setColor(flagYellow)
                        .setDescription("Player has flagged a check on Kauri.")
                        .setTitle(new WebhookEmbed.EmbedTitle(player.getName(), ""))
                        .addField(field("Player", player.getName()))
                        .addField(field("Detection", check.getName()))
                        .addField(field("Violation", MathUtils.round(vl, 1) + "/" + check.getPunishVl()))
                        .addField(field("Type", check.getCheckType().name()))
                        .addField(field("Descrption", check.getDescription())).build());

                return now;
            }
            return lastTime;
        });
    }

    private static WebhookEmbed.EmbedField field(String key, String value) {
        return field(false, key, value);
    }

    private static WebhookEmbed.EmbedField field(boolean inline, String key, String value) {
        return new WebhookEmbed.EmbedField(inline, key, value);
    }

    static {
        urlCheck = Pattern.compile("^http|https+://discord\\.com/api/webhooks.*");
    }
}
