package net.flectone.pulse.module.command.clearmail.model;

import net.flectone.pulse.model.event.message.context.StringMessageContext;
import net.flectone.pulse.module.command.mail.model.Mail;
import org.jspecify.annotations.NonNull;

public interface ClearMessageContext extends StringMessageContext {

    static ClearMessageContextImpl.ClearMessageContextImplBuilder builder() {
        return ClearMessageContextImpl.builder();
    }

    @NonNull Mail mail();

    ClearMessageContext withMail(@NonNull Mail mail);

}