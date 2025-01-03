package net.flectone.pulse.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Ticker {

    private final boolean enable;
    private final long period;

    public Ticker() {
        this.enable = false;
        this.period = 100L;
    }
}
