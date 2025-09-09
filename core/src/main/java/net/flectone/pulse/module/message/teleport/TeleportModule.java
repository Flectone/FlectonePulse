package net.flectone.pulse.module.message.teleport;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import static net.flectone.pulse.execution.pipeline.MessagePipeline.ReplacementTag.empty;

@Singleton
public class TeleportModule extends AbstractModuleLocalization<Localization.Message.Teleport> {

    private final Message.Teleport message;
    private final Permission.Message.Teleport permission;
    private final ListenerRegistry listenerRegistry;
    private final MessagePipeline messagePipeline;

    @Inject
    public TeleportModule(FileResolver fileResolver,
                          ListenerRegistry listenerRegistry,
                          MessagePipeline messagePipeline) {
        super(localization -> localization.getMessage().getTeleport(), MessageType.TELEPORT);

        this.message = fileResolver.getMessage().getTeleport();
        this.permission = fileResolver.getPermission().getMessage().getTeleport();
        this.listenerRegistry = listenerRegistry;
        this.messagePipeline = messagePipeline;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(TeleportPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, TeleportEntity teleportEntity) {
        if (isModuleDisabledFor(fPlayer)) return;
        if (teleportEntity.isIncorrect()) return;

        FEntity target = teleportEntity.target() == null ? fPlayer : teleportEntity.target();

        if (translationKey == MinecraftTranslationKey.COMMANDS_TELEPORT_SUCCESS_ENTITY_SINGLE) {
            sendMessage(TeleportEntityMetadata.<Localization.Message.Teleport>builder()
                    .sender(target)
                    .filterPlayer(fPlayer)
                    .format(s -> s.getEntity().getSingle())
                    .teleportEntity(teleportEntity)
                    .translationKey(translationKey)
                    .destination(message.getDestination())
                    .sound(getModuleSound())
                    .tagResolvers(fReceiver -> new TagResolver[]{destinationTag(fReceiver, teleportEntity.destination())})
                    .build()
            );
        } else {
            sendMessage(TeleportEntityMetadata.<Localization.Message.Teleport>builder()
                    .sender(target)
                    .filterPlayer(fPlayer)
                    .format(s -> Strings.CS.replace(
                            s.getEntity().getMultiple(),
                            "<count>",
                            StringUtils.defaultString(teleportEntity.count())
                    ))
                    .teleportEntity(teleportEntity)
                    .translationKey(translationKey)
                    .destination(message.getDestination())
                    .sound(getModuleSound())
                    .build()
            );
        }
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, TeleportLocation teleportLocation) {
        if (isModuleDisabledFor(fPlayer)) return;
        if (teleportLocation.isIncorrect()) return;

        sendMessage(TeleportLocationMetadata.<Localization.Message.Teleport>builder()
                .sender(teleportLocation.target() == null ? fPlayer : teleportLocation.target())
                .filterPlayer(fPlayer)
                .format(s -> StringUtils.replaceEach(
                        translationKey == MinecraftTranslationKey.COMMANDS_TELEPORT_SUCCESS_LOCATION_SINGLE ? s.getLocation().getSingle() : s.getLocation().getMultiple(),
                        new String[]{"<count>", "<x>", "<y>", "<z>"},
                        new String[]{StringUtils.defaultString(teleportLocation.count()), teleportLocation.x(), teleportLocation.y(), teleportLocation.z()}
                ))
                .teleportLocation(teleportLocation)
                .translationKey(translationKey)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }

    public TagResolver destinationTag(FPlayer receiver, FEntity destination) {
        String tag = "destination";
        if (!isEnable()) return empty(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            Component component = messagePipeline.builder(destination, receiver, "<display_name>").build();

            return Tag.selfClosingInserting(component);
        });
    }
}
