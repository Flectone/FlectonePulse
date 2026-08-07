package net.flectone.pulse.module.command.chatsetting.builder;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.chatsetting.model.SubMenuItem;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public interface MenuBuilder {

    void open(FPlayer fPlayer, UUID fTargetUUID);

    void openSubMenu(FPlayer fPlayer, UUID fTargetUUID, Component header, Runnable closeConsumer, List<SubMenuItem> items, Function<SubMenuItem, String> getItemMessage, Consumer<SubMenuItem> onSelect, @Nullable String id);

}
