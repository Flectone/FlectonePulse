package net.flectone.pulse.module.message.format.moderation.swear;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.format.moderation.swear.listener.SwearPulseListener;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.util.logging.FLogger;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Singleton
public class SwearModule extends AbstractModuleLocalization<Localization.Message.Format.Moderation.Swear> {

    private final Message.Format.Moderation.Swear message;
    private final Permission.Message.Format.Moderation.Swear permission;
    private final FLogger fLogger;
    private final ListenerRegistry listenerRegistry;

    @Getter private Pattern combinedPattern;

    @Inject
    public SwearModule(FileResolver fileResolver,
                       FLogger fLogger,
                       ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getFormat().getModeration().getSwear());

        this.message = fileResolver.getMessage().getFormat().getModeration().getSwear();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getModeration().getSwear();
        this.fLogger = fLogger;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        registerPermission(permission.getBypass());
        registerPermission(permission.getSee());

        try {
            combinedPattern = Pattern.compile(String.join("|", this.message.getTrigger()));
        } catch (PatternSyntaxException e) {
            fLogger.warning(e);
        }

        listenerRegistry.register(SwearPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }
}
