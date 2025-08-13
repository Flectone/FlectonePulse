package net.flectone.pulse.module.message.format.moderation.swear;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.format.moderation.swear.listener.SwearPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Singleton
public class SwearModule extends AbstractModuleLocalization<Localization.Message.Format.Moderation.Swear> {

    private final Cache<String, String> messageCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(100000)
            .build();

    private final Message.Format.Moderation.Swear message;
    private final Permission.Message.Format.Moderation.Swear permission;
    private final FLogger fLogger;
    private final ListenerRegistry listenerRegistry;
    private final PermissionChecker permissionChecker;

    @Getter private Pattern combinedPattern;

    @Inject
    public SwearModule(FileResolver fileResolver,
                       FLogger fLogger,
                       ListenerRegistry listenerRegistry,
                       PermissionChecker permissionChecker) {
        super(localization -> localization.getMessage().getFormat().getModeration().getSwear());

        this.message = fileResolver.getMessage().getFormat().getModeration().getSwear();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getModeration().getSwear();
        this.fLogger = fLogger;
        this.listenerRegistry = listenerRegistry;
        this.permissionChecker = permissionChecker;
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
    public void onDisable() {
        messageCache.invalidateAll();
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public String cacheReplace(FEntity sender, String string) {
        if (StringUtils.isEmpty(string)) return string;

        try {
            return messageCache.get(string, () -> replace(sender, string));
        } catch (ExecutionException e) {
            fLogger.warning(e);
        }

        return replace(sender, string);
    }

    private String replace(FEntity sender, String string) {
        if (permissionChecker.check(sender, permission.getBypass())) return string;
        if (combinedPattern == null) return string;

        StringBuilder result = new StringBuilder();
        Matcher matcher = combinedPattern.matcher(string);
        while (matcher.find()) {
            String word = matcher.group(0);
            if (word != null && message.getIgnore().contains(word.trim().toLowerCase())) continue;

            matcher.appendReplacement(result, "<swear:'" + word + "'>");
        }

        matcher.appendTail(result);

        return result.toString();
    }
}
