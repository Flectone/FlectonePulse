package net.flectone.pulse.module;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.MultilineString;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A module whose text is a list rather than a single line, stepping to the next entry each
 * time it fires, as used by rotating messages such as the MOTD or auto broadcasts.
 * @author TheFaser
 */
public interface ModuleListLocalization extends ModuleLocalization {

    /**
     * The lines a player may currently be shown.
     *
     * @param fPlayer the player the text is for
     * @return the available lines
     */
    List<String> getAvailableMessages(FPlayer fPlayer);

    /**
     * The line a player is currently on.
     *
     * @param id the rotation id, which is the player id mixed with the list being rotated
     * @param defaultIndex what to return when the player has no position yet
     * @return the stored index, or the default
     */
    int getPlayerIndexOrDefault(int id, int defaultIndex);

    /**
     * Picks a random index, used when the rotation is set to random order.
     *
     * @param start the lower bound, inclusive
     * @param end the upper bound, exclusive
     * @return the random index
     */
    int nextInt(int start, int end);

    /**
     * Remembers the line a player is now on.
     *
     * @param id the rotation id
     * @param playerIndex the new index
     */
    void savePlayerIndex(int id, int playerIndex);

    /**
     * Flattens a list of blocks into a list of lines, joining the lines of each block with a line.
     *
     * @param values the blocks
     * @return one string per block
     */
    default List<String> joinMultiList(List<MultilineString> values) {
        return values.stream()
                .map(MultilineString::value)
                .toList();
    }

    /**
     * The line a player is on right now, without advancing the rotation.
     *
     * @param fPlayer the player the text is for
     * @return the current line, or null if there are none
     */
    default @Nullable String getCurrentMessage(FPlayer fPlayer) {
        List<String> messages = getAvailableMessages(fPlayer);
        if (messages.isEmpty()) return null;

        int fPlayerID = fPlayer.id();
        int playerIndex = getPlayerIndexOrDefault(fPlayerID, 0) % messages.size();

        return messages.get(playerIndex);
    }

    /**
     * Advances the rotation and returns the next line.
     *
     * @param fPlayer the player the text is for
     * @param random whether to jump to a random line instead of the next one
     * @return the next line, or null if there are none
     */
    default @Nullable String getNextMessage(FPlayer fPlayer, boolean random) {
        int id = fPlayer.id();
        List<String> messages = getAvailableMessages(fPlayer);

        return incrementAndGetMessage(id, random, messages);
    }

    /**
     * Advances the rotation over an explicit list, keeping a position separate from the module's own.
     *
     * @param fPlayer the player the text is for
     * @param random whether to jump to a random line instead of the next one
     * @param messages the lines to rotate through
     * @return the next line, or null if the list is empty
     */
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
