package net.flectone.pulse.module.message.spawn;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.message.TranslatableMessageEvent;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.spawn.listener.ChangeGameStatePacketListener;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.List;
import java.util.UUID;

@Singleton
public class SpawnModule extends AbstractModuleMessage<Localization.Message.Spawn> {

    private final Message.Spawn message;
    private final Permission.Message.Spawn permission;
    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public SpawnModule(FileResolver fileResolver,
                       FPlayerService fPlayerService,
                       ListenerRegistry listenerRegistry,
                       EventProcessRegistry eventProcessRegistry) {
        super(localization -> localization.getMessage().getSpawn());

        this.message = fileResolver.getMessage().getSpawn();
        this.permission = fileResolver.getPermission().getMessage().getSpawn();
        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;
        this.eventProcessRegistry = eventProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(ChangeGameStatePacketListener.class);
        eventProcessRegistry.registerMessageHandler(event -> {
            if (event.getKey() == MinecraftTranslationKey.BLOCK_MINECRAFT_SET_SPAWN) {
                event.cancel();
                send(event.getUserUUID(), event.getKey());
                return;
            }

            if (event.getKey().startsWith("commands.spawnpoint.success")) {
                TranslatableComponent translatableComponent = event.getComponent();
                List<Component> translationArguments = translatableComponent.args();

                if (translationArguments.size() < 4) return;

                Component targetComponent;
                Component xComponent;
                Component yComponent;
                Component zComponent;
                String angle = "";
                String world = "";

                if (event.getKey() == MinecraftTranslationKey.COMMANDS_SPAWNPOINT_SUCCESS) {
                    // legacy format, player first
                    targetComponent = translationArguments.get(0);
                    xComponent = translationArguments.get(1);
                    yComponent = translationArguments.get(2);
                    zComponent = translationArguments.get(3);
                } else {
                    // coordinates first, player last
                    xComponent = translationArguments.get(0);
                    yComponent = translationArguments.get(1);
                    zComponent = translationArguments.get(2);
                    targetComponent = translationArguments.getLast();

                    // check for optional angle and world
                    if (translationArguments.size() >= 5 && translationArguments.get(3) instanceof TextComponent angleComponent) {
                        angle = angleComponent.content();
                    }

                    if (translationArguments.size() >= 6 && translationArguments.get(4) instanceof TextComponent worldComponent) {
                        world = worldComponent.content();
                    }
                }

                if (!(xComponent instanceof TextComponent xComp)) return;
                if (!(yComponent instanceof TextComponent yComp)) return;
                if (!(zComponent instanceof TextComponent zComp)) return;
                if (!(targetComponent instanceof TextComponent tgtComp)) return;

                String x = xComp.content();
                String y = yComp.content();
                String z = zComp.content();
                String value = tgtComp.content();

                event.cancel();
                send(event, x, y, z, angle, world, value);
            }
        });
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID receiver, MinecraftTranslationKey key) {
        FPlayer fPlayer = fPlayerService.getFPlayer(receiver);
        if (checkModulePredicates(fPlayer)) return;

        builder(fPlayer)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(spawn -> key == MinecraftTranslationKey.BLOCK_MINECRAFT_SET_SPAWN
                        ? spawn.getSet() : spawn.getNotValid())
                .sound(getSound())
                .sendBuilt();
    }

    @Async
    public void send(TranslatableMessageEvent event, String x, String y, String z, String angle, String world, String value) {
        FPlayer fPlayer = fPlayerService.getFPlayer(event.getUserUUID());
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayer;

        boolean isSingle = event.getKey() == MinecraftTranslationKey.COMMANDS_SPAWNPOINT_SUCCESS_SINGLE
                || event.getKey() == MinecraftTranslationKey.COMMANDS_SPAWNPOINT_SUCCESS;

        if (isSingle) {
            fTarget = fPlayerService.getFPlayer(value);
            if (fTarget.isUnknown()) return;
        }

        builder(fTarget)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(s -> (isSingle ? s.getSingle() : s.getMultiple().replace("<count>", value))
                        .replace("<x>", x)
                        .replace("<y>", y)
                        .replace("<z>", z)
                        .replace("<angle>", angle)
                        .replace("<world>", world)
                )
                .sound(getSound())
                .sendBuilt();
    }
}
