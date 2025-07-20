package net.flectone.pulse.module.command.chatsetting;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformServerAdapter;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.controller.InventoryController;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.inventory.Inventory;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.provider.PacketProvider;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.util.Arrays;
import java.util.Map;

@Singleton
public class ChatsettingModule extends AbstractModuleCommand<Localization.Command.Chatsetting> {

    private final Permission.Message.Chat chatPermission;
    private final Command.Chatsetting command;
    private final Permission.Command.Chatsetting permission;
    private final FPlayerService fPlayerService;
    private final MessagePipeline messagePipeline;
    private final CommandRegistry commandRegistry;
    private final PermissionChecker permissionChecker;
    private final InventoryController inventoryController;
    private final PlatformServerAdapter platformServerAdapter;
    private final boolean modernVersion;

    @Inject
    public ChatsettingModule(FileResolver fileResolver,
                             FPlayerService fPlayerService,
                             MessagePipeline messagePipeline,
                             CommandRegistry commandRegistry,
                             PermissionChecker permissionChecker,
                             InventoryController inventoryController,
                             PlatformServerAdapter platformServerAdapter,
                             PacketProvider packetProvider) {
        super(localization -> localization.getCommand().getChatsetting(), null);

        this.chatPermission = fileResolver.getPermission().getMessage().getChat();
        this.command = fileResolver.getCommand().getChatsetting();
        this.permission = fileResolver.getPermission().getCommand().getChatsetting();
        this.fPlayerService = fPlayerService;
        this.messagePipeline = messagePipeline;
        this.commandRegistry = commandRegistry;
        this.permissionChecker = permissionChecker;
        this.inventoryController = inventoryController;
        this.platformServerAdapter = platformServerAdapter;
        this.modernVersion = packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_14);
    }

    @Override
    protected boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        permission.getSettings().values().forEach(this::registerPermission);

        String commandName = getName(command);
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .handler(this)
        );

        addPredicate(this::checkCooldown);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        sendSettingInventory(fPlayer);

        playSound(fPlayer);
    }

    private void sendSettingInventory(FPlayer fPlayer) {
        Localization.Command.Chatsetting localization = resolveLocalization(fPlayer);
        Component header = messagePipeline.builder(fPlayer, localization.getInventory()).build();

        Inventory.Builder inventoryBuilder = new Inventory.Builder()
                .name(header)
                .size(54)
                .addCloseConsumer(inventory -> fPlayerService.saveSettings(fPlayer));

        for (FPlayer.Setting setting : FPlayer.Setting.values()) {
            inventoryBuilder = switch (setting) {
                case CHAT -> handleChat(fPlayer, inventoryBuilder);
                case COLOR -> handleColor(fPlayer, inventoryBuilder);
                case STYLE -> handleStyle(fPlayer, inventoryBuilder);
                default -> handleCheckboxItem(fPlayer, setting, inventoryBuilder);
            };
        }

        inventoryController.open(fPlayer, inventoryBuilder.build(modernVersion));
    }

    private Inventory.Builder handleChat(FPlayer fPlayer, Inventory.Builder inventoryBuilder) {
        Command.Chatsetting.Menu menu = command.getMenu();
        Command.Chatsetting.Menu.Chat chat = menu.getChat();

        int slot = chat.getSlot();
        if (slot == -1) return inventoryBuilder;

        String currentChat = fPlayer.getSettingValue(FPlayer.Setting.CHAT);
        if (currentChat == null) {
            currentChat = "default";
        }

        String material = menu.getMaterial();

        Localization.Command.Chatsetting localization = resolveLocalization(fPlayer);

        String[] messages = localization.getMenu().getChat().getItem()
                .replace("<chat>", currentChat)
                .split("<br>");

        String title = messages.length > 0 ? messages[0] : "";
        String[] lore = messages.length > 1 ? Arrays.copyOfRange(messages, 1, messages.length) : new String[]{};

        return inventoryBuilder
                .addItem(slot, platformServerAdapter.buildItemStack(fPlayer, material, title, lore))
                .addClickHandler(slot, (itemStack, inventory) -> {
                    if (!permissionChecker.check(fPlayer, permission.getSettings().get(FPlayer.Setting.CHAT))) {
                        builder(fPlayer)
                                .format(Localization.Command.Chatsetting::getNoPermission)
                                .sendBuilt();
                        return;
                    }

                    String header = localization.getMenu().getChat().getInventory();
                    Component componentHeader = messagePipeline.builder(fPlayer, header).build();

                    Inventory.Builder inventoryChatsBuilder = new Inventory.Builder()
                            .name(componentHeader)
                            .size(54)
                            .addCloseConsumer(chatsInventory -> fPlayerService.saveSettings(fPlayer));

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
                                .addItem(i, platformServerAdapter.buildItemStack(fPlayer, chatMaterial, chatTitle, chatLore))
                                .addClickHandler(i, (chatItemStack, chatInventory) -> {
                                    Permission.IPermission permission = chatPermission.getTypes().get(chatName);
                                    if (!permissionChecker.check(fPlayer, permission)) {
                                        builder(fPlayer)
                                                .format(Localization.Command.Chatsetting::getNoPermission)
                                                .sendBuilt();
                                        return;
                                    }

                                    if (chatName.equalsIgnoreCase("default")) {
                                        fPlayer.setSetting(FPlayer.Setting.CHAT, null);
                                    } else {
                                        fPlayer.setSetting(FPlayer.Setting.CHAT, chatName);
                                    }

                                    inventoryController.close(fPlayer.getUuid());
                                    sendSettingInventory(fPlayer);
                                });
                    }

                    inventoryController.open(fPlayer, inventoryChatsBuilder.build(modernVersion));
                });
    }

    private Inventory.Builder handleColor(FPlayer fPlayer, Inventory.Builder inventoryBuilder) {
        Command.Chatsetting.Menu menu = command.getMenu();
        Command.Chatsetting.Menu.Color color = menu.getColor();

        int slot = color.getSlot();
        if (slot == -1) return inventoryBuilder;

        String material = menu.getMaterial();

        Localization.Command.Chatsetting localization = resolveLocalization(fPlayer);

        String[] messages = localization.getMenu().getColor().getItem()
                .split("<br>");

        String title = messages.length > 0 ? messages[0] : "";
        String[] lore = messages.length > 1 ? Arrays.copyOfRange(messages, 1, messages.length) : new String[]{};

        return inventoryBuilder
                .addItem(slot, platformServerAdapter.buildItemStack(fPlayer, material, title, lore))
                .addClickHandler(slot, (itemStack, inventory) -> {
                    if (!permissionChecker.check(fPlayer, permission.getSettings().get(FPlayer.Setting.COLOR))) {
                        builder(fPlayer)
                                .format(Localization.Command.Chatsetting::getNoPermission)
                                .sendBuilt();
                        return;
                    }

                    String header = localization.getMenu().getColor().getInventory();
                    Component componentHeader = messagePipeline.builder(fPlayer, header).build();

                    Inventory.Builder inventoryColorsBuilder = new Inventory.Builder()
                            .name(componentHeader)
                            .size(54)
                            .addCloseConsumer(colorsInventory -> fPlayerService.saveColors(fPlayer));

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
                                .addItem(i, platformServerAdapter.buildItemStack(fPlayer, colorMaterial, colorTitle, colorLore))
                                .addClickHandler(i, (colorItemStack, colorInventory) -> {
                                    fPlayer.getColors().clear();

                                    if (!colorName.equalsIgnoreCase("default")) {
                                        fPlayer.getColors().putAll(colors);
                                    }

                                    inventoryController.close(fPlayer.getUuid());
                                    sendSettingInventory(fPlayer);
                                });
                    }

                    inventoryController.open(fPlayer, inventoryColorsBuilder.build(modernVersion));
                });
    }

    private Inventory.Builder handleStyle(FPlayer fPlayer, Inventory.Builder inventoryBuilder) {
        Command.Chatsetting.Menu menu = command.getMenu();
        Command.Chatsetting.Menu.Style style = menu.getStyle();

        int slot = style.getSlot();
        if (slot == -1) return inventoryBuilder;

        String material = menu.getMaterial();

        Localization.Command.Chatsetting localization = resolveLocalization(fPlayer);

        String[] messages = localization.getMenu().getStyle().getItem()
                .split("<br>");

        String title = messages.length > 0 ? messages[0] : "";
        String[] lore = messages.length > 1 ? Arrays.copyOfRange(messages, 1, messages.length) : new String[]{};

        return inventoryBuilder
                .addItem(slot, platformServerAdapter.buildItemStack(fPlayer, material, title, lore))
                .addClickHandler(slot, (itemStack, inventory) -> {
                    if (!permissionChecker.check(fPlayer, permission.getSettings().get(FPlayer.Setting.STYLE))) {
                        builder(fPlayer)
                                .format(Localization.Command.Chatsetting::getNoPermission)
                                .sendBuilt();
                        return;
                    }

                    String header = localization.getMenu().getStyle().getInventory();
                    Component componentHeader = messagePipeline.builder(fPlayer, header).build();

                    Inventory.Builder inventoryStylesBuilder = new Inventory.Builder()
                            .name(componentHeader)
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
                                .addItem(i, platformServerAdapter.buildItemStack(fPlayer, styleMaterial, styleTitle, styleLore))
                                .addClickHandler(i, (styleItemStack, styleInventory) -> {
                                    if (styleName.equalsIgnoreCase("default")) {
                                        fPlayerService.deleteSetting(fPlayer, FPlayer.Setting.STYLE);
                                    } else {
                                        fPlayerService.saveOrUpdateSetting(fPlayer, FPlayer.Setting.STYLE, styleType.getValue());
                                    }

                                    inventoryController.close(fPlayer.getUuid());
                                    sendSettingInventory(fPlayer);
                                });
                    }

                    inventoryController.open(fPlayer, inventoryStylesBuilder.build(modernVersion));
                });
    }

    private Inventory.Builder handleCheckboxItem(FPlayer fPlayer, FPlayer.Setting setting, Inventory.Builder inventoryBuilder) {
        Command.Chatsetting.Checkbox checkbox = command.getCheckbox();
        if (!checkbox.getTypes().containsKey(setting)) return inventoryBuilder;

        int slot = checkbox.getTypes().get(setting);
        if (slot == -1) return inventoryBuilder;

        boolean enabled = fPlayer.isSetting(setting);

        String material = enabled ? checkbox.getEnabledMaterial() : checkbox.getDisabledMaterial();

        Localization.Command.Chatsetting localization = resolveLocalization(fPlayer);
        String title = localization.getCheckbox().getTypes().getOrDefault(setting, "");
        String lore = enabled ? localization.getCheckbox().getFormatEnable() : localization.getCheckbox().getFormatDisable();

        return inventoryBuilder
                .addItem(slot, platformServerAdapter.buildItemStack(fPlayer, material, title, lore))
                .addClickHandler(slot, (itemStack, inventory) -> {
                    if (!permissionChecker.check(fPlayer, permission.getSettings().get(setting))) {
                        builder(fPlayer)
                                .format(Localization.Command.Chatsetting::getNoPermission)
                                .sendBuilt();
                        return;
                    }

                    boolean currentEnabled = fPlayer.isSetting(setting);

                    if (currentEnabled) {
                        // disable
                        fPlayer.removeSetting(setting);
                    } else {
                        // enable
                        fPlayer.setSetting(setting, "");
                    }

                    String invertMaterial = currentEnabled ? checkbox.getDisabledMaterial() : checkbox.getEnabledMaterial();
                    String invertLore = currentEnabled ? localization.getCheckbox().getFormatDisable() : localization.getCheckbox().getFormatEnable();

                    ItemStack newItemStack = platformServerAdapter.buildItemStack(fPlayer, invertMaterial, title, invertLore);
                    inventoryController.changeItem(fPlayer, inventory, slot, newItemStack);
                });
    }
}
