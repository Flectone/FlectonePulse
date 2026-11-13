package net.flectone.pulse.module.command.chatsetting.builder;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.chatsetting.model.SubMenuItem;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A menu builder for platforms without a chatsetting menu.
 * @author TheFaser
 */
public final class DummyMenuBuilder implements MenuBuilder {

    public static final DummyMenuBuilder INSTANCE = new DummyMenuBuilder();

    private DummyMenuBuilder() {
    }

    @Override
    public void open(FPlayer fPlayer, UUID fTargetUUID) {
        // chatsetting menus are a platform feature, nothing to open by default
    }

    @Override
    public void openSubMenu(FPlayer fPlayer,
                            UUID fTargetUUID,
                            Component header,
                            Runnable closeConsumer,
                            List<SubMenuItem> items,
                            Function<SubMenuItem, String> getItemMessage,
                            Consumer<SubMenuItem> onSelect,
                            @Nullable String id) {
        // chatsetting menus are a platform feature, nothing to open by default
    }

}
