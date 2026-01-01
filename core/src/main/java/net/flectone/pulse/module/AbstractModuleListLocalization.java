package net.flectone.pulse.module;

import com.google.inject.Inject;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.RandomUtil;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractModuleListLocalization<M extends LocalizationSetting> extends AbstractModuleLocalization<M> {

    private final Map<Integer, Integer> messageIndexMap = new ConcurrentHashMap<>();

    @Inject private RandomUtil randomUtil;

    public abstract List<String> getAvailableMessages(FPlayer fPlayer);

    @Override
    public void onEnable() {
        super.onEnable();

        messageIndexMap.clear();
    }

    public List<String> joinMultiList(List<List<String>> values) {
        return values.stream()
                .map(strings -> String.join("<br>", strings))
                .toList();
    }

    public @Nullable String getCurrentMessage(FPlayer fPlayer) {
        List<String> messages = getAvailableMessages(fPlayer);
        if (messages.isEmpty()) return null;

        int fPlayerID = fPlayer.getId();
        int playerIndex = messageIndexMap.getOrDefault(fPlayerID, 0) % messages.size();

        return messages.get(playerIndex);
    }

    public @Nullable String getNextMessage(FPlayer fPlayer, boolean random) {
        int id = fPlayer.getId();
        List<String> messages = getAvailableMessages(fPlayer);

        return incrementAndGetMessage(id, random, messages);
    }

    public @Nullable String getNextMessage(FPlayer fPlayer, boolean random, List<String> messages) {
        int id = fPlayer.getId() + messages.hashCode();

        return incrementAndGetMessage(id, random, messages);
    }

    private @Nullable String incrementAndGetMessage(int id, boolean random, List<String> messages) {
        if (messages.isEmpty()) return null;

        int playerIndex = messageIndexMap.getOrDefault(id, 0);

        if (random) {
            playerIndex = randomUtil.nextInt(0, messages.size());
        } else {
            playerIndex++;
            playerIndex = playerIndex % messages.size();
        }

        messageIndexMap.put(id, playerIndex);

        return messages.get(playerIndex);
    }

}
