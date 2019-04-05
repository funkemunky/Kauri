package cc.funkemunky.anticheat.api.utils;

import cc.funkemunky.api.utils.Init;

@Init
public class Messages {

    @Message(name = "command.errorData")
    public static String errorData = "&cThere was an error trying to find your data object.";

    @Message(name = "command.alerts.toggledAlerts")
    public static String toggledAlerts = "&7Toggled your alerts &f%enabled%&7.";

    @Message(name = "command.alerts.toggledDevAlerts")
    public static String toggledDevAlerts = "&7Toggled your developer alerts &f%enabled%&7.";
}
