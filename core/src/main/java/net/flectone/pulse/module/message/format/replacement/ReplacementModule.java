package net.flectone.pulse.module.message.format.replacement;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.replacement.listener.ReplacementPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public class ReplacementModule extends AbstractModule {

    private final Map<String, String> replacements = new HashMap<>();

    private final Message.Format.Replacement message;
    private final Permission.Message.Format.Replacement permission;
    private final ListenerRegistry listenerRegistry;

    private Pattern pattern;

    @Inject
    public ReplacementModule(FileResolver fileResolver,
                             ListenerRegistry listenerRegistry) {
        this.message = fileResolver.getMessage().getFormat().getReplacement();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getReplacement();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        listenerRegistry.register(ReplacementPulseListener.class);

        for (Message.Format.Replacement.ReplacementValue r : message.getValues()) {
            replacements.put(r.getTrigger(), "<replacement:'" + r.getTrigger() + "'>");
        }

        String regex = "(?<!\\\\)(?<![^\\s\\p{Punct}])(?i)(" +
                replacements.keySet().stream()
                        .map(Pattern::quote)
                        .collect(Collectors.joining("|")) +
                ")(?![^\\s\\p{Punct}])";

        pattern = Pattern.compile(regex);
    }

    @Override
    public void onDisable() {
        replacements.clear();
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public String processMessage(String message) {
        if (StringUtils.isEmpty(message)) return message;

        StringBuilder stringBuilder = new StringBuilder();
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            String trigger = matcher.group(1).toLowerCase();
            matcher.appendReplacement(stringBuilder, replacements.get(trigger));
        }

        matcher.appendTail(stringBuilder);

        return stringBuilder.toString();
    }

}
