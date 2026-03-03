package net.flectone.pulse.module.message.auto;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.util.Sound;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.ModuleListLocalization;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.RandomUtil;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.type.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AutoModule implements ModuleListLocalization<Localization.Message.Auto> {

    private final Map<Integer, Integer> messageIndexMap = new ConcurrentHashMap<>();

    private final FileFacade fileFacade;
    private final TaskScheduler taskScheduler;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final RandomUtil randomUtil;

    @Override
    public void onEnable() {
        config().types().forEach((key, value) -> {
            Pair<Sound, PermissionSetting> sound = Pair.of(value.sound(), permission().types().get(key));

            Ticker ticker = value.ticker();
            if (ticker.enable()) {
                taskScheduler.runPlayerRegionTimer(fPlayer -> send(fPlayer, key, value, sound), ticker.period());
            }
        });
    }

    @Override
    public void onDisable() {
        messageIndexMap.clear();
    }

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return ModuleListLocalization.super.permissionBuilder().addAll(permission().types().values());
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_AUTO;
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

    @Override
    public int getPlayerIndexOrDefault(int id, int defaultIndex) {
        return messageIndexMap.getOrDefault(id, defaultIndex);
    }

    @Override
    public int nextInt(int start, int end) {
        return randomUtil.nextInt(start, end);
    }

    @Override
    public void savePlayerIndex(int id, int playerIndex) {
        messageIndexMap.put(id, playerIndex);
    }

    public void send(FPlayer fPlayer, String name, Message.Auto.Type type, Pair<Sound, PermissionSetting> sound) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        List<String> messages = localization(fPlayer).types().get(name);
        if (messages == null) return;

        String format = getNextMessage(fPlayer, type.random(), messages);
        if (StringUtils.isEmpty(format)) return;

        messageDispatcher.dispatch(this, EventMetadata.<Localization.Message.Auto>builder()
                .sender(fPlayer)
                .format(format)
                .destination(type.destination())
                .sound(sound)
                .build()
        );
    }
}
