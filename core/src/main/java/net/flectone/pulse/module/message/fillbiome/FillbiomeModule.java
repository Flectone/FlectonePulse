package net.flectone.pulse.module.message.fillbiome;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.fillbiome.listener.FillbiomePulseListener;
import net.flectone.pulse.module.message.fillbiome.model.Fillbiome;
import net.flectone.pulse.module.message.fillbiome.model.FillbiomeMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import org.apache.commons.lang3.StringUtils;

@Singleton
public class FillbiomeModule extends AbstractModuleLocalization<Localization.Message.Fillbiome> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public FillbiomeModule(FileResolver fileResolver,
                           ListenerRegistry listenerRegistry) {
        super(MessageType.FILLBIOME);

        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(FillbiomePulseListener.class);
    }

    @Override
    public Message.Fillbiome config() {
        return fileResolver.getMessage().getFillbiome();
    }

    @Override
    public Permission.Message.Fillbiome permission() {
        return fileResolver.getPermission().getMessage().getFillbiome();
    }

    @Override
    public Localization.Message.Fillbiome localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getFillbiome();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Fillbiome fillbiome) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(FillbiomeMetadata.<Localization.Message.Fillbiome>builder()
                .sender(fPlayer)
                .range(config().getRange())
                .format(s -> StringUtils.replaceEach(
                        translationKey == MinecraftTranslationKey.COMMANDS_FILLBIOME_SUCCESS ? s.getFormat() : s.getFormatCount(),
                        new String[]{"<blocks>", "<x1>", "<y1>", "<z1>", "<x2>", "<y2>", "<z2>"},
                        new String[]{StringUtils.defaultString(fillbiome.count()), fillbiome.x1(), fillbiome.y1(), fillbiome.z1(), fillbiome.x2(), fillbiome.y2(), fillbiome.z2()}
                ))
                .fillbiome(fillbiome)
                .translationKey(translationKey)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}