package net.flectone.pulse.module.command.chatsetting.builder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.value.FColor;
import net.flectone.pulse.module.command.chatsetting.ChatsettingModule;
import net.flectone.pulse.module.command.chatsetting.handler.ChatsettingHandler;
import net.flectone.pulse.module.command.chatsetting.model.SubMenuItem;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.serializer.HytaleComponentSerializer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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
    private final HytaleComponentSerializer componentSerializer;
    private final SocialService socialService;

    @Override
    public void open(FPlayer fPlayer, UUID fTargetUUID) {
        PlayerRef playerRef = playerRef(fPlayer);
        if (playerRef == null) return;

        Ref<EntityStore> refStore = playerRef.getReference();
        if (refStore == null) return;

        World world = refStore.getStore().getExternalData().getWorld();
        if (world == null) return;

        FPlayer fTarget = fPlayerService.getFPlayer(fTargetUUID);
        Localization.Command.Chatsetting localization = chatsettingModule.localization(fPlayer);

        Component header = messagePipeline.build(MessageContext.builder()
                .module(chatsettingModule.name())
                .sender(fPlayer)
                .receiver(fTarget)
                .message(localization.inventory().trim())
                .build()
        );

        List<MenuButton> buttons = new ArrayList<>();

        MenuButton chatButton = createChatMenu(fPlayer, fTarget, localization);
        if (chatButton != null) buttons.add(chatButton);

        MenuButton seeButton = createFColorMenu(fPlayer, fTarget, FColor.Type.SEE, chatsettingModule.config().menu().see(), localization.menu().see());
        if (seeButton != null) buttons.add(seeButton);

        MenuButton outButton = createFColorMenu(fPlayer, fTarget, FColor.Type.OUT, chatsettingModule.config().menu().out(), localization.menu().out());
        if (outButton != null) buttons.add(outButton);

        for (String setting : chatsettingModule.config().checkbox().types().keySet()) {
            MenuButton checkboxButton = createCheckbox(fPlayer, fTarget, setting);
            if (checkboxButton == null) continue;

            buttons.add(checkboxButton);
        }

        world.execute(() -> openPage(playerRef, refStore, header, buttons));
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
        PlayerRef playerRef = playerRef(fPlayer);
        if (playerRef == null) return;

        Ref<EntityStore> refStore = playerRef.getReference();
        if (refStore == null) return;

        World world = refStore.getStore().getExternalData().getWorld();
        if (world == null) return;

        FPlayer fTarget = fPlayerService.getFPlayer(fTargetUUID);
        List<MenuButton> buttons = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            SubMenuItem item = items.get(i);
            String[] messages = getItemMessage.apply(item).split("<br>");

            Component title = buildComponent(fTarget, messages.length > 0 ? messages[0] : "");
            Component lore = buildComponent(fTarget, messages.length > 1 ? String.join("<br>", Arrays.copyOfRange(messages, 1, messages.length)) : "");

            String subId = id + "_" + i;

            buttons.add(new MenuButton(subId, title, lore, _ ->
                    chatsettingHandler.handleSubMenu(fPlayer, item, () -> {
                        onSelect.accept(item);
                        closeConsumer.run();
                        open(fPlayer, fTargetUUID);
                    })
            ));
        }

        world.execute(() -> openPage(playerRef, refStore, header, buttons));
    }

    private @Nullable MenuButton createCheckbox(FPlayer fPlayer, FPlayer fTarget, String messageType) {
        Command.Chatsetting.Checkbox checkbox = chatsettingModule.config().checkbox();

        int slot = checkbox.types().get(messageType);
        if (slot == -1) return null;

        boolean enabled = socialService.isSetting(fTarget, messageType);

        Component title = buildCheckboxTitle(fPlayer, fTarget, messageType, enabled);
        Component lore = buildCheckboxLore(fPlayer, fTarget, enabled);

        String id = "fp_checkbox_" + messageType;

        return new MenuButton(id, title, lore, page -> {
            ChatsettingHandler.Status status = chatsettingHandler.handleCheckbox(fPlayer, fTarget, messageType);
            if (status == ChatsettingHandler.Status.DENIED) return;

            boolean currentEnabled = status.toBoolean();

            page.updateButton(
                    id,
                    buildCheckboxTitle(fPlayer, fTarget, messageType, !currentEnabled),
                    buildCheckboxLore(fPlayer, fTarget, !currentEnabled)
            );
        });
    }

    private Component buildCheckboxTitle(FPlayer fPlayer, FPlayer fTarget, String messageType, boolean enabled) {
        return messagePipeline.build(MessageContext.builder()
                .module(chatsettingModule.name())
                .sender(fPlayer)
                .receiver(fTarget)
                .message(chatsettingModule.getCheckboxTitle(fPlayer, messageType, enabled))
                .build()
        );
    }

    private Component buildCheckboxLore(FPlayer fPlayer, FPlayer fTarget, boolean enabled) {
        return messagePipeline.build(MessageContext.builder()
                .module(chatsettingModule.name())
                .sender(fPlayer)
                .receiver(fTarget)
                .message(chatsettingModule.getCheckboxLore(fPlayer, enabled))
                .build()
        );
    }

    private @Nullable MenuButton createChatMenu(FPlayer fPlayer,
                                                FPlayer fTarget,
                                                Localization.Command.Chatsetting localization) {
        Command.Chatsetting.Menu.Chat chat = chatsettingModule.config().menu().chat();

        int slot = chat.slot();
        if (slot == -1) return null;

        String currentChat = chatsettingModule.getPlayerChat(fTarget);

        String[] messages = Strings.CS.replace(
                localization.menu().chat().item(),
                "<chat>", localization.menu().chat().types().getOrDefault(currentChat, currentChat)
        ).split("<br>");

        Component title = buildComponent(fTarget, messages.length > 0 ? messages[0] : "");
        Component lore = buildComponent(fTarget, messages.length > 1 ? String.join("<br>", Arrays.copyOfRange(messages, 1, messages.length)) : "");

        String id = "fp_chat";

        return new MenuButton(id, title, lore, _ ->
                chatsettingHandler.handleChatMenu(fPlayer, fTarget, chat, localization, this, id)
        );
    }

    private @Nullable MenuButton createFColorMenu(FPlayer fPlayer,
                                                  FPlayer fTarget,
                                                  FColor.Type type,
                                                  Command.Chatsetting.Menu.Color color,
                                                  Localization.Command.Chatsetting.Menu.SubMenu subMenu) {
        int slot = color.slot();
        if (slot == -1) return null;

        String[] messages = subMenu.item().split("<br>");

        Component title = buildComponent(fTarget, messages.length > 0 ? messages[0] : "");
        Component lore = buildComponent(fTarget, messages.length > 1 ? String.join("<br>", Arrays.copyOfRange(messages, 1, messages.length)) : "");

        String id = "fp_fcolor_" + type.ordinal();

        return new MenuButton(id, title, lore, _ ->
                chatsettingHandler.handleFColorMenu(fPlayer, fTarget, type, color, subMenu, this, id)
        );
    }

    private Component buildComponent(FPlayer sender, String message) {
        return messagePipeline.build(MessageContext.builder()
                .module(chatsettingModule.name())
                .sender(sender)
                .message(message)
                .build()
        );
    }

    private void openPage(PlayerRef playerRef, Ref<EntityStore> refStore, Component title, List<MenuButton> buttons) {
        Store<EntityStore> store = refStore.getStore();
        Player player = store.getComponent(refStore, Player.getComponentType());
        if (player == null) return;

        player.getPageManager().openCustomPage(refStore, store, new HytaleChatsettingPage(
                playerRef,
                title,
                buttons,
                componentSerializer,
                config()
        ));
    }

    private @Nullable PlayerRef playerRef(FPlayer fPlayer) {
        return platformPlayerAdapter.convertToPlatformPlayer(fPlayer) instanceof PlayerRef playerRef ? playerRef : null;
    }

    private Command.Chatsetting.Modern config() {
        return chatsettingModule.config().modern();
    }

}
