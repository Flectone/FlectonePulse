package net.flectone.pulse.module.message.status.motd;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.random.RandomGenerator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MotdModuleImpl implements MotdModule {

    private final Map<Integer, Integer> messageIndexMap = new ConcurrentHashMap<>();

    private final FileFacade fileFacade;
    private final RandomGenerator randomGenerator;
    private final SocialService socialService;

    @Override
    public void onDisable() {
        messageIndexMap.clear();
    }

    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return joinMultiList(localization(fPlayer).values());
    }

    @Override
    public int getPlayerIndexOrDefault(int id, int defaultIndex) {
        return messageIndexMap.getOrDefault(id, defaultIndex);
    }

    @Override
    public int nextInt(int start, int end) {
        return randomGenerator.nextInt(start, end);
    }

    @Override
    public void savePlayerIndex(int id, int playerIndex) {
        messageIndexMap.put(id, playerIndex);
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_STATUS_MOTD;
    }

    @Override
    public Message.Status.MOTD config() {
        return fileFacade.message().status().motd();
    }

    @Override
    public Permission.Message.Status.MOTD permission() {
        return fileFacade.permission().message().status().motd();
    }

    @Override
    public Localization.Message.Status.MOTD localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().status().motd();
    }

}
