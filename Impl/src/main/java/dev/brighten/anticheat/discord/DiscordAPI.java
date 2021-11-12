package dev.brighten.anticheat.discord;

import cc.funkemunky.api.utils.*;
import cc.funkemunky.api.utils.club.minnced.discord.webhook.WebhookClient;
import cc.funkemunky.api.utils.club.minnced.discord.webhook.WebhookClientBuilder;
import cc.funkemunky.api.utils.club.minnced.discord.webhook.send.WebhookEmbed;
import cc.funkemunky.api.utils.club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import cc.funkemunky.api.utils.club.minnced.discord.webhook.send.WebhookMessageBuilder;
import dev.brighten.api.check.DevStage;
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

    @ConfigSetting(path = "discord.send", name = "bans")
    private static boolean sendBans = true;

    @ConfigSetting(path = "discord.send", name = "alerts")
    private static boolean sendAlerts = false;

    @ConfigSetting(path = "discord.send", name = "startMsg")
    private static boolean startMsg = false;

    @ConfigSetting(path = "discord", name = "devAlerts")
    private static boolean devAlerts = false;

    public void load() {
        if(!enabled) {
            MiscUtils.printToConsole("&7Discord webhooks not enabled. Cancelling...");
            return;
        }
        load:
        {
            MiscUtils.printToConsole("&7Loading URL: &e%s", url);

            MiscUtils.printToConsole("&7Initializing webhook api...");
            client = new WebhookClientBuilder(url).setThreadFactory(job -> {
                Thread thread = new Thread(job);

                thread.setName("Discord Webhook Thread Factory");
                thread.setDaemon(true);

                return thread;
            }).setWait(true).build();

            author = new WebhookEmbed.EmbedAuthor("Kauri", "https://i.imgur.com/QkJPEor.jpg",
                    "https://i.imgur.com/QkJPEor.jpg");

            if(startMsg)
            client.send(new WebhookMessageBuilder().setUsername(author.getName()).setAvatarUrl(author.getIconUrl())
                    .setContent("Started webhook").build());
        }
        MiscUtils.printToConsole("&7Completed loading!");
    }

    public void unload() {
        MiscUtils.printToConsole("&cUnloading Discord Webhook...");
        if(client != null) {
            client.close();
            client = null;
        }
        lastDiscordSend.clear();
        client = null;
        author = null;
        enabled = false;
        MiscUtils.printToConsole("&7Completed!");
    }

    private static final int banRed = 0xD9471A, noBanOrange = 0xE8A83A, flagYellow = 0xFFEB33;

    public void sendBan(Player player, KauriCheck check, boolean exempt) {
        if(!enabled) return;
        if(!sendBans || client == null || (!devAlerts && check.getDevStage() != DevStage.STABLE)) return;

        lastDiscordSend.compute(player.getUniqueId(), (key, lastTime) -> {
            long now = System.currentTimeMillis();
            if(lastTime == null || now - lastTime > delay) {
                client.send(new WebhookEmbedBuilder().setAuthor(author)
                        .setColor(check.isExecutable() || exempt ? banRed : noBanOrange)
                        .setTitle(new WebhookEmbed.EmbedTitle("Kauri Ban", ""))
                        .setDescription(check.isExecutable() ?
                                (exempt ? "Player would have been banned but is exempted from banning."
                                        : "Player was banned by Kauri.")
                                : "Player would have been banned by Kauri but the check is not set to ban players.")
                        .addField(field("Player", player.getName()))
                        .addField(field("Detection", check.getName()))
                        .addField(field("Type", check.getCheckType().name()))
                        .addField(field("Description", check.getDescription())).build());

                return now;
            }
            return lastTime;
        });
    }

    public void sendFlag(Player player, KauriCheck check, boolean dev, float vl) {
        if(!enabled) return;
        if(!sendAlerts || client == null || (!devAlerts && dev)) return;
        lastDiscordSend.compute(player.getUniqueId(), (key, lastTime) -> {
            long now = System.currentTimeMillis();
            if(lastTime == null || now - lastTime > delay) {
                client.send(new WebhookEmbedBuilder().setAuthor(author).setColor(flagYellow)
                        .setTitle(new WebhookEmbed.EmbedTitle("Kauri Flag", ""))
                        .addField(field("Player", player.getName()))
                        .addField(field("Detection", check.getName()))
                        .addField(field("Violation", MathUtils.round(vl, 1) + "/" + check.getPunishVl()))
                        .addField(field("Description", check.getDescription()))
                        .setFooter(new WebhookEmbed.EmbedFooter(check.getCheckType().name(), "")).build());

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
