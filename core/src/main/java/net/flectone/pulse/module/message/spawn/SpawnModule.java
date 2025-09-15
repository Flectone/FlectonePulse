package net.flectone.pulse.module.message.spawn;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
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

    private final Message.Spawn message;
    private final Permission.Message.Spawn permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public SpawnModule(FileResolver fileResolver,
                       ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getSpawn(), MessageType.SPAWN);

        this.message = fileResolver.getMessage().getSpawn();
        this.permission = fileResolver.getPermission().getMessage().getSpawn();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(SpawnPacketListener.class);
        listenerRegistry.register(SpawnPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
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
                .destination(message.getDestination())
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
                .range(message.getRange())
                .format(localization -> StringUtils.replaceEach(
                        translationKey == MinecraftTranslationKey.COMMANDS_SETWORLDSPAWN_SUCCESS ? localization.getSetWorld() : isSingle ? localization.getSingle() : localization.getMultiple(),
                        new String[]{"<players>", "<x>", "<y>", "<z>", "<angle>", "<world>"},
                        new String[]{StringUtils.defaultString(spawn.getPlayers()), spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getAngle(), StringUtils.defaultString(spawn.getWorld())}
                ))
                .spawn(spawn)
                .translationKey(translationKey)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, spawn.getTarget())})
                .build()
        );
    }
}
