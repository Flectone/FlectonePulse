package net.flectone.pulse.module.command.mail.model;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.StringMessageContext;
import org.jspecify.annotations.NonNull;

public interface MailMessageContext extends StringMessageContext {

    static MailMessageContextImpl.MailMessageContextImplBuilder builder() {
        return MailMessageContextImpl.builder();
    }

    @NonNull Mail mail();

    @NonNull FPlayer target();

    MailMessageContext withMail(@NonNull Mail mail);

    MailMessageContext withTarget(@NonNull FPlayer target);

}