package net.flectone.pulse.module.message.bubble.render;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.bubble.model.Bubble;

import java.util.function.Predicate;

public interface BubbleRender {

    void renderBubble(Bubble bubble);

    void removeBubbleIf(Predicate<Bubble> bubbleEntityPredicate);

    void removeAllBubbles();

    boolean isCorrectPlayer(FPlayer sender);

    boolean isModern();

    boolean isInteractionRiding();

}
