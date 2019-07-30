package cc.funkemunky.anticheat.api.utils;

import cc.funkemunky.api.utils.Init;

@Init
public class Messages {

    @Message(name = "command.error.data")
    public static String errorData = "&cThere was an error trying to find your data object.";

    @Message(name = "command.error.playerOffline")
    public static String errorPlayerOffline = "&cThat player seems to be offline. Check your spelling!";

    @Message(name = "command.error.invalidArguments.normal")
    public static String invalidArguments = "&cThe inputted arguments are invalid. Please check the help page for more information.";

    @Message(name = "command.error.invalidArguments.integer")
    public static String invalidArgumentsInteger = "&cThe inputted argument must be in the form of a number.";

    @Message(name = "command.error.playerDoesntExist")
    public static String playerDoesntExist = "&cThat player could not be found in Mojang's databases. Check your spelling!";

    @Message(name = "command.error.checkDoesntExist")
    public static String checkDoesntExist = "&cThe check \"%check%\" does not exist.";

    @Message(name = "command.error.playersOnly")
    public static String playersOnly = "&cYou must be a player to run this command.";

    @Message(name = "command.alerts.toggledAlerts")
    public static String toggledAlerts = "&7Toggled your alerts &f%enabled%&7.";

    @Message(name = "command.alerts.setTier")
    public static String setTierAlerts = "&7Your alerts tier is set to &f%tier%&7.";

    @Message(name = "command.alerts.toggledDevAlerts")
    public static String toggledDevAlerts = "&7Toggled your developer alerts &f%enabled%&7.";

    @Message(name = "field.alertTiers.certain")
    public static String certain = "Certain";

    @Message(name = "field.alertTiers.high")
    public static String high = "High";

    @Message(name = "field.alertTiers.likely")
    public static String likely = "Likely";

    @Message(name = "field.alertTiers.possible")
    public static String possible = "Possible";

    @Message(name = "field.alertTiers.low")
    public static String low = "Low";
}
