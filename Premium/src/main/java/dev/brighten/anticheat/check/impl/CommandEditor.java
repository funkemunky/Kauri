package dev.brighten.anticheat.check.impl;

import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.reflections.types.WrappedField;
import cc.funkemunky.api.reflections.types.WrappedMethod;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.Priority;
import co.aikar.commands.annotation.CommandAlias;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Config;
import dev.brighten.anticheat.commands.KauriCommand;
import dev.brighten.api.KauriVersion;
import org.bukkit.plugin.PluginDescriptionFile;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.logging.Level;

@Init(priority = Priority.HIGH)
public class CommandEditor {
    public CommandEditor() {
        CommandAlias oldAnnotation = KauriCommand.class.getAnnotation(CommandAlias.class);
        CommandAlias alias = new CommandAlias() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return oldAnnotation.annotationType();
            }

            @Override
            public String value() {
                return String.join("|", ConfigAra.commandAlias);
            }
        };

        WrappedMethod method = new WrappedClass(Class.class).getMethod("getDeclaredAnnotationMap");

        Map<Class<? extends Annotation>, Annotation> annotations = method.invoke(KauriCommand.class);
        annotations.put(CommandAlias.class, alias);
    }
}
