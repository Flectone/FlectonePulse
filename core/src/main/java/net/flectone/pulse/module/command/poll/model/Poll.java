package net.flectone.pulse.module.command.poll.model;

import lombok.Getter;
import lombok.Setter;
import net.flectone.pulse.model.FEntity;

import java.util.HashMap;
import java.util.UUID;

@Getter
public class Poll {

    private final int id;
    private final int countVotes;
    private final boolean multipleVote;

    @Setter
    private boolean expired;

    private final HashMap<UUID, boolean[]> votesMap = new HashMap<>();

    public Poll(int id, int countVotes, boolean multipleVote) {
        this.id = id;
        this.countVotes = countVotes;
        this.multipleVote = multipleVote;
    }

    public int vote(FEntity fPlayer, int numberVote) {
        boolean[] votes = votesMap.getOrDefault(fPlayer.getUuid(), new boolean[countVotes]);

        for (int x = 0; x < countVotes; x++) {
            if (votes[x] && !multipleVote) return -1;
        }

        votes[numberVote] = !votes[numberVote];
        votesMap.put(fPlayer.getUuid(), votes);
        return votes[numberVote] ? 1 : 0;
    }

    public int[] getCountAnswers() {
        int[] countAnswers = new int[countVotes];

        for (boolean[] answers : votesMap.values()) {
            for (int x = 0; x < answers.length; x++) {
                if (answers[x]) {
                    countAnswers[x]++;
                }
            }
        }

        return countAnswers;
    }
}
