package net.flectone.pulse.module.command.chatsetting.builder;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.serializer.HytaleComponentSerializer;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HytaleChatsettingPage extends InteractiveCustomUIPage<HytaleChatsettingPage.MenuEvent> {

    private static final String PAGE = "Pages/FlectonePulse/Chatsetting.ui";
    private static final String ROW = "Pages/FlectonePulse/ChatsettingRow.ui";
    private static final String BUTTON = "Pages/FlectonePulse/ChatsettingButton.ui";
    private static final String BUTTON_ID = "#ChatsettingButton";
    private static final String CENTER = "#ChatsettingCenter";
    private static final String GRID = "#ChatsettingGrid";
    private static final String PANEL = "#ChatsettingPanel";
    private static final String TITLE = "#ChatsettingTitle";

    private final Component title;
    private final List<MenuButton> buttons;
    private final Map<String, Integer> indexes;
    private final HytaleComponentSerializer componentSerializer;
    private final Command.Chatsetting.Modern config;
    private final int columns;
    private final int spacing;

    public HytaleChatsettingPage(PlayerRef playerRef,
                                 Component title,
                                 List<MenuButton> buttons,
                                 HytaleComponentSerializer componentSerializer,
                                 Command.Chatsetting.Modern config) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, MenuEvent.CODEC);

        this.title = title;
        this.buttons = buttons;
        this.componentSerializer = componentSerializer;
        this.config = config;
        this.columns = Math.max(1, config.columns());
        this.spacing = Math.max(0, config.buttonPadding());
        this.indexes = new HashMap<>();

        for (int i = 0; i < buttons.size(); i++) {
            indexes.put(buttons.get(i).id(), i);
        }
    }

    @Override
    public void build(Ref<EntityStore> ref, UICommandBuilder commands, UIEventBuilder events, Store<EntityStore> store) {
        commands.append(PAGE);
        commands.setObject(PANEL + ".Anchor", createPanelAnchor());
        commands.set(TITLE + ".TextSpans", componentSerializer.toHytale(title));
        commands.set(CENTER + ".LayoutMode", "CenterMiddle");
        commands.set(GRID + ".LayoutMode", "Top");
        commands.setObject(GRID + ".Anchor", createGridAnchor());

        for (int i = 0; i < buttons.size(); i++) {
            MenuButton button = buttons.get(i);

            int row = i / columns;
            int column = i % columns;
            String rowSelector = GRID + "[" + row + "]";

            if (column == 0) {
                commands.append(GRID, ROW);
                commands.setObject(rowSelector + ".Anchor", createRowAnchor());
            }

            commands.append(rowSelector, BUTTON);

            String selector = rowSelector + "[" + column + "]";
            String buttonSelector = selector + " " + BUTTON_ID;
            applyButton(commands, selector, buttonSelector, button);
            events.addEventBinding(CustomUIEventBindingType.Activating, buttonSelector, EventData.of("Action", button.id()), false);
        }
    }

    @Override
    public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, MenuEvent event) {
        Integer index = indexes.get(event.action);
        if (index == null) return;

        buttons.get(index).action().accept(this);
    }

    public void updateButton(String id, Component title, Component lore) {
        Integer index = indexes.get(id);
        if (index == null) return;

        String selector = GRID + "[" + (index / columns) + "][" + (index % columns) + "] " + BUTTON_ID;

        UICommandBuilder commands = new UICommandBuilder();
        commands.set(selector + ".TextSpans", componentSerializer.toHytale(title));
        commands.set(selector + ".TooltipTextSpans", componentSerializer.toHytale(lore));

        sendUpdate(commands);
    }

    private void applyButton(UICommandBuilder commands, String selector, String buttonSelector, MenuButton button) {
        commands.setObject(selector + ".Anchor", createButtonAnchor());
        commands.setObject(buttonSelector + ".Anchor", createButtonInnerAnchor());

        commands.set(selector + ".Padding.Left", spacing / 2);
        commands.set(selector + ".Padding.Right", spacing - spacing / 2);
        commands.set(selector + ".Padding.Top", spacing / 2);
        commands.set(selector + ".Padding.Bottom", spacing - spacing / 2);

        commands.set(buttonSelector + ".TextSpans", componentSerializer.toHytale(button.title()));
        commands.set(buttonSelector + ".TooltipTextSpans", componentSerializer.toHytale(button.lore()));

        commands.set(buttonSelector + ".Padding.Left", config.buttonPadding());
        commands.set(buttonSelector + ".Padding.Top", config.buttonPadding());
        commands.set(buttonSelector + ".Padding.Right", config.buttonPadding());
        commands.set(buttonSelector + ".Padding.Bottom", config.buttonPadding());
    }

    private Anchor createPanelAnchor() {
        Anchor anchor = new Anchor();
        anchor.setWidth(Value.of(config.panelWidth()));
        anchor.setHeight(Value.of(config.panelHeight()));
        return anchor;
    }

    private Anchor createButtonAnchor() {
        Anchor anchor = new Anchor();
        anchor.setWidth(Value.of(config.buttonWidth() + spacing));
        anchor.setHeight(Value.of(config.buttonHeight() + spacing));
        return anchor;
    }

    private Anchor createButtonInnerAnchor() {
        Anchor anchor = new Anchor();
        anchor.setWidth(Value.of(config.buttonWidth()));
        anchor.setHeight(Value.of(config.buttonHeight()));
        return anchor;
    }

    private Anchor createRowAnchor() {
        Anchor anchor = new Anchor();
        anchor.setWidth(Value.of(columns * (config.buttonWidth() + spacing)));
        anchor.setHeight(Value.of(config.buttonHeight() + spacing));
        return anchor;
    }

    private Anchor createGridAnchor() {
        Anchor anchor = new Anchor();
        anchor.setWidth(Value.of(columns * (config.buttonWidth() + spacing)));
        return anchor;
    }

    public static final class MenuEvent {

        public static final BuilderCodec<MenuEvent> CODEC = BuilderCodec.builder(MenuEvent.class, MenuEvent::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (event, action) -> event.action = action, event -> event.action)
                .add()
                .build();

        private String action;

    }

}
