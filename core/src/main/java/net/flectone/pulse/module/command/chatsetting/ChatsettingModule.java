package net.flectone.pulse.module.command.chatsetting;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import lombok.Getter;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.controller.InventoryController;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.inventory.Inventory;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.PermissionUtil;
import net.kyori.adventure.text.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class ChatsettingModule extends AbstractModuleCommand<Localization.Command.Chatsetting> {

    private final Message.Chat chat;
    @Getter private final Command.Chatsetting command;
    @Getter private final Permission.Command.Chatsetting permission;

    private final FPlayerDAO fPlayerDAO;
    private final ComponentUtil componentUtil;
    private final CommandUtil commandUtil;
    private final PermissionUtil permissionUtil;
    private final InventoryController inventoryController;

    public ChatsettingModule(FileManager fileManager,
                             FPlayerDAO fPlayerDAO,
                             ComponentUtil componentUtil,
                             CommandUtil commandUtil,
                             PermissionUtil permissionUtil,
                             InventoryController inventoryController) {
        super(localization -> localization.getCommand().getChatsetting(), null);

        this.fPlayerDAO = fPlayerDAO;
        this.componentUtil = componentUtil;
        this.commandUtil = commandUtil;
        this.permissionUtil = permissionUtil;
        this.inventoryController = inventoryController;

        chat = fileManager.getMessage().getChat();
        command = fileManager.getCommand().getChatsetting();
        permission = fileManager.getPermission().getCommand().getChatsetting();

        addPredicate(this::checkCooldown);
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;

        Localization.Command.Chatsetting localization = resolveLocalization(fPlayer);
        Component header = componentUtil.builder(fPlayer, localization.getHeader()).build();

        Inventory.Builder inventoryBuilder = new Inventory.Builder()
                .name(header)
                .size(54)
                .addCloseConsumer(inventory -> fPlayerDAO.updateFPlayer(fPlayer));

        for (var entry : command.getSettings().entrySet()) {
            FPlayer.Setting setting = entry.getKey();

            int settingIndex = setting == FPlayer.Setting.CHAT
                    || setting == FPlayer.Setting.COLOR
                    || fPlayer.getSettings()[setting.ordinal()] ? 0 : 1;
            int slot = entry.getValue().getSlot();

            List<List<String>> messages = localization.getSettings().get(setting);
            List<String> itemMessages = messages.get(settingIndex);

            inventoryBuilder = inventoryBuilder
                    .addItem(slot, buildItemStack(settingIndex, fPlayer, itemMessages, entry.getValue()))
                    .addClickHandler(slot, (itemStack, inventory) -> {
                        if (!permissionUtil.has(fPlayer, permission.getSettings().get(setting).getName())) {
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

                                Set<String> chats = chat.getTypes().keySet();

                                if (fPlayer.getChat() == null) {
                                    fPlayer.setChat(chats.iterator().next());
                                } else {
                                    for (Iterator<String> it = chats.iterator(); it.hasNext(); ) {
                                        String chat = it.next();
                                        if (chat.equals(fPlayer.getChat())) {
                                            chat = it.hasNext() ? it.next() : null;
                                            fPlayer.setChat(chat);
                                            break;
                                        }
                                    }
                                }
                            }
                            default -> fPlayer.getSettings()[setting.ordinal()] = newSettingIndex == 0;
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

        permission.getSettings().values().forEach(this::registerPermission);

        getCommand().getAliases().forEach(commandUtil::unregister);

        createCommand();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }
}
