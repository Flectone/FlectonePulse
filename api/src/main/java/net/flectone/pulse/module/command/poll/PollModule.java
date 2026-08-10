package net.flectone.pulse.module.command.poll;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.poll.model.Poll;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.UUID;

/**
 * The /poll command, which runs a vote in chat.
 * @author TheFaser
 */
public interface PollModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    /**
     * Runs the voting form of the command.
     *
     * @param fPlayer the voter
     * @param commandContext the parsed arguments
     */
    void executeVote(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Poll config();

    @Override
    Permission.Command.Poll permission();

    @Override
    Localization.Command.Poll localization(FPlayer fPlayer);

    /**
     * Starts a poll and announces it.
     *
     * @param fPlayer who started it
     * @param title the question
     * @param multipleValue whether a voter may pick several answers
     * @param endTimeValue how long voting stays open, in milliseconds
     * @param repeatTimeValue how often the poll is re-announced, in milliseconds
     * @param answers the options to vote on
     */
    void createPoll(FPlayer fPlayer, String title, boolean multipleValue, long endTimeValue, long repeatTimeValue, List<String> answers);

    /**
     * Stores a poll and makes it the one an unqualified vote applies to.
     *
     * @param poll the poll
     */
    void saveAndUpdateLast(Poll poll);

    /**
     * Records a vote and announces the new tally.
     *
     * @param fPlayer the voter
     * @param id the poll id
     * @param numberVote the chosen option
     * @param metadataUUID the shared message id
     */
    void vote(FEntity fPlayer, int id, int numberVote, UUID metadataUUID);

    /**
     * The wording confirming a vote.
     *
     * @param fPlayer the reader
     * @param voteType whether the vote was cast, changed or withdrawn
     * @param answerID the chosen option
     * @param pollID the poll id
     * @param count the votes that option now has
     * @return the confirmation text
     */
    String resolveVote(FPlayer fPlayer, int voteType, int answerID, int pollID, int count);

    /**
     * The rendered poll, laid out for the stage it has reached.
     *
     * @param fPlayer the reader
     * @param poll the poll
     * @param status the stage
     * @return the rendered poll
     */
    String resolvePollFormat(FPlayer fPlayer, Poll poll, Status status);

    /**
     * The answers of a poll, each as its own tag
     *
     * @param sender who created the poll
     * @param fReceiver the reader
     * @param poll the poll
     * @return one resolver per answer
     */
    TagResolver[] resolveAnswerTags(FEntity sender, FPlayer fReceiver, Poll poll);

    /**
     * How far a poll has got, either just announced, running, or closed.
     */
    enum Status {
        START,
        RUN,
        END
    }

    /**
     * What was done to a poll, either created, re-announced, or voted on.
     */
    enum Action {
        CREATE,
        REPEAT,
        VOTE
    }

}
