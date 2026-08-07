package net.flectone.pulse.module.command.clearmail.model;

import net.flectone.pulse.model.event.message.context.StringMessageContext;
import net.flectone.pulse.module.command.mail.model.Mail;
import org.jspecify.annotations.NonNull;

/**
 * A mail removed by the clear-mail command.
 * Builds on {@link StringMessageContext}.
 * @author TheFaser
 */
public interface ClearMessageContext extends StringMessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static ClearMessageContextImpl.ClearMessageContextImplBuilder builder() {
        return ClearMessageContextImpl.builder();
    }

    /**
     * The mail that was deleted.
     *
     * @return the mail that was deleted
     */
    @NonNull Mail mail();

    /**
     * Returns a copy carrying a different value.
     *
     * @param mail the mail that was deleted
     * @return the copy
     */
    ClearMessageContext withMail(@NonNull Mail mail);

}