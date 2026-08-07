package net.flectone.pulse.module.message.format;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.util.constant.AdventureTag;
import net.flectone.pulse.util.constant.ModuleName;
import org.jspecify.annotations.NonNull;

/**
 * Groups the formatting steps applied to a message, and decides which MiniMessage tags the sender may use.
 * @author TheFaser
 */
public interface FormatModule extends ModuleLocalization {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    ModuleName name();

    @Override
    Message.Format config();

    @Override
    Permission.Message.Format permission();

    @Override
    Localization.Message.Format localization(FPlayer fPlayer);

    /**
     * Registers the tags every formatting module contributes.
     *
     * @param messageContext the message being formatted
     * @return the context with the tags added
     */
    MessageContext addTags(MessageContext messageContext);

    /**
     * Whether a sender may use a MiniMessage tag.
     *
     * @param adventureTag the tag
     * @param sender who wrote the message
     * @param needPermission whether the sender's permission is checked, or only the config
     * @return true if the tag is allowed
     */
    boolean isCorrectTag(AdventureTag adventureTag, FEntity sender, boolean needPermission);

}
