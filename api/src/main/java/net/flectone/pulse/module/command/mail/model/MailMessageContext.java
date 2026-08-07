package net.flectone.pulse.module.command.mail.model;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.StringMessageContext;
import org.jspecify.annotations.NonNull;

/**
 * A mail being sent or delivered.
 * Builds on {@link StringMessageContext}.
 * @author TheFaser
 */
public interface MailMessageContext extends StringMessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static MailMessageContextImpl.MailMessageContextImplBuilder builder() {
        return MailMessageContextImpl.builder();
    }

    /**
     * The mail.
     *
     * @return the mail
     */
    @NonNull Mail mail();

    /**
     * The player it is addressed to.
     *
     * @return the player it is addressed to
     */
    @NonNull FPlayer target();

    /**
     * Returns a copy carrying a different value.
     *
     * @param mail the mail
     * @return the copy
     */
    MailMessageContext withMail(@NonNull Mail mail);

    /**
     * Returns a copy carrying a different value.
     *
     * @param target the player it is addressed to
     * @return the copy
     */
    MailMessageContext withTarget(@NonNull FPlayer target);

}