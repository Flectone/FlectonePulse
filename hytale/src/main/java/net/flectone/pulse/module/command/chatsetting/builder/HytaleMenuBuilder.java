package net.flectone.pulse.module.command.chatsetting.builder;

import au.ellie.hyui.builders.*;
import au.ellie.hyui.elements.LayoutModeSupported;
import au.ellie.hyui.events.UIContext;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import eu.mikart.adventure.platform.hytale.HytaleComponentSerializer;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.chatsetting.ChatsettingModule;
import net.flectone.pulse.module.command.chatsetting.handler.ChatsettingHandler;
import net.flectone.pulse.module.command.chatsetting.model.SubMenuItem;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytaleMenuBuilder implements MenuBuilder {

    private final ChatsettingModule chatsettingModule;
    private final MessagePipeline messagePipeline;
    private final ChatsettingHandler chatsettingHandler;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final FPlayerService fPlayerService;

    @Override
    public void open(FPlayer fPlayer, UUID fTargetUUID) {
        Object platformPlayer = platformPlayerAdapter.convertToPlatformPlayer(fPlayer);
        if (!(platformPlayer instanceof PlayerRef playerRef)) return;

        Ref<EntityStore> refStore = playerRef.getReference();
        if (refStore == null) return;

        UUID worldUUID = playerRef.getWorldUuid();
        if (worldUUID == null) return;

        Universe universe = Universe.get();
        if (universe == null) return;

        World world = universe.getWorld(worldUUID);
        if (world == null) return;

        FPlayer fTarget = fPlayerService.getFPlayer(fTargetUUID);

        Localization.Command.Chatsetting localization = chatsettingModule.localization(fPlayer);
        MessageContext headerContext = messagePipeline.createContext(fPlayer, fTarget, localization.inventory().trim());
        Component header = messagePipeline.build(headerContext);

        GridGroup gridGroup = new GridGroup(config().columns());

        gridGroup = createDialogChatMenu(fPlayer, fTarget, gridGroup, localization);
        gridGroup = createDialogFColorMenu(fPlayer, fTarget, FColor.Type.SEE, gridGroup, chatsettingModule.config().menu().see(), localization.menu().see());
        gridGroup = createDialogFColorMenu(fPlayer, fTarget, FColor.Type.OUT, gridGroup, chatsettingModule.config().menu().out(), localization.menu().out());

        for (String setting : chatsettingModule.config().checkbox().types().keySet()) {
            gridGroup = createDialogCheckbox(fPlayer, fTarget, gridGroup, setting);
        }

        PageBuilder pageBuilder = createPage(playerRef, header, gridGroup);

        world.execute(() -> pageBuilder.open(refStore.getStore()));
    }

    private GridGroup createDialogCheckbox(FPlayer fPlayer, FPlayer fTarget, GridGroup gridGroup, String messageType) {
        Command.Chatsetting.Checkbox checkbox = chatsettingModule.config().checkbox();

        int slot = checkbox.types().get(messageType);
        if (slot == -1) return gridGroup;

        boolean enabled = fTarget.isSetting(messageType);

        String title = chatsettingModule.getCheckboxTitle(fPlayer, messageType, enabled);
        MessageContext titleContext = messagePipeline.createContext(fPlayer, fTarget, title);
        Component componentTitle = messagePipeline.build(titleContext);

        String lore = chatsettingModule.getCheckboxLore(fPlayer, enabled);
        MessageContext loreContext = messagePipeline.createContext(fPlayer, fTarget, lore);
        Component componentLore = messagePipeline.build(loreContext);

        String id = "fp_" + UUID.randomUUID();

        return gridGroup.append(createButton(id, componentTitle, componentLore, (unused, uiContext) -> {
            ChatsettingHandler.Status status = chatsettingHandler.handleCheckbox(fPlayer, fTarget, messageType);
            if (status == ChatsettingHandler.Status.DENIED) return;

            FPlayer finalFTarget = fPlayerService.getFPlayer(fTarget);
            boolean currentEnabled = status.toBoolean();

            String invertTitle = chatsettingModule.getCheckboxTitle(fPlayer, messageType, !currentEnabled);
            MessageContext invertTitleContext = messagePipeline.createContext(fPlayer, finalFTarget, invertTitle);
            Component componentInvertTitle = messagePipeline.build(invertTitleContext);

            String invertLore = chatsettingModule.getCheckboxLore(fPlayer, !currentEnabled);
            MessageContext invertLoreContext = messagePipeline.createContext(fPlayer, finalFTarget, invertLore);
            Component componentInvertLore = messagePipeline.build(invertLoreContext);

            uiContext.getById(id, ButtonBuilder.class).ifPresent(buttonBuilder -> buttonBuilder
                    .withText(PlainTextComponentSerializer.plainText().serialize(componentInvertTitle))
                    .withTooltipTextSpan(HytaleComponentSerializer.get().serialize(componentInvertLore))
            );

            uiContext.updatePage(true);

            chatsettingModule.saveSetting(finalFTarget, messageType);
        }));
    }

    private GridGroup createDialogChatMenu(FPlayer fPlayer,
                                           FPlayer fTarget,
                                           GridGroup gridGroup,
                                           Localization.Command.Chatsetting localization) {
        Command.Chatsetting.Menu.Chat chat = chatsettingModule.config().menu().chat();

        int slot = chat.slot();
        if (slot == -1) return gridGroup;

        String currentChat = chatsettingModule.getPlayerChat(fTarget);

        String[] messages = Strings.CS.replace(
                localization.menu().chat().item(),
                "<chat>", currentChat
        ).split("<br>");

        String title = messages.length > 0 ? messages[0] : "";
        String lore = messages.length > 1 ? String.join("<br>", Arrays.copyOfRange(messages, 1, messages.length)) : "";

        MessageContext titleContext = messagePipeline.createContext(fTarget, title);
        Component componentTitle = messagePipeline.build(titleContext);

        MessageContext loreContext = messagePipeline.createContext(fTarget, lore);
        Component componentLore = messagePipeline.build(loreContext);

        String id = "fp_chat";

        return gridGroup.append(createButton(id, componentTitle, componentLore, (unused, uiContext) ->
                chatsettingHandler.handleChatMenu(fPlayer, fTarget, chat, localization, this, id)
        ));
    }

    private GridGroup createDialogFColorMenu(FPlayer fPlayer,
                                             FPlayer fTarget,
                                             FColor.Type type,
                                             GridGroup gridGroup,
                                             Command.Chatsetting.Menu.Color color,
                                             Localization.Command.Chatsetting.Menu.SubMenu subMenu) {
        int slot = color.slot();
        if (slot == -1) return gridGroup;

        String[] messages = subMenu.item().split("<br>");

        String title = messages.length > 0 ? messages[0] : "";
        String lore = messages.length > 1 ? String.join("<br>", Arrays.copyOfRange(messages, 1, messages.length)) : "";

        MessageContext titleContext = messagePipeline.createContext(fTarget, title);
        Component componentTitle = messagePipeline.build(titleContext);

        MessageContext loreContext = messagePipeline.createContext(fTarget, lore);
        Component componentLore = messagePipeline.build(loreContext);

        String id = "fp_fcolor_" + type.ordinal();

        return gridGroup.append(createButton(id, componentTitle, componentLore, (unused, uiContext) ->
                chatsettingHandler.handleFColorMenu(fPlayer, fTarget, type, color, subMenu, this, id)
        ));
    }

    @Override
    public void openSubMenu(FPlayer fPlayer,
                            UUID fTargetUUID,
                            Component header,
                            Runnable closeConsumer,
                            List<SubMenuItem> items,
                            Function<SubMenuItem, String> getItemMessage,
                            Consumer<SubMenuItem> onSelect,
                            String id) {
        Object platformPlayer = platformPlayerAdapter.convertToPlatformPlayer(fPlayer);
        if (!(platformPlayer instanceof PlayerRef playerRef)) return;

        Ref<EntityStore> refStore = playerRef.getReference();
        if (refStore == null) return;

        UUID worldUUID = playerRef.getWorldUuid();
        if (worldUUID == null) return;

        Universe universe = Universe.get();
        if (universe == null) return;

        World world = universe.getWorld(worldUUID);
        if (world == null) return;

        GridGroup gridGroup = new GridGroup(config().columns());

        FPlayer fTarget = fPlayerService.getFPlayer(fTargetUUID);

        for (int i = 0; i < items.size(); i++) {
            SubMenuItem item = items.get(i);
            String message = getItemMessage.apply(item);
            String[] messages = message.split("<br>");

            String title = messages.length > 0 ? messages[0] : "";
            String lore = messages.length > 1 ? String.join("<br>", Arrays.copyOfRange(messages, 1, messages.length)) : "";

            MessageContext titleContext = messagePipeline.createContext(fTarget, title);
            Component componentTitle = messagePipeline.build(titleContext);

            MessageContext loreContext = messagePipeline.createContext(fTarget, lore);
            Component componentLore = messagePipeline.build(loreContext);

            String subId = id + "_" + i;

            gridGroup = gridGroup.append(createButton(subId, componentTitle, componentLore, (unused, uiContext) ->
                    chatsettingHandler.handleSubMenu(fPlayer, item, () -> {
                        onSelect.accept(item);
                        closeConsumer.run();
                        open(fPlayer, fTargetUUID);
                    })));
        }

        PageBuilder pageBuilder = createPage(playerRef, header, gridGroup);

        world.execute(() -> pageBuilder.open(refStore.getStore()));
    }

    private record GridGroup(
            int maxPerLine,
            int currentLine,
            GroupBuilder mainGroup,
            GroupBuilder rowGroup
    ) {

        public GridGroup(int columns) {
            this(columns, 0, GroupBuilder.group().withLayoutMode(LayoutModeSupported.LayoutMode.Top), GroupBuilder.group().withLayoutMode(LayoutModeSupported.LayoutMode.Center));
        }

        public GroupBuilder buildMainGroup() {
            return mainGroup.addChild(rowGroup);
        }

        public GridGroup append(@Nullable UIElementBuilder<?> uiElementBuilder) {
            GridGroup gridGroup = this;
            if (uiElementBuilder == null) return gridGroup;
            if (gridGroup.currentLine == gridGroup.maxPerLine) {
                gridGroup = new GridGroup(
                        gridGroup.maxPerLine,
                        0,
                        gridGroup.buildMainGroup(),
                        GroupBuilder.group().withLayoutMode(LayoutModeSupported.LayoutMode.Center)
                );
            }

            return new GridGroup(
                    gridGroup.maxPerLine,
                    gridGroup.currentLine + 1,
                    gridGroup.mainGroup,
                    gridGroup.rowGroup.addChild(uiElementBuilder)
            );
        }

    }

    private ButtonBuilder createButton(String id, Component componentTitle, Component componentLore, BiConsumer<Void, UIContext> callback) {
        return ButtonBuilder.textButton()
                .withId(id)
                .withText(PlainTextComponentSerializer.plainText().serialize(componentTitle))
                .withTooltipTextSpan(HytaleComponentSerializer.get().serialize(componentLore))
                .withAnchor(new HyUIAnchor()
                        .setWidth(config().buttonWidth())
                        .setHeight(config().buttonHeight())
                )
                .withPadding(new HyUIPadding().setFull(config().buttonPadding()))
                .addEventListener(CustomUIEventBindingType.Activating, callback);
    }

    private PageBuilder createPage(PlayerRef playerRef, Component title, GridGroup gridGroup) {
        return PageBuilder.pageForPlayer(playerRef)
                .withLifetime(CustomPageLifetime.CanDismissOrCloseThroughInteraction)
                .addElement(PageOverlayBuilder.pageOverlay()
                        .addChild(ContainerBuilder.container()
                                .withTitleText(PlainTextComponentSerializer.plainText().serialize(title))
                                .withAnchor(new HyUIAnchor()
                                        .setWidth(config().panelWidth())
                                        .setHeight(config().panelHeight())
                                )
                                .addChild(gridGroup.buildMainGroup())
                        )
                );
    }

    private Command.Chatsetting.Modern config() {
        return chatsettingModule.config().modern();
    }
}