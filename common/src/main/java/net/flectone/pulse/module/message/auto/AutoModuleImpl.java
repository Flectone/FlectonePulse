package net.flectone.pulse.module.message.auto;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.value.Pair;
import net.flectone.pulse.model.value.Sound;
import net.flectone.pulse.model.value.Ticker;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.random.RandomGenerator;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AutoModuleImpl implements AutoModule {

    private final Map<Integer, Integer> messageIndexMap = new ConcurrentHashMap<>();

    private final FileFacade fileFacade;
    private final TaskScheduler taskScheduler;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final RandomGenerator randomUtil;
    private final SocialService socialService;

    @Override
    public void onEnable() {
        config().types().forEach((key, value) -> {
            Pair<Sound, PermissionSetting> sound = Pair.of(value.sound(), permission().types().get(key));

            Ticker ticker = value.ticker();
            if (ticker.enable()) {
                taskScheduler.runPlayerAsyncTimer(fPlayer -> send(fPlayer, key, value, sound), ticker.period());
            }
        });
    }

    @Override
    public void onDisable() {
        messageIndexMap.clear();
    }

    @Override
    public Set<PermissionSetting> permissions() {
        Set<PermissionSetting> permissions = new LinkedHashSet<>(AutoModule.super.permissions());
        permissions.addAll(permission().types().values());
        return Collections.unmodifiableSet(permissions);
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
    public Localization.Message.Auto localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().auto();
    }

    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return List.of();
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

    @Override
    public void send(FPlayer fPlayer, String name, Message.Auto.Type type, Pair<Sound, PermissionSetting> sound) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        List<String> messages = localization(fPlayer).types().get(name);
        if (messages == null) return;

        String format = getNextMessage(fPlayer, type.random(), messages);
        if (StringUtils.isEmpty(format)) return;

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .destination(type.destination())
                .sound(sound)
                .messageContext(fResolver -> MessageContext.builder()
                        .sender(fPlayer)
                        .receiver(fResolver)
                        .message(format)
                        .build()
                )
                .build()
        );
    }
}
