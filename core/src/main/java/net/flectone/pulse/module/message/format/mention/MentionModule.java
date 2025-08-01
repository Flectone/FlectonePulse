package net.flectone.pulse.module.message.format.mention;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.format.mention.listener.MentionPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;

import java.util.UUID;
import java.util.WeakHashMap;

@Singleton
public class MentionModule extends AbstractModuleLocalization<Localization.Message.Format.Mention> {

    private final WeakHashMap<UUID, Boolean> processedMentions = new WeakHashMap<>();

    private final Message.Format.Mention message;
    private final Permission.Message.Format.Mention permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public MentionModule(FileResolver fileResolver,
                         ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getFormat().getMention());

        this.message = fileResolver.getMessage().getFormat().getMention();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getMention();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        registerPermission(permission.getGroup());
        registerPermission(permission.getBypass());

        listenerRegistry.register(MentionPulseListener.class);
    }

    @Override
    public void onDisable() {
        processedMentions.clear();
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public void sendMention(UUID processId, FPlayer fPlayer) {
        if (processedMentions.containsKey(processId)) return;

        processedMentions.put(processId, true);

        playSound(fPlayer);

        builder(fPlayer)
                .destination(message.getDestination())
                .format(Localization.Message.Format.Mention::getPerson)
                .sound(null)
                .sendBuilt();
    }
}
