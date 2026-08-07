package net.flectone.pulse.platform.filter;

import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.value.Range;
import org.jspecify.annotations.NonNull;

import java.util.function.Predicate;

/**
 * Turns a {@link Range} into a test for whether one player should receive a message,
 * by distance, by world, by server or across the whole proxy.
 * @author TheFaser
 */
public interface RangeFilter {

    /**
     * Builds the receiver test for a message from its metadata.
     *
     * @param eventMetadata supplies the range and the extra filter
     * @param messageContext supplies the sender the range is measured from
     * @return the test a player must pass to receive the message
     */
    @NonNull Predicate<FPlayer> createFilter(@NonNull EventMetadata eventMetadata, @NonNull MessageContext messageContext);

    /**
     * Builds the receiver test for an explicit sender and range.
     *
     * @param filterPlayer the entity the range is measured from
     * @param range the range
     * @return the test a player must pass to receive the message
     */
    @NonNull Predicate<FPlayer> createFilter(@NonNull FEntity filterPlayer, @NonNull Range range);

    /**
     * Whether two players are in the same world and within the given radius.
     *
     * @param fPlayer the sender
     * @param fReceiver the candidate receiver
     * @param range the radius in blocks
     * @return true if the receiver is close enough
     */
    boolean checkDistance(@NonNull FPlayer fPlayer, @NonNull FPlayer fReceiver, int range);

    /**
     * Whether the receiver may read messages from the sender's world, by world name.
     *
     * @param fPlayer the sender
     * @param fReceiver the candidate receiver
     * @return true if the receiver is allowed
     */
    boolean checkWorldNamePermission(@NonNull FPlayer fPlayer, @NonNull FPlayer fReceiver);

    /**
     * Whether the receiver may read messages from the sender's world, by world type.
     *
     * @param fPlayer the sender
     * @param fReceiver the candidate receiver
     * @return true if the receiver is allowed
     */
    boolean checkWorldTypePermission(@NonNull FPlayer fPlayer, @NonNull FPlayer fReceiver);

}
