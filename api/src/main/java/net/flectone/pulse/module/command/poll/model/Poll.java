package net.flectone.pulse.module.command.poll.model;

import lombok.Getter;
import net.flectone.pulse.model.entity.FEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A running poll and the votes cast so far.
 * @author TheFaser
 */
@Getter
public class Poll {

    private final int id;
    private final int creator;
    private final long endTime;
    private final long repeatTime;
    private final boolean multipleVote;
    private final String title;
    private final List<String> answers = new ArrayList<>();
    private final Map<UUID, boolean[]> votesMap = new ConcurrentHashMap<>();

    private long nextRepeat;

    /**
     * Creates a poll.
     *
     * @param id the database id
     * @param creator the player who started it
     * @param endTime when voting closes, in milliseconds
     * @param repeatTime how often it is re-announced, in milliseconds
     * @param multipleVote whether a voter may pick several answers
     * @param title the question
     * @param answers the options to vote on
     */
    public Poll(int id, int creator, long endTime, long repeatTime, boolean multipleVote, String title, List<String> answers) {
        this.id = id;
        this.creator = creator;
        this.endTime = endTime;
        this.repeatTime = repeatTime;
        this.nextRepeat = System.currentTimeMillis() + repeatTime;
        this.multipleVote = multipleVote;
        this.title = title;
        this.answers.addAll(answers);
    }

    /**
     * Records a vote, withdrawing the voter's previous one when only a single choice is allowed.
     *
     * @param fPlayer the voter
     * @param numberVote the chosen option
     * @return whether the vote was cast, changed or withdrawn
     */
    public int vote(FEntity fPlayer, int numberVote) {
        boolean[] votes = votesMap.getOrDefault(fPlayer.uuid(), new boolean[answers.size()]);

        for (int x = 0; x < answers.size(); x++) {
            if (votes[x] && !multipleVote) return -1;
        }

        votes[numberVote] = !votes[numberVote];
        votesMap.put(fPlayer.uuid(), votes);
        return votes[numberVote] ? 1 : 0;
    }

    /**
     * How many votes each option has.
     *
     * @return the tallies, in option order
     */
    public int[] getCountAnswers() {
        int[] countAnswers = new int[answers.size()];

        for (boolean[] answers : votesMap.values()) {
            for (int x = 0; x < answers.length; x++) {
                if (answers[x]) {
                    countAnswers[x]++;
                }
            }
        }

        return countAnswers;
    }

    /**
     * Whether voting has closed.
     *
     * @return true if the poll is over
     */
    public boolean isEnded() {
        return System.currentTimeMillis() >= endTime;
    }

    /**
     * Whether the poll is due to be announced again.
     *
     * @return true if it should be re-announced now
     */
    public boolean repeat() {
        if (System.currentTimeMillis() < nextRepeat) return false;

        nextRepeat = System.currentTimeMillis() + repeatTime;
        return true;
    }
}
