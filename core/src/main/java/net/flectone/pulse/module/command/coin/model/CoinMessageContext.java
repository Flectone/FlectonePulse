package net.flectone.pulse.module.command.coin.model;

import net.flectone.pulse.model.event.message.context.MessageContext;

public interface CoinMessageContext extends MessageContext {

    static CoinMessageContextImpl.CoinMessageContextImplBuilder builder() {
        return CoinMessageContextImpl.builder();
    }

    int percent();

    CoinMessageContext withPercent(int percent);

}