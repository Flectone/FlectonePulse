package net.flectone.pulse.module.command.poll.model;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.flectone.pulse.model.entity.FEntity;

import java.util.*;

@Getter
public class Poll {

    private final int id;
    private final int creator;
    private final long endTime;
    private final long repeatTime;
    private final boolean multipleVote;
    private final String title;
    private final List<String> answers = new ObjectArrayList<>();
    private final Map<UUID, boolean[]> votesMap = new HashMap<>();

    private long nextRepeat;

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

    public int vote(FEntity fPlayer, int numberVote) {
        boolean[] votes = votesMap.getOrDefault(fPlayer.getUuid(), new boolean[answers.size()]);

        for (int x = 0; x < answers.size(); x++) {
            if (votes[x] && !multipleVote) return -1;
        }

        votes[numberVote] = !votes[numberVote];
        votesMap.put(fPlayer.getUuid(), votes);
        return votes[numberVote] ? 1 : 0;
    }

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

    public boolean isEnded() {
        return System.currentTimeMillis() >= endTime;
    }

    public boolean repeat() {
        if (System.currentTimeMillis() < nextRepeat) return false;

        nextRepeat = System.currentTimeMillis() + repeatTime;
        return true;
    }
}
