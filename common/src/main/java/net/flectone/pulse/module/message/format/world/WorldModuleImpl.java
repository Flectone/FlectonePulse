package net.flectone.pulse.module.message.format.world;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.message.format.world.listener.PulseWorldListener;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.SocialService;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Set;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WorldModuleImpl implements WorldModule {

    private final FileFacade fileFacade;
    private final SocialService socialService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ListenerRegistry listenerRegistry;
    private final TaskScheduler taskScheduler;
    private final MessagePipeline messagePipeline;
    private final ModuleController moduleController;

    @Override
    public void onEnable() {
        listenerRegistry.register(PulseWorldListener.class);
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_FORMAT_WORLD;
    }

    @Override
    public Message.Format.World config() {
        return fileFacade.message().format().world();
    }

    @Override
    public Permission.Message.Format.World permission() {
        return fileFacade.permission().message().format().world();
    }

    @Override
    public MessageContext addTag(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(this, sender)) return messageContext;
        if (!(sender instanceof FPlayer fPlayer)) return messageContext;

        return messageContext.addTagResolver(messagePipeline.resolver(Set.of(MessagePipeline.ReplacementTag.WORLD.getTagName(), "world_prefix"), (_, _) -> {
            String worldPrefix = socialService.getSetting(fPlayer, SettingText.WORLD_PREFIX);
            if (StringUtils.isEmpty(worldPrefix)) return MessagePipeline.ReplacementTag.emptyTag();
            if (!worldPrefix.contains("%")) return Tag.preProcessParsed(worldPrefix);

            MessageContext worldContext = MessageContext.builder()
                    .sender(fPlayer)
                    .receiver(messageContext.receiver())
                    .message(worldPrefix)
                    .flags(messageContext.flags())
                    .flag(MessageFlag.PLAYER_MESSAGE, false)
                    .build();

            return Tag.inserting(messagePipeline.build(worldContext));
        }));
    }

    @Override
    public void update(FPlayer fPlayer) {
        taskScheduler.runAsync(() -> {
            if (moduleController.isDisabledFor(this, fPlayer)) return;

            String newWorldPrefix = config().mode() == Mode.TYPE
                    ? config().values().get(platformPlayerAdapter.getWorldEnvironment(fPlayer))
                    : config().values().get(platformPlayerAdapter.getWorldName(fPlayer));

            SettingText setting = SettingText.WORLD_PREFIX;
            if (Objects.equals(socialService.getSetting(fPlayer, setting), newWorldPrefix)) return;

            socialService.saveSetting(fPlayer, setting, newWorldPrefix);
        });
    }

}
