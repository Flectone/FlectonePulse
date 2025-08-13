package net.flectone.pulse.module.command.chatsetting.builder;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.chatsetting.model.SubMenuItem;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface MenuBuilder {

    void open(FPlayer fPlayer, FPlayer fTarget);

    void openSubMenu(FPlayer fPlayer,
                     FPlayer fTarget,
                     Component header,
                     Runnable closeConsumer,
                     List<SubMenuItem> items,
                     Function<SubMenuItem, String> getItemMessage,
                     Consumer<SubMenuItem> onSelect,
                     @Nullable String id);
}
