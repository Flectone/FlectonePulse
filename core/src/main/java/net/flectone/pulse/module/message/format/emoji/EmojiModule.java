package net.flectone.pulse.module.message.format.emoji;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.emoji.listener.EmojiPulseListener;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;

@Singleton
public class EmojiModule extends AbstractModule {

    private final Message.Format.Emoji message;
    private final Permission.Message.Format.Emoji permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public EmojiModule(FileResolver fileResolver,
                       ListenerRegistry listenerRegistry) {
        this.message = fileResolver.getMessage().getFormat().getEmoji();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getEmoji();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        listenerRegistry.register(EmojiPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

}
