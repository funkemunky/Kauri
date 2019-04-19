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

    @Message(name = "command.alerts.toggledAlerts")
    public static String toggledAlerts = "&7Toggled your alerts &f%enabled%&7.";

    @Message(name = "command.alerts.toggledDevAlerts")
    public static String toggledDevAlerts = "&7Toggled your developer alerts &f%enabled%&7.";
}
