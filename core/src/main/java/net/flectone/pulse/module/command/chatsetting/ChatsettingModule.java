package net.flectone.pulse.module.command.chatsetting;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.google.inject.Inject;
import lombok.Getter;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.controller.InventoryController;
import net.flectone.pulse.database.dao.SettingDAO;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.inventory.Inventory;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.PermissionUtil;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;

import java.util.List;

public abstract class ChatsettingModule extends AbstractModuleCommand<Localization.Command.Chatsetting> {

    private final Message.Chat chatMessage;
    private final Permission.Message.Chat chatPermission;
    @Getter private final Command.Chatsetting command;
    @Getter private final Permission.Command.Chatsetting permission;

    private final SettingDAO settingDAO;
    private final ComponentUtil componentUtil;
    private final CommandUtil commandUtil;
    private final PermissionUtil permissionUtil;
    private final InventoryController inventoryController;

    public ChatsettingModule(FileManager fileManager,
                             SettingDAO settingDAO,
                             ComponentUtil componentUtil,
                             CommandUtil commandUtil,
                             PermissionUtil permissionUtil,
                             InventoryController inventoryController) {
        super(localization -> localization.getCommand().getChatsetting(), null);

        this.settingDAO = settingDAO;
        this.componentUtil = componentUtil;
        this.commandUtil = commandUtil;
        this.permissionUtil = permissionUtil;
        this.inventoryController = inventoryController;

        chatMessage = fileManager.getMessage().getChat();
        chatPermission = fileManager.getPermission().getMessage().getChat();
        command = fileManager.getCommand().getChatsetting();
        permission = fileManager.getPermission().getCommand().getChatsetting();

        addPredicate(this::checkCooldown);
    }

    @Inject
    private FLogger fLogger;

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;

        Localization.Command.Chatsetting localization = resolveLocalization(fPlayer);
        Component header = componentUtil.builder(fPlayer, localization.getHeader()).build();

        Inventory.Builder inventoryBuilder = new Inventory.Builder()
                .name(header)
                .size(54)
                .addCloseConsumer(inventory -> settingDAO.save(fPlayer));

        for (var entry : command.getItems().entrySet()) {
            FPlayer.Setting setting = entry.getKey();

            int settingIndex = setting == FPlayer.Setting.CHAT
                    || setting == FPlayer.Setting.COLOR
                    || fPlayer.isSetting(setting) ? 0 : 1;
            int slot = entry.getValue().getSlot();

            List<List<String>> messages = localization.getItems().get(setting);
            List<String> itemMessages = messages.get(settingIndex);

            inventoryBuilder = inventoryBuilder
                    .addItem(slot, buildItemStack(settingIndex, fPlayer, itemMessages, entry.getValue()))
                    .addClickHandler(slot, (itemStack, inventory) -> {
                        if (!permissionUtil.has(fPlayer, permission.getItems().get(setting).getName())) {
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
                                    if (!permissionUtil.has(fPlayer, permission)) continue;

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

                        ItemStack newItemStack = buildItemStack(newSettingIndex, fPlayer, itemMessages, entry.getValue());
                        inventoryController.changeItem(fPlayer, inventory, slot, newItemStack);
                    });
        }

        inventoryController.open(fPlayer, inventoryBuilder.build());

        playSound(fPlayer);
    }

    public abstract ItemStack buildItemStack(int settingIndex, FPlayer fPlayer, List<String> itemMessages, Command.Chatsetting.SettingItem settingItem);

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        permission.getItems().values().forEach(this::registerPermission);

        getCommand().getAliases().forEach(commandUtil::unregister);

        createCommand();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }
}
