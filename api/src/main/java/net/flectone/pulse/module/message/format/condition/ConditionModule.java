package net.flectone.pulse.module.message.format.condition;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Picks between alternative texts based on a condition, such as a permission or a placeholder value.
 * @author TheFaser
 */
public interface ConditionModule extends ModuleLocalization {

    @Override
    void onEnable();

    @Override
    Localization.Message.Format.Condition localization(FPlayer fPlayer);

    @Override
    ModuleName name();

    @Override
    Message.Format.Condition config();

    @Override
    Permission.Message.Format.Condition permission();

    /**
     * Registers the tag that picks between alternative texts.
     *
     * @param messageContext the message being formatted
     * @return the context with the tag added
     */
    MessageContext addTag(MessageContext messageContext);

    /**
     * The value a condition currently has for a player.
     *
     * @param conditionName the condition
     * @param fPlayer the player
     * @return the value, or an empty string if the condition is unknown
     */
    @Nullable
    String getConditionValue(String conditionName, FPlayer fPlayer);

    /**
     * The value a condition has for a sender and reader pair, with the message flags in scope.
     *
     * @param conditionName the condition
     * @param fPlayer who wrote the message
     * @param fReceiver who reads it
     * @param flags the formatting switches
     * @return the value, or an empty string if the condition is unknown
     */
    @Nullable
    String getConditionValue(String conditionName, FEntity fPlayer, FPlayer fReceiver, Map<MessageFlag, Boolean> flags);

}
