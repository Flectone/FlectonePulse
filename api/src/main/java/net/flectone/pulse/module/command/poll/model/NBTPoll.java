package net.flectone.pulse.module.command.poll.model;

import java.util.List;

/**
 * The poll settings a player filled into the creation dialog, before they are validated.
 *
 * @param input the question
 * @param multiple whether a voter may pick several answers
 * @param endTime how long voting stays open
 * @param repeatTime how often the poll is re-announced
 * @param answers the options to vote on
 * @author TheFaser
 */
public record NBTPoll(
        String input,
        boolean multiple,
        float endTime,
        float repeatTime,
        List<String> answers
) {
}
