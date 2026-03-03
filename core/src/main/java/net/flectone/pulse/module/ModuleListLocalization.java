package net.flectone.pulse.module;

import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.entity.FPlayer;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface ModuleListLocalization<M extends LocalizationSetting> extends ModuleLocalization<M> {

    List<String> getAvailableMessages(FPlayer fPlayer);

    int getPlayerIndexOrDefault(int id, int defaultIndex);

    int nextInt(int start, int end);

    void savePlayerIndex(int id, int playerIndex);

    default List<String> joinMultiList(List<List<String>> values) {
        return values.stream()
                .map(strings -> String.join("<br>", strings))
                .toList();
    }

    default @Nullable String getCurrentMessage(FPlayer fPlayer) {
        List<String> messages = getAvailableMessages(fPlayer);
        if (messages.isEmpty()) return null;

        int fPlayerID = fPlayer.id();
        int playerIndex = getPlayerIndexOrDefault(fPlayerID, 0) % messages.size();

        return messages.get(playerIndex);
    }

    default @Nullable String getNextMessage(FPlayer fPlayer, boolean random) {
        int id = fPlayer.id();
        List<String> messages = getAvailableMessages(fPlayer);

        return incrementAndGetMessage(id, random, messages);
    }

    default @Nullable String getNextMessage(FPlayer fPlayer, boolean random, List<String> messages) {
        int id = fPlayer.id() + messages.hashCode();

        return incrementAndGetMessage(id, random, messages);
    }

    private @Nullable String incrementAndGetMessage(int id, boolean random, List<String> messages) {
        if (messages.isEmpty()) return null;

        int playerIndex = getPlayerIndexOrDefault(id, 0);

        if (random) {
            playerIndex = nextInt(0, messages.size());
        } else {
            playerIndex++;
            playerIndex = playerIndex % messages.size();
        }

        savePlayerIndex(id, playerIndex);

        return messages.get(playerIndex);
    }

}
