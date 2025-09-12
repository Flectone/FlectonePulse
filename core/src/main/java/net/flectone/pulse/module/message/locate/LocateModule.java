package net.flectone.pulse.module.message.locate;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
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

    private final Message.Locate message;
    private final Permission.Message.Locate permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public LocateModule(FileResolver fileResolver,
                        ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getLocate(), MessageType.LOCATE);

        this.message = fileResolver.getMessage().getLocate();
        this.permission = fileResolver.getPermission().getMessage().getLocate();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(LocatePulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Locate locate) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(LocateMetadata.<Localization.Message.Locate>builder()
                .sender(fPlayer)
                .filterPlayer(fPlayer)
                .format(s -> StringUtils.replaceEach(
                        switch (translationKey) {
                            case COMMANDS_LOCATE_BIOME_SUCCESS -> s.getBiome();
                            case COMMANDS_LOCATE_POI_SUCCESS -> s.getPoi();
                            case COMMANDS_LOCATE_STRUCTURE_SUCCESS -> s.getStructure();
                            default -> "";
                        },
                        new String[]{"<name>", "<x>", "<y>", "<z>", "<blocks>"},
                        new String[]{locate.name(), locate.x(), locate.y(), locate.z(), locate.blocks()}
                ))
                .locate(locate)
                .translationKey(translationKey)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}