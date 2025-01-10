package net.flectone.pulse.module;

import com.google.inject.Inject;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.util.RandomUtil;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractModuleListMessage<M extends Localization.ILocalization> extends AbstractModuleMessage<M> {

    private final HashMap<Integer, Integer> PLAYER_INDEX = new HashMap<>();

    @Inject private RandomUtil randomUtil;

    public AbstractModuleListMessage(Function<Localization, M> messageFunction) {
        super(messageFunction);
    }

    public abstract List<String> getAvailableMessages(FPlayer fPlayer);

    public List<String> joinMultiList(List<List<String>> values) {
        return values.stream()
                .map(strings -> String.join("<br>", strings))
                .toList();
    }

    @Nullable
    public String getCurrentMessage(FPlayer fPlayer) {
        List<String> messages = getAvailableMessages(fPlayer);
        if (messages.isEmpty()) return null;

        int fPlayerID = fPlayer.getId();
        int playerIndex = PLAYER_INDEX.getOrDefault(fPlayerID, 0) % messages.size();

        return messages.get(playerIndex);
    }

    @Nullable
    public String getNextMessage(FPlayer fPlayer, boolean random) {
        List<String> messages = getAvailableMessages(fPlayer);
        if (messages.isEmpty()) return null;

        int fPlayerID = fPlayer.getId();
        int playerIndex = PLAYER_INDEX.getOrDefault(fPlayerID, 0);

        if (random) {
            playerIndex = randomUtil.nextInt(0, messages.size());
        } else {
            playerIndex++;
            playerIndex = playerIndex % messages.size();
        }

        PLAYER_INDEX.put(fPlayerID, playerIndex);

        return messages.get(playerIndex);
    }

}
