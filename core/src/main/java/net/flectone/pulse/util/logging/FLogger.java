package net.flectone.pulse.util.logging;

import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

@Singleton
public class FLogger extends Logger {

    private final String PLUGIN_NAME = "\033[0;34m(FlectonePulse) \033[0m";

    private final List<String> PLUGIN_INFO = List.of(
            " \033[0;34m ___       ___  __  ___  __        ___ \033[0m",
            " \033[0;34m|__  |    |__  /  `  |  /  \\ |\\ | |__  \033[0m",
            " \033[0;34m|    |___ |___ \\__,  |  \\__/ | \\| |___ \033[0m",
            " \033[0;34m __             __   ___ \033[0;96m                       \033[0m",
            " \033[0;34m|__) |  | |    /__` |__  \033[0;96m                   \033[0m",
            " \033[0;34m|    \\__/ |___ .__/ |___\033[0;96m   /\\         \033[0m",
            " \033[0;96m                          /  \\ v<version>               \033[0m",
            " \033[0;96m__/\\___  ____/\\_____  ___/    \\______ \033[0m",
            " \033[0;96m       \\/           \\/  \033[0m"
    );

    private final Consumer<LogRecord> logConsumer;

    private LogFilter logFilter;

    public FLogger(Consumer<LogRecord> logConsumer) {
        super("", null);

        this.logConsumer = logConsumer;
    }

    public FLogger(Logger logger) {
        super("", null);

        logger.setLevel(Level.OFF);
        setParent(logger);
        setLevel(Level.ALL);

        logConsumer = super::log;
    }

    public void enableFilter() {
        this.logFilter = new LogFilter();
    }

    public void reload(List<String> messages) {
        if (logFilter == null) return;

        logFilter.getMessages().clear();
        logFilter.getMessages().addAll(messages);
    }

    @Override
    public void log(LogRecord logRecord) {
        String colorLog = switch (logRecord.getLevel().intValue()) {
            // warn
            case 900 -> "\033[0;93m";
            // info
            case 800 -> "\033[0;96m";

            default -> "";
        };

        logRecord.setMessage(PLUGIN_NAME + colorLog + logRecord.getMessage() + "\033[0m");

        logConsumer.accept(logRecord);
    }

    @Override
    public void info(String msg) {
        log(buildLogRecord(Level.INFO, msg));
    }

    public void logEnabling() {
        info("Enabling...");
    }

    public void logEnabled() {
        info("FlectonePulse v" + BuildConfig.PROJECT_VERSION + " enabled");
    }

    public void logDisabling() {
        info("Disabling...");
    }

    public void logDisabled() {
        info("FlectonePulse v" + BuildConfig.PROJECT_VERSION + " disabled");
    }

    public void logReloading() {
        info("Reloading...");
    }

    public void logReloaded() {
        info("FlectonePulse v" + BuildConfig.PROJECT_VERSION + " reloaded");
    }

    public void logPluginInfo() {
        PLUGIN_INFO.forEach(string -> {
            string = string.replace("<version>", BuildConfig.PROJECT_VERSION);
            info(string);
        });
    }

    public void info(Component component) {
        info(PlainTextComponentSerializer.plainText().serialize(component));
    }

    public void warning(Exception e) {
        LogRecord warn = new LogRecord(Level.WARNING, "An error occurred, report it to https://github.com/Flectone/FlectonePulse/issues");
        warn.setThrown(e);

        logConsumer.accept(warn);
    }

    public void warningTree(Component component) {
        warningTree(component, 0);
    }

    private void warningTree(Component component, int indentLevel) {
        String indent = "| ".repeat(indentLevel);

        warning(indent + "|- Component: " + component.getClass().getSimpleName());

        if (component instanceof TranslatableComponent translatable) {
            warning(indent + "| Key: " + translatable.key());
            warning(indent + "|= Arguments:");
            for (TranslationArgument arg : translatable.arguments()) {
                if (arg.value() instanceof Component argComp) {
                    warningTree(argComp, indentLevel + 1);
                } else {
                    warning(indent + "| " + arg.value());
                }
            }
        } else if (component instanceof TextComponent textComponent) {
            warning(indent + "| Content: " + textComponent.content());
        }

        Style style = component.style();
        warning(indent + "| Style:");
        warning(indent + "| | Bold: " + style.decoration(TextDecoration.BOLD));
        warning(indent + "| | Italic: " + style.decoration(TextDecoration.ITALIC));
        warning(indent + "| | Underlined: " + style.decoration(TextDecoration.UNDERLINED));
        warning(indent + "| | Strikethrough: " + style.decoration(TextDecoration.STRIKETHROUGH));
        warning(indent + "| | Obfuscated: " + style.decoration(TextDecoration.OBFUSCATED));
        warning(indent + "| | Color: " + style.color());

        ClickEvent clickEvent = style.clickEvent();
        if (clickEvent != null) {
            warning(indent + "| ClickEvent:");
            warning(indent + "| | Action: " + clickEvent.action());
            warning(indent + "| | Value: " + clickEvent.value());
        }

        HoverEvent<?> hoverEvent = style.hoverEvent();
        if (hoverEvent != null) {
            warning(indent + "| HoverEvent:");
            warning(indent + "| | Action: " + hoverEvent.action());

            warning(indent + "| | Value: " + hoverEvent.value());
        }

        if (!component.children().isEmpty()) {
            warning(indent + "| Children from : " + component);
            for (Component child : component.children()) {
                warningTree(child, indentLevel + 1);
            }
        }
    }

    private LogRecord buildLogRecord(Level level, String message) {
        LogRecord logRecord = new LogRecord(level, message);
        logRecord.setLoggerName("");
        return logRecord;
    }
}
