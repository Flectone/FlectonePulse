package net.flectone.pulse.module;

import com.google.inject.Inject;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.util.RandomUtil;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractModuleListLocalization<M extends Localization.Localizable> extends AbstractModuleLocalization<M> {

    private final HashMap<Integer, Integer> messageIndexMap = new HashMap<>();

    @Inject private RandomUtil randomUtil;

    protected AbstractModuleListLocalization(Function<Localization, M> messageFunction) {
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
        int playerIndex = messageIndexMap.getOrDefault(fPlayerID, 0) % messages.size();

        return messages.get(playerIndex);
    }

    @Nullable
    public String getNextMessage(FPlayer fPlayer, boolean random) {
        int id = fPlayer.getId();
        List<String> messages = getAvailableMessages(fPlayer);

        return incrementAndGetMessage(id, random, messages);
    }

    @Nullable
    public String getNextMessage(FPlayer fPlayer, boolean random, List<String> messages) {
        int id = fPlayer.getId() + messages.hashCode();

        return incrementAndGetMessage(id, random, messages);
    }

    @Nullable
    private String incrementAndGetMessage(int id, boolean random, List<String> messages) {
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
