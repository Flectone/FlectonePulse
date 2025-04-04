package net.flectone.pulse.module.command.chatsetting;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformServerAdapter;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.controller.InventoryController;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.inventory.Inventory;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.formatter.MessageFormatter;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.util.List;

@Singleton
public class ChatsettingModule extends AbstractModuleCommand<Localization.Command.Chatsetting> {

    private final Message.Chat chatMessage;
    private final Permission.Message.Chat chatPermission;
    private final Command.Chatsetting command;
    private final Permission.Command.Chatsetting permission;

    private final FPlayerService fPlayerService;
    private final MessageFormatter messageFormatter;
    private final CommandRegistry commandRegistry;
    private final PermissionChecker permissionChecker;
    private final InventoryController inventoryController;
    private final PlatformServerAdapter platformServerAdapter;

    @Inject
    public ChatsettingModule(FileManager fileManager,
                             FPlayerService fPlayerService,
                             MessageFormatter messageFormatter,
                             CommandRegistry commandRegistry,
                             PermissionChecker permissionChecker,
                             InventoryController inventoryController,
                             PlatformServerAdapter platformServerAdapter) {
        super(localization -> localization.getCommand().getChatsetting(), null);

        this.fPlayerService = fPlayerService;
        this.messageFormatter = messageFormatter;
        this.commandRegistry = commandRegistry;
        this.permissionChecker = permissionChecker;
        this.inventoryController = inventoryController;
        this.platformServerAdapter = platformServerAdapter;

        chatMessage = fileManager.getMessage().getChat();
        chatPermission = fileManager.getPermission().getMessage().getChat();
        command = fileManager.getCommand().getChatsetting();
        permission = fileManager.getPermission().getCommand().getChatsetting();

        addPredicate(this::checkCooldown);
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        permission.getItems().values().forEach(this::registerPermission);

        String commandName = getName(command);
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .handler(this)
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        Localization.Command.Chatsetting localization = resolveLocalization(fPlayer);
        Component header = messageFormatter.builder(fPlayer, localization.getHeader()).build();

        Inventory.Builder inventoryBuilder = new Inventory.Builder()
                .name(header)
                .size(54)
                .addCloseConsumer(inventory -> fPlayerService.saveSettings(fPlayer));

        for (var entry : command.getItems().entrySet()) {
            FPlayer.Setting setting = entry.getKey();

            int settingIndex = setting == FPlayer.Setting.CHAT
                    || setting == FPlayer.Setting.COLOR
                    || fPlayer.isSetting(setting) ? 0 : 1;
            int slot = entry.getValue().getSlot();

            List<List<String>> messages = localization.getItems().get(setting);
            List<String> itemMessages = messages.get(settingIndex);

            inventoryBuilder = inventoryBuilder
                    .addItem(slot, platformServerAdapter.buildItemStack(settingIndex, fPlayer, itemMessages, entry.getValue()))
                    .addClickHandler(slot, (itemStack, inventory) -> {
                        if (!permissionChecker.check(fPlayer, permission.getItems().get(setting).getName())) {
                            builder(fPlayer)
                                    .format(Localization.Command.Chatsetting::getNoPermission)
                                    .sendBuilt();
                            return;
                        }

                        int newSettingIndex = entry.getValue().getMaterials().get(0).equalsIgnoreCase(itemStack.getType().getName().getKey()) ? 1 : 0;

                        switch (setting) {
                            case COLOR -> newSettingIndex = 0;
                            case CHAT -> {
                                newSettingIndex = 0;

                                boolean needNextChat = fPlayer.getSettingValue(FPlayer.Setting.CHAT) == null;
                                boolean chatChanged = false;
                                for (String chatName : chatMessage.getTypes().keySet()) {
                                    String permission = chatPermission.getTypes().get(chatName).getName();
                                    if (!permissionChecker.check(fPlayer, permission)) continue;

                                    if (needNextChat) {
                                        fPlayer.setSetting(FPlayer.Setting.CHAT, chatName);
                                        chatChanged = true;
                                        break;
                                    }

                                    if (chatName.equalsIgnoreCase(fPlayer.getSettingValue(FPlayer.Setting.CHAT))) {
                                        needNextChat = true;
                                    }
                                }

                                if (!chatChanged && needNextChat) {
                                    fPlayer.setSetting(FPlayer.Setting.CHAT, null);
                                }

                            }
                            default -> {
                                if (newSettingIndex == 0) {
                                    fPlayer.setSetting(setting, "");
                                } else {
                                    fPlayer.removeSetting(setting);
                                }
                            }
                        }

                        ItemStack newItemStack = platformServerAdapter.buildItemStack(newSettingIndex, fPlayer, itemMessages, entry.getValue());
                        inventoryController.changeItem(fPlayer, inventory, slot, newItemStack);
                    });
        }

        inventoryController.open(fPlayer, inventoryBuilder.build());

        playSound(fPlayer);
    }
}
