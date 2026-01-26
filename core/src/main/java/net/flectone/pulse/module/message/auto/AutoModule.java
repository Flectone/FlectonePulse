package net.flectone.pulse.module.message.auto;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.util.Sound;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.AbstractModuleListLocalization;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.type.tuple.Pair;

import java.util.Collections;
import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AutoModule extends AbstractModuleListLocalization<Localization.Message.Auto> {

    private final FileFacade fileFacade;
    private final TaskScheduler taskScheduler;

    @Override
    public void onEnable() {
        super.onEnable();

        config().types().forEach((key, value) -> {
            Pair<Sound, PermissionSetting> sound = Pair.of(value.sound(), permission().types().get(key));

            Ticker ticker = value.ticker();
            if (ticker.enable()) {
                taskScheduler.runPlayerRegionTimer(fPlayer -> send(fPlayer, key, value, sound), ticker.period());
            }
        });
    }

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return super.permissionBuilder().addAll(permission().types().values());
    }

    @Override
    public MessageType messageType() {
        return MessageType.AUTO;
    }

    @Override
    public Message.Auto config() {
        return fileFacade.message().auto();
    }

    @Override
    public Permission.Message.Auto permission() {
        return fileFacade.permission().message().auto();
    }

    @Override
    public Localization.Message.Auto localization(FEntity sender) {
        return fileFacade.localization(sender).message().auto();
    }

    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return Collections.emptyList();
    }

    public void send(FPlayer fPlayer, String name, Message.Auto.Type type, Pair<Sound, PermissionSetting> sound) {
        if (isModuleDisabledFor(fPlayer)) return;

        List<String> messages = localization(fPlayer).types().get(name);
        if (messages == null) return;

        String format = getNextMessage(fPlayer, type.random(), messages);
        if (StringUtils.isEmpty(format)) return;

        sendMessage(EventMetadata.<Localization.Message.Auto>builder()
                .sender(fPlayer)
                .format(format)
                .destination(type.destination())
                .sound(sound)
                .build()
        );
    }
}
