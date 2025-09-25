package net.flectone.pulse.module.message.teleport;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.teleport.listener.TeleportPulseListener;
import net.flectone.pulse.module.message.teleport.model.TeleportEntity;
import net.flectone.pulse.module.message.teleport.model.TeleportEntityMetadata;
import net.flectone.pulse.module.message.teleport.model.TeleportLocation;
import net.flectone.pulse.module.message.teleport.model.TeleportLocationMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

@Singleton
public class TeleportModule extends AbstractModuleLocalization<Localization.Message.Teleport> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public TeleportModule(FileResolver fileResolver,
                          ListenerRegistry listenerRegistry) {
        super(MessageType.TELEPORT);

        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(TeleportPulseListener.class);
    }

    @Override
    public Message.Teleport config() {
        return fileResolver.getMessage().getTeleport();
    }

    @Override
    public Permission.Message.Teleport permission() {
        return fileResolver.getPermission().getMessage().getTeleport();
    }

    @Override
    public Localization.Message.Teleport localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getTeleport();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, TeleportEntity teleportEntity) {
        if (isModuleDisabledFor(fPlayer)) return;

        if (translationKey == MinecraftTranslationKey.COMMANDS_TELEPORT_SUCCESS_ENTITY_SINGLE) {
            sendMessage(TeleportEntityMetadata.<Localization.Message.Teleport>builder()
                    .sender(fPlayer)
                    .range(config().getRange())
                    .format(localization -> localization.getEntity().getSingle())
                    .teleportEntity(teleportEntity)
                    .translationKey(translationKey)
                    .destination(config().getDestination())
                    .sound(getModuleSound())
                    .tagResolvers(fResolver -> new TagResolver[]{
                            targetTag(fResolver, teleportEntity.getTarget()),
                            targetTag("second_target", fResolver, teleportEntity.getSecondTarget())
                    })
                    .build()
            );
        } else {
            sendMessage(TeleportEntityMetadata.<Localization.Message.Teleport>builder()
                    .sender(fPlayer)
                    .range(config().getRange())
                    .format(localization -> Strings.CS.replace(
                            localization.getEntity().getMultiple(),
                            "<entities>",
                            StringUtils.defaultString(teleportEntity.getEntities())
                    ))
                    .teleportEntity(teleportEntity)
                    .translationKey(translationKey)
                    .destination(config().getDestination())
                    .sound(getModuleSound())
                    .tagResolvers(fResolver -> new TagResolver[]{targetTag("second_target", fResolver, teleportEntity.getSecondTarget())})
                    .build()
            );
        }
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, TeleportLocation teleportLocation) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(TeleportLocationMetadata.<Localization.Message.Teleport>builder()
                .sender(fPlayer)
                .range(config().getRange())
                .format(localization -> StringUtils.replaceEach(
                        translationKey == MinecraftTranslationKey.COMMANDS_TELEPORT_SUCCESS_LOCATION_SINGLE ? localization.getLocation().getSingle() : localization.getLocation().getMultiple(),
                        new String[]{"<entities>", "<x>", "<y>", "<z>"},
                        new String[]{StringUtils.defaultString(teleportLocation.getEntities()), teleportLocation.getX(), teleportLocation.getY(), teleportLocation.getZ()}
                ))
                .teleportLocation(teleportLocation)
                .translationKey(translationKey)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, teleportLocation.getTarget())})
                .build()
        );
    }
}
