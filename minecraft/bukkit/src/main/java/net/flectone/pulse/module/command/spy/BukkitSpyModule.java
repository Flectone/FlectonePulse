package net.flectone.pulse.module.command.spy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.module.command.spy.listener.BukkitSpyListener;
import net.flectone.pulse.platform.registry.BukkitListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.file.FileFacade;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;

@Singleton
public class BukkitSpyModule extends SpyModule {

    private final FPlayerService fPlayerService;
    private final BukkitListenerRegistry bukkitListenerManager;
    private final TaskScheduler taskScheduler;

    @Inject
    public BukkitSpyModule(FileFacade fileFacade,
                           FPlayerService fPlayerService,
                           PermissionChecker permissionChecker,
                           BukkitListenerRegistry bukkitListenerManager,
                           TaskScheduler taskScheduler) {
        super(fileFacade, fPlayerService, permissionChecker);

        this.fPlayerService = fPlayerService;
        this.bukkitListenerManager = bukkitListenerManager;
        this.taskScheduler = taskScheduler;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        bukkitListenerManager.register(BukkitSpyListener.class, Event.Priority.NORMAL);
    }

    public void check(PlayerCommandPreprocessEvent event) {
        if (!isEnable()) return;

        taskScheduler.runAsync(() -> {
            String rawInput = event.getMessage();
            String[] arguments = rawInput.split(" ");

            String commandName = arguments.length != 0 ? arguments[0].substring(1) : "";
            if (!needToSpy("command", commandName)) return;

            FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer().getUniqueId());
            FPlayer fReceiver = arguments.length > 1 ? fPlayerService.getFPlayer(arguments[1]) : FPlayer.UNKNOWN;

            spy(fPlayer, commandName, event.getMessage(), fReceiver.isUnknown() ? Collections.emptyList() : List.of(fReceiver));
        });
    }

    public void check(InventoryClickEvent event) {
        if (!isEnable()) return;

        taskScheduler.runAsync(() -> {
            if (!needToSpy("action", "anvil")) return;
            if (event.isCancelled()) return;
            if (!(event.getClickedInventory() instanceof AnvilInventory)) return;
            if (event.getSlot() != 2) return;
            if (event.getCurrentItem() == null) return;
            if (event.getCurrentItem().getItemMeta() == null) return;
            if (!(event.getWhoClicked() instanceof Player player)) return;

            FPlayer fPlayer = fPlayerService.getFPlayer(player.getUniqueId());

            ItemStack itemStack = event.getCurrentItem();
            ItemMeta itemMeta = itemStack.getItemMeta();

            String itemName = itemMeta == null
                    ? itemStack.getType().name()
                    : itemMeta.getDisplayName();

            spy(fPlayer, "anvil", itemName);
        });
    }

    public void check(PlayerEditBookEvent event) {
        if (!isEnable()) return;

        taskScheduler.runAsync(() -> {
            if (!needToSpy("action", "book")) return;

            FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer().getUniqueId());

            BookMeta bookMeta = event.getNewBookMeta();
            spy(fPlayer, "book", String.join(" ", bookMeta.getPages()));

            if (bookMeta.getTitle() != null) {
                spy(fPlayer, "book", bookMeta.getTitle());
            }
        });
    }

    public void check(SignChangeEvent event) {
        if (!isEnable()) return;

        taskScheduler.runAsync(() -> {
            if (!needToSpy("action", "sign")) return;

            FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer().getUniqueId());

            String message = String.join(" ", event.getLines());
            spy(fPlayer, "sign", message);
        });
    }

    public void check(AsyncPlayerChatEvent event) {
        if (!isEnable()) return;

        taskScheduler.runAsync(() -> {
            FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer().getUniqueId());

            String message = event.getMessage();

            check(fPlayer, "chat", message, event.getRecipients().stream()
                    .map(player -> fPlayerService.getFPlayer(player.getUniqueId()))
                    .toList()
            );
        });
    }
}
