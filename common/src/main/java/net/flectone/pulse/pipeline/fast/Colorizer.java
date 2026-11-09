package net.flectone.pulse.pipeline.fast;

import net.kyori.adventure.text.format.TextColor;
import org.jspecify.annotations.NonNull;

interface Colorizer {

    void init(int size);

    @NonNull TextColor color();

    void advance();

}
