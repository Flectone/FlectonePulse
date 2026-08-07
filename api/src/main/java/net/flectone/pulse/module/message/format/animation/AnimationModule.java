package net.flectone.pulse.module.message.format.animation;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.constant.ModuleName;

import java.util.UUID;

/**
 * Cycles a message through a list of frames so it appears animated.
 * @author TheFaser
 */
public interface AnimationModule extends ModuleLocalization {

    @Override
    void onEnable();

    @Override
    Localization.Message.Format.Animation localization(FPlayer fPlayer);

    @Override
    ModuleName name();

    @Override
    Message.Format.Animation config();

    @Override
    Permission.Message.Format.Animation permission();

    /**
     * Registers the tag that renders an animation frame.
     *
     * @param messageContext the message being formatted
     * @return the context with the tag added
     */
    MessageContext addTag(MessageContext messageContext);

    /**
     * Advances an animation to its next frame.
     *
     * @param sender who wrote the message
     * @param receiver who reads it
     * @param animation the animation name
     * @param maxInterval how many calls each frame lasts
     * @param maxIndex how many frames there are
     * @return the frame to draw
     */
    int increment(UUID sender, UUID receiver, String animation, int maxInterval, int maxIndex);

    /**
     * Identifies one running animation, so each reader sees their own progress.
     *
     * @param sender who wrote the message
     * @param receiver who reads it
     * @param animation the animation name
     */
    record AnimationKey(UUID sender, UUID receiver, String animation) {}

}
