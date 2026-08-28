package net.flectone.pulse.module.message.format.questionanswer;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleLocalization;

/**
 * Answers recognized questions in chat automatically.
 * @author TheFaser
 */
public interface QuestionAnswerModule extends ModuleLocalization {

    @Override
    Message.Format.QuestionAnswer config();

    @Override
    Permission.Message.Format.QuestionAnswer permission();

    @Override
    Localization.Message.Format.QuestionAnswer localization(FPlayer fPlayer);

    /**
     * Looks for a recognised question and queues the automatic reply.
     *
     * @param messageContext the message being formatted
     * @return the formatted context
     */
    MessageContext format(MessageContext messageContext);

    /**
     * Registers the tag that renders the matched question.
     *
     * @param messageContext the message being formatted
     * @return the context with the tag added
     */
    MessageContext addTag(MessageContext messageContext);

}
