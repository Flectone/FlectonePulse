package net.flectone.pulse.module.message.format.world;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.world.listener.WorldPacketListener;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import static net.flectone.pulse.util.TagResolverUtil.emptyTagResolver;

@Singleton
public class WorldModule extends AbstractModule implements MessageProcessor {

    private final Message.Format.World message;
    private final Permission.Message.Format.World permission;
    private final Permission.Message.Format formatPermission;

    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ListenerRegistry listenerRegistry;
    private final PermissionChecker permissionChecker;

    @Inject
    public WorldModule(FileManager fileManager,
                       FPlayerService fPlayerService,
                       PlatformPlayerAdapter platformPlayerAdapter,
                       ListenerRegistry listenerRegistry,
                       PermissionChecker permissionChecker,
                       MessageProcessRegistry messageProcessRegistry) {
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.listenerRegistry = listenerRegistry;
        this.permissionChecker = permissionChecker;

        message = fileManager.getMessage().getFormat().getWorld();
        permission = fileManager.getPermission().getMessage().getFormat().getWorld();
        formatPermission = fileManager.getPermission().getMessage().getFormat();

        messageProcessRegistry.register(150, this);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        listenerRegistry.register(WorldPacketListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void process(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (messageContext.isUserMessage() && !permissionChecker.check(sender, formatPermission.getAll())) return;

        messageContext.addTagResolvers(worldTag(sender));
    }

    @Async
    public void update(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        String newWorldPrefix = message.getMode() == WorldMode.TYPE
                ? message.getValues().get(platformPlayerAdapter.getWorldEnvironment(fPlayer))
                : message.getValues().get(platformPlayerAdapter.getWorldName(fPlayer));

        String fPlayerWorldPrefix = fPlayer.getSettingValue(FPlayer.Setting.WORLD_PREFIX);
        if (newWorldPrefix == null && fPlayerWorldPrefix == null) return;
        if (newWorldPrefix != null && newWorldPrefix.equalsIgnoreCase(fPlayerWorldPrefix)) return;

        fPlayerService.saveOrUpdateSetting(fPlayer, FPlayer.Setting.WORLD_PREFIX, newWorldPrefix);
    }

    private TagResolver worldTag(@NotNull FEntity sender) {
        String tag = "world_prefix";
        if (checkModulePredicates(sender)) return emptyTagResolver(tag);
        if (!(sender instanceof FPlayer fPlayer)) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            String worldPrefix = fPlayer.getSettingValue(FPlayer.Setting.WORLD_PREFIX);
            if (worldPrefix == null) return Tag.selfClosingInserting(Component.empty());

            return Tag.preProcessParsed(worldPrefix);
        });
    }
}
