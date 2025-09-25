package net.flectone.pulse.module.message.auto;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Sound;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.AbstractModuleListLocalization;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

@Singleton
public class AutoModule extends AbstractModuleListLocalization<Localization.Message.Auto> {

    private final FileResolver fileResolver;
    private final TaskScheduler taskScheduler;
    private final FPlayerService fPlayerService;

    @Inject
    public AutoModule(FileResolver fileResolver,
                      TaskScheduler taskScheduler,
                      FPlayerService fPlayerService) {
        super(MessageType.AUTO);

        this.fileResolver = fileResolver;
        this.taskScheduler = taskScheduler;
        this.fPlayerService = fPlayerService;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        config().getTypes().forEach((key, value) -> {
            Sound sound = createSound(value.getSound(), permission().getTypes().get(key));

            Ticker ticker = value.getTicker();
            if (ticker.isEnable()) {
                taskScheduler.runAsyncTimer(() -> fPlayerService.getOnlineFPlayers().forEach(fPlayer -> send(fPlayer, key, value, sound)), ticker.getPeriod());
            }
        });
    }

    @Override
    public Message.Auto config() {
        return fileResolver.getMessage().getAuto();
    }

    @Override
    public Permission.Message.Auto permission() {
        return fileResolver.getPermission().getMessage().getAuto();
    }

    @Override
    public Localization.Message.Auto localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getAuto();
    }

    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return Collections.emptyList();
    }

    public void send(FPlayer fPlayer, String name, Message.Auto.Type type, Sound sound) {
        if (isModuleDisabledFor(fPlayer)) return;

        List<String> messages = localization(fPlayer).getTypes().get(name);
        if (messages == null) return;

        String format = getNextMessage(fPlayer, type.isRandom(), messages);
        if (StringUtils.isEmpty(format)) return;

        sendMessage(metadataBuilder()
                .sender(fPlayer)
                .format(format)
                .destination(type.getDestination())
                .sound(sound)
                .build()
        );
    }
}
