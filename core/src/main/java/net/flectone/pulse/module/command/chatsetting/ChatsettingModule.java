package net.flectone.pulse.module.command.chatsetting;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.inventory.Inventory;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.InventoryController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Singleton
public class ChatsettingModule extends AbstractModuleCommand<Localization.Command.Chatsetting> {

    private final Permission.Message.Chat chatPermission;
    private final Command.Chatsetting command;
    private final Permission.Command.Chatsetting permission;
    private final FPlayerService fPlayerService;
    private final MessagePipeline messagePipeline;
    private final PermissionChecker permissionChecker;
    private final InventoryController inventoryController;
    private final PlatformServerAdapter platformServerAdapter;
    private final CommandParserProvider commandParserProvider;
    private final ProxySender proxySender;
    private final ProxyRegistry proxyRegistry;
    private final boolean modernVersion;

    @Inject
    public ChatsettingModule(FileResolver fileResolver,
                             FPlayerService fPlayerService,
                             MessagePipeline messagePipeline,
                             PermissionChecker permissionChecker,
                             InventoryController inventoryController,
                             PlatformServerAdapter platformServerAdapter,
                             CommandParserProvider commandParserProvider,
                             ProxySender proxySender,
                             ProxyRegistry proxyRegistry,
                             PacketProvider packetProvider) {
        super(localization -> localization.getCommand().getChatsetting(), Command::getChatsetting);

        this.chatPermission = fileResolver.getPermission().getMessage().getChat();
        this.command = fileResolver.getCommand().getChatsetting();
        this.permission = fileResolver.getPermission().getCommand().getChatsetting();
        this.fPlayerService = fPlayerService;
        this.messagePipeline = messagePipeline;
        this.permissionChecker = permissionChecker;
        this.inventoryController = inventoryController;
        this.platformServerAdapter = platformServerAdapter;
        this.commandParserProvider = commandParserProvider;
        this.proxySender = proxySender;
        this.proxyRegistry = proxyRegistry;
        this.modernVersion = packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_14);
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        permission.getSettings().values().forEach(this::registerPermission);

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::getPlayer);
        String promptType = addPrompt(1, Localization.Command.Prompt::getType);
        String promptValue = addPrompt(2, Localization.Command.Prompt::getValue);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission.getName())
                .optional(promptPlayer, commandParserProvider.offlinePlayerParser(), commandParserProvider.playerSuggestionPermission(true, permission.getOther()))
                .optional(promptType, commandParserProvider.singleMessageParser(), typeSuggestion())
                .optional(promptValue, commandParserProvider.messageParser())
        );

        addPredicate(this::checkCooldown);
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> typeSuggestion() {
        return (context, input) -> {
            if (!permissionChecker.check(context.sender(), permission.getOther())) return Collections.emptyList();

            return Arrays.stream(FPlayer.Setting.values())
                    .map(setting -> Suggestion.suggestion(setting.name()))
                    .toList();
        };
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer)) return;

        // example /chatsetting TheFaser
        if (permissionChecker.check(fPlayer, permission.getOther())) {
            String promptPlayer = getPrompt(0);
            Optional<String> optionalPlayer = commandContext.optional(promptPlayer);
            if (optionalPlayer.isPresent()) {
                executeOther(fPlayer, optionalPlayer.get(), commandContext);
                return;
            }
        }

        // default command usage
        sendSettingInventory(fPlayer);

        playSound(fPlayer);
    }

    private void executeOther(FPlayer fPlayer, String target, CommandContext<FPlayer> commandContext) {
        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) return;

        fPlayerService.loadSettings(fTarget);

        String promptType = getPrompt(1);
        Optional<String> optionalType = commandContext.optional(promptType);

        // GUI
        if (optionalType.isEmpty()) {
            sendSettingInventory(fPlayer, fTarget);
            return;
        }

        // command
        FPlayer.Setting setting = FPlayer.Setting.fromString(optionalType.get());
        if (setting == null) return;

        String promptValue = getPrompt(2);
        Optional<String> optionalValue = commandContext.optional(promptValue);

        if (fTarget.isSetting(setting) && optionalValue.isEmpty()) {
            fTarget.removeSetting(setting);
        } else {
            fTarget.setSetting(setting, optionalValue.orElse(""));
        }

        updateSettings(fTarget);
    }

    private void updateSettings(FPlayer fPlayer) {
        fPlayerService.saveSettings(fPlayer);

        // update proxy players
        if (proxyRegistry.hasEnabledProxy()) {
            proxySender.send(fPlayer, MessageType.COMMAND_CHATSETTING, dataOutputStream -> {});
        }
    }

    private void sendSettingInventory(FPlayer fPlayer) {
        sendSettingInventory(fPlayer, fPlayer);
    }

    private void sendSettingInventory(FPlayer fPlayer, FPlayer fTarget) {
        Localization.Command.Chatsetting localization = resolveLocalization(fTarget);
        Component header = messagePipeline.builder(fTarget, localization.getInventory()).build();

        Inventory.Builder inventoryBuilder = new Inventory.Builder()
                .name(header)
                .size(54)
                .addCloseConsumer(inventory -> updateSettings(fTarget));

        for (FPlayer.Setting setting : FPlayer.Setting.values()) {
            inventoryBuilder = switch (setting) {
                case CHAT -> handleChat(fPlayer, fTarget, inventoryBuilder);
                case COLOR -> handleColor(fPlayer, fTarget, inventoryBuilder);
                case STYLE -> handleStyle(fPlayer, fTarget, inventoryBuilder);
                default -> handleCheckboxItem(fPlayer, fTarget, setting, inventoryBuilder);
            };
        }

        inventoryController.open(fPlayer, inventoryBuilder.build(modernVersion));
    }

    private Inventory.Builder handleChat(FPlayer fPlayer, FPlayer fTarget, Inventory.Builder inventoryBuilder) {
        Command.Chatsetting.Menu menu = command.getMenu();
        Command.Chatsetting.Menu.Chat chat = menu.getChat();

        int slot = chat.getSlot();
        if (slot == -1) return inventoryBuilder;

        String currentChat = fTarget.getSettingValue(FPlayer.Setting.CHAT);
        if (currentChat == null) {
            currentChat = "default";
        }

        String material = menu.getMaterial();

        Localization.Command.Chatsetting localization = resolveLocalization(fTarget);

        String[] messages = localization.getMenu().getChat().getItem()
                .replace("<chat>", currentChat)
                .split("<br>");

        String title = messages.length > 0 ? messages[0] : "";
        String[] lore = messages.length > 1 ? Arrays.copyOfRange(messages, 1, messages.length) : new String[]{};

        return inventoryBuilder
                .addItem(slot, platformServerAdapter.buildItemStack(fTarget, material, title, lore))
                .addClickHandler(slot, (itemStack, inventory) -> {
                    if (!permissionChecker.check(fPlayer, permission.getSettings().get(FPlayer.Setting.CHAT))) {
                        builder(fPlayer)
                                .format(Localization.Command.Chatsetting::getNoPermission)
                                .sendBuilt();
                        return;
                    }

                    String header = localization.getMenu().getChat().getInventory();
                    Component componentHeader = messagePipeline.builder(fTarget, header).build();

                    Inventory.Builder inventoryChatsBuilder = new Inventory.Builder()
                            .name(componentHeader)
                            .size(54)
                            .addCloseConsumer(chatsInventory -> updateSettings(fTarget));

                    for (int i = 0; i < chat.getTypes().size(); i++) {
                        Command.Chatsetting.Menu.Chat.Type chatType = chat.getTypes().get(i);
                        String chatName = chatType.getName();
                        String chatMaterial = chatType.getMaterial();

                        String[] chatMessages = localization.getMenu().getChat().getTypes().getOrDefault(chatName, "")
                                .replace("<chat>", chatName)
                                .split("<br>");

                        String chatTitle = chatMessages.length > 0 ? chatMessages[0] : "";
                        String[] chatLore = chatMessages.length > 1 ? Arrays.copyOfRange(chatMessages, 1, chatMessages.length) : new String[]{};

                        inventoryChatsBuilder = inventoryChatsBuilder
                                .addItem(i, platformServerAdapter.buildItemStack(fTarget, chatMaterial, chatTitle, chatLore))
                                .addClickHandler(i, (chatItemStack, chatInventory) -> {
                                    Permission.IPermission chatTypePermission = chatPermission.getTypes().get(chatName);
                                    if (!permissionChecker.check(fPlayer, chatTypePermission)) {
                                        builder(fPlayer)
                                                .format(Localization.Command.Chatsetting::getNoPermission)
                                                .sendBuilt();
                                        return;
                                    }

                                    if (chatName.equalsIgnoreCase("default")) {
                                        fTarget.setSetting(FPlayer.Setting.CHAT, null);
                                    } else {
                                        fTarget.setSetting(FPlayer.Setting.CHAT, chatName);
                                    }

                                    inventoryController.close(fPlayer.getUuid());
                                    sendSettingInventory(fPlayer, fTarget);
                                });
                    }

                    inventoryController.open(fPlayer, inventoryChatsBuilder.build(modernVersion));
                });
    }

    private Inventory.Builder handleColor(FPlayer fPlayer, FPlayer fTarget, Inventory.Builder inventoryBuilder) {
        Command.Chatsetting.Menu menu = command.getMenu();
        Command.Chatsetting.Menu.Color color = menu.getColor();

        int slot = color.getSlot();
        if (slot == -1) return inventoryBuilder;

        String material = menu.getMaterial();

        Localization.Command.Chatsetting localization = resolveLocalization(fTarget);

        String[] messages = localization.getMenu().getColor().getItem()
                .split("<br>");

        String title = messages.length > 0 ? messages[0] : "";
        String[] lore = messages.length > 1 ? Arrays.copyOfRange(messages, 1, messages.length) : new String[]{};

        return inventoryBuilder
                .addItem(slot, platformServerAdapter.buildItemStack(fTarget, material, title, lore))
                .addClickHandler(slot, (itemStack, inventory) -> {
                    if (!permissionChecker.check(fPlayer, permission.getSettings().get(FPlayer.Setting.COLOR))) {
                        builder(fPlayer)
                                .format(Localization.Command.Chatsetting::getNoPermission)
                                .sendBuilt();
                        return;
                    }

                    String header = localization.getMenu().getColor().getInventory();
                    Component componentHeader = messagePipeline.builder(fTarget, header).build();

                    Inventory.Builder inventoryColorsBuilder = new Inventory.Builder()
                            .name(componentHeader)
                            .size(54)
                            .addCloseConsumer(colorsInventory -> updateSettings(fTarget));

                    for (int i = 0; i < color.getTypes().size(); i++) {
                        Command.Chatsetting.Menu.Color.Type colorType = color.getTypes().get(i);
                        String colorName = colorType.getName();
                        String colorMaterial = colorType.getMaterial();
                        Map<String, String> colors = colorType.getColors();

                        String colorMessage = localization.getMenu().getColor().getTypes().getOrDefault(colorName, "");
                        for (Map.Entry<String, String> entry : colors.entrySet()) {
                            colorMessage = colorMessage.replace("<fcolor:" + entry.getKey() + ">", entry.getValue());
                        }

                        String[] colorMessages = colorMessage.split("<br>");

                        String colorTitle = colorMessages.length > 0 ? colorMessages[0] : "";
                        String[] colorLore = colorMessages.length > 1 ? Arrays.copyOfRange(colorMessages, 1, colorMessages.length) : new String[]{};

                        inventoryColorsBuilder = inventoryColorsBuilder
                                .addItem(i, platformServerAdapter.buildItemStack(fTarget, colorMaterial, colorTitle, colorLore))
                                .addClickHandler(i, (colorItemStack, colorInventory) -> {
                                    fTarget.getColors().clear();

                                    if (!colorName.equalsIgnoreCase("default")) {
                                        fTarget.getColors().putAll(colors);
                                    }

                                    inventoryController.close(fPlayer.getUuid());
                                    sendSettingInventory(fPlayer, fTarget);
                                });
                    }

                    inventoryController.open(fPlayer, inventoryColorsBuilder.build(modernVersion));
                });
    }

    private Inventory.Builder handleStyle(FPlayer fPlayer, FPlayer fTarget, Inventory.Builder inventoryBuilder) {
        Command.Chatsetting.Menu menu = command.getMenu();
        Command.Chatsetting.Menu.Style style = menu.getStyle();

        int slot = style.getSlot();
        if (slot == -1) return inventoryBuilder;

        String material = menu.getMaterial();

        Localization.Command.Chatsetting localization = resolveLocalization(fTarget);

        String[] messages = localization.getMenu().getStyle().getItem()
                .split("<br>");

        String title = messages.length > 0 ? messages[0] : "";
        String[] lore = messages.length > 1 ? Arrays.copyOfRange(messages, 1, messages.length) : new String[]{};

        return inventoryBuilder
                .addItem(slot, platformServerAdapter.buildItemStack(fTarget, material, title, lore))
                .addClickHandler(slot, (itemStack, inventory) -> {
                    if (!permissionChecker.check(fPlayer, permission.getSettings().get(FPlayer.Setting.STYLE))) {
                        builder(fPlayer)
                                .format(Localization.Command.Chatsetting::getNoPermission)
                                .sendBuilt();
                        return;
                    }

                    String header = localization.getMenu().getStyle().getInventory();
                    Component componentHeader = messagePipeline.builder(fTarget, header).build();

                    Inventory.Builder inventoryStylesBuilder = new Inventory.Builder()
                            .name(componentHeader)
                            .addCloseConsumer(colorsInventory -> updateSettings(fTarget))
                            .size(54);

                    for (int i = 0; i < style.getTypes().size(); i++) {
                        Command.Chatsetting.Menu.Style.Type styleType = style.getTypes().get(i);
                        String styleName = styleType.getName();
                        String styleMaterial = styleType.getMaterial();

                        String[] styleMessages = localization.getMenu().getStyle().getTypes().getOrDefault(styleName, "")
                                .replace("<style>", styleType.getValue())
                                .split("<br>");

                        String styleTitle = styleMessages.length > 0 ? styleMessages[0] : "";
                        String[] styleLore = styleMessages.length > 1 ? Arrays.copyOfRange(styleMessages, 1, styleMessages.length) : new String[]{};

                        inventoryStylesBuilder = inventoryStylesBuilder
                                .addItem(i, platformServerAdapter.buildItemStack(fTarget, styleMaterial, styleTitle, styleLore))
                                .addClickHandler(i, (styleItemStack, styleInventory) -> {
                                    if (styleName.equalsIgnoreCase("default")) {
                                        fPlayerService.deleteSetting(fTarget, FPlayer.Setting.STYLE);
                                    } else {
                                        fPlayerService.saveOrUpdateSetting(fTarget, FPlayer.Setting.STYLE, styleType.getValue());
                                    }

                                    inventoryController.close(fPlayer.getUuid());
                                    sendSettingInventory(fPlayer, fTarget);
                                });
                    }

                    inventoryController.open(fPlayer, inventoryStylesBuilder.build(modernVersion));
                });
    }

    private Inventory.Builder handleCheckboxItem(FPlayer fPlayer, FPlayer fTarget, FPlayer.Setting setting, Inventory.Builder inventoryBuilder) {
        Command.Chatsetting.Checkbox checkbox = command.getCheckbox();
        if (!checkbox.getTypes().containsKey(setting)) return inventoryBuilder;

        int slot = checkbox.getTypes().get(setting);
        if (slot == -1) return inventoryBuilder;

        boolean enabled = fTarget.isSetting(setting);

        String material = enabled ? checkbox.getEnabledMaterial() : checkbox.getDisabledMaterial();

        Localization.Command.Chatsetting localization = resolveLocalization(fTarget);
        String title = localization.getCheckbox().getTypes().getOrDefault(setting, "");
        String lore = enabled ? localization.getCheckbox().getFormatEnable() : localization.getCheckbox().getFormatDisable();

        return inventoryBuilder
                .addItem(slot, platformServerAdapter.buildItemStack(fTarget, material, title, lore))
                .addClickHandler(slot, (itemStack, inventory) -> {
                    if (!permissionChecker.check(fPlayer, permission.getSettings().get(setting))) {
                        builder(fPlayer)
                                .format(Localization.Command.Chatsetting::getNoPermission)
                                .sendBuilt();
                        return;
                    }

                    boolean currentEnabled = fTarget.isSetting(setting);

                    if (currentEnabled) {
                        // disable
                        fTarget.removeSetting(setting);
                    } else {
                        // enable
                        fTarget.setSetting(setting, "");
                    }

                    String invertMaterial = currentEnabled ? checkbox.getDisabledMaterial() : checkbox.getEnabledMaterial();
                    String invertLore = currentEnabled ? localization.getCheckbox().getFormatDisable() : localization.getCheckbox().getFormatEnable();

                    ItemStack newItemStack = platformServerAdapter.buildItemStack(fTarget, invertMaterial, title, invertLore);
                    inventoryController.changeItem(fPlayer, inventory, slot, newItemStack);
                });
    }
}
