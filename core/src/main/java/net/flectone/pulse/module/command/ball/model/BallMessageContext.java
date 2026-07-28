package net.flectone.pulse.module.command.ball.model;

import net.flectone.pulse.model.event.message.context.StringMessageContext;

public interface BallMessageContext extends StringMessageContext {

    static BallMessageContextImpl.BallMessageContextImplBuilder builder() {
        return BallMessageContextImpl.builder();
    }

    int answer();

    BallMessageContext withAnswer(int answer);

}