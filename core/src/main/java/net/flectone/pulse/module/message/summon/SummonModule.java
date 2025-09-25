package net.flectone.pulse.module.message.summon;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.summon.listener.SummonPulseListener;
import net.flectone.pulse.module.message.summon.model.SummonMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

@Singleton
public class SummonModule extends AbstractModuleLocalization<Localization.Message.Summon> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public SummonModule(FileResolver fileResolver,
                        ListenerRegistry listenerRegistry) {
        super(MessageType.SUMMON);

        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(SummonPulseListener.class);
    }

    @Override
    public Message.Summon config() {
        return fileResolver.getMessage().getSummon();
    }

    @Override
    public Permission.Message.Summon permission() {
        return fileResolver.getPermission().getMessage().getSummon();
    }

    @Override
    public Localization.Message.Summon localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getSummon();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, FEntity target) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(SummonMetadata.<Localization.Message.Summon>builder()
                .sender(fPlayer)
                .range(config().getRange())
                .format(Localization.Message.Summon::getFormat)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .target(target)
                .translationKey(translationKey)
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, target)})
                .build()
        );
    }
}
