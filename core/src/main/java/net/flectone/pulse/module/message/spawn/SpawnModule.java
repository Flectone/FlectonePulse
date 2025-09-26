package net.flectone.pulse.module.message.spawn;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.spawn.listener.SpawnPacketListener;
import net.flectone.pulse.module.message.spawn.listener.SpawnPulseListener;
import net.flectone.pulse.module.message.spawn.model.Spawn;
import net.flectone.pulse.module.message.spawn.model.SpawnMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;

@Singleton
public class SpawnModule extends AbstractModuleLocalization<Localization.Message.Spawn> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public SpawnModule(FileResolver fileResolver,
                       ListenerRegistry listenerRegistry) {
        super(MessageType.SPAWN);

        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(SpawnPacketListener.class);
        listenerRegistry.register(SpawnPulseListener.class);
    }

    @Override
    public Message.Spawn config() {
        return fileResolver.getMessage().getSpawn();
    }

    @Override
    public Permission.Message.Spawn permission() {
        return fileResolver.getPermission().getMessage().getSpawn();
    }

    @Override
    public Localization.Message.Spawn localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getSpawn();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(SpawnMetadata.<Localization.Message.Spawn>builder()
                .sender(fPlayer)
                .filterPlayer(fPlayer)
                .format(localization -> translationKey == MinecraftTranslationKey.BLOCK_MINECRAFT_SET_SPAWN
                        ? localization.getSet() : localization.getNotValid()
                )
                .destination(config().getDestination())
                .translationKey(translationKey)
                .sound(getModuleSound())
                .build()
        );
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Spawn spawn) {
        if (isModuleDisabledFor(fPlayer)) return;

        boolean isSingle = translationKey == MinecraftTranslationKey.COMMANDS_SPAWNPOINT_SUCCESS_SINGLE
                || translationKey == MinecraftTranslationKey.COMMANDS_SPAWNPOINT_SUCCESS;

        sendMessage(SpawnMetadata.<Localization.Message.Spawn>builder()
                .sender(fPlayer)
                .range(config().getRange())
                .format(localization -> StringUtils.replaceEach(
                        translationKey == MinecraftTranslationKey.COMMANDS_SETWORLDSPAWN_SUCCESS ? localization.getSetWorld() : isSingle ? localization.getSingle() : localization.getMultiple(),
                        new String[]{"<players>", "<x>", "<y>", "<z>", "<angle>", "<world>"},
                        new String[]{StringUtils.defaultString(spawn.getPlayers()), spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getAngle(), StringUtils.defaultString(spawn.getWorld())}
                ))
                .spawn(spawn)
                .translationKey(translationKey)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, spawn.getTarget())})
                .build()
        );
    }
}
