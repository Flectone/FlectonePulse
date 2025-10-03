package net.flectone.pulse.module.message.locate;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.locate.listener.LocatePulseListener;
import net.flectone.pulse.module.message.locate.model.Locate;
import net.flectone.pulse.module.message.locate.model.LocateMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import org.apache.commons.lang3.StringUtils;

@Singleton
public class LocateModule extends AbstractModuleLocalization<Localization.Message.Locate> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public LocateModule(FileResolver fileResolver,
                        ListenerRegistry listenerRegistry) {
        super(MessageType.LOCATE);

        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(LocatePulseListener.class);
    }

    @Override
    public Message.Locate config() {
        return fileResolver.getMessage().getLocate();
    }

    @Override
    public Permission.Message.Locate permission() {
        return fileResolver.getPermission().getMessage().getLocate();
    }

    @Override
    public Localization.Message.Locate localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getLocate();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Locate locate) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(LocateMetadata.<Localization.Message.Locate>builder()
                .sender(fPlayer)
                .range(config().getRange())
                .format(localization -> StringUtils.replaceEach(
                        switch (translationKey) {
                            case COMMANDS_LOCATE_BIOME_SUCCESS, COMMANDS_LOCATEBIOME_SUCCESS -> localization.getBiome();
                            case COMMANDS_LOCATE_POI_SUCCESS -> localization.getPoi();
                            case COMMANDS_LOCATE_STRUCTURE_SUCCESS, COMMANDS_LOCATE_SUCCESS -> localization.getStructure();
                            default -> "";
                        },
                        new String[]{"<value>", "<x>", "<y>", "<z>", "<blocks>"},
                        new String[]{locate.value(), locate.x(), locate.y(), locate.z(), locate.blocks()}
                ))
                .locate(locate)
                .translationKey(translationKey)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}