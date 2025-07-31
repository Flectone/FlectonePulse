package net.flectone.pulse.module.command.spy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.registry.BukkitListenerRegistry;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
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

import java.util.List;
import java.util.Map;

@Singleton
public class BukkitSpyModule extends SpyModule {

    private final FPlayerService fPlayerService;
    private final BukkitListenerRegistry bukkitListenerManager;

    @Inject
    public BukkitSpyModule(FileResolver fileResolver,
                           FPlayerService fPlayerService,
                           CommandRegistry commandRegistry,
                           PermissionChecker permissionChecker,
                           BukkitListenerRegistry bukkitListenerManager) {
        super(fileResolver, commandRegistry, fPlayerService, permissionChecker);

        this.fPlayerService = fPlayerService;
        this.bukkitListenerManager = bukkitListenerManager;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        bukkitListenerManager.register(SpyListener.class, Event.Priority.NORMAL);
    }

    @Async
    public void check(PlayerCommandPreprocessEvent event) {
        if (!isEnable()) return;

        Map<String, List<String>> categories = getCommand().getCategories();
        if (categories.get("command") == null) return;

        String action = event.getMessage();
        if (!action.isEmpty()) {
            action = action.split(" ")[0].substring(1);
        }

        if (!categories.get("command").contains(action)) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer());
        spy(fPlayer, action, event.getMessage());
    }

    @Async
    public void check(InventoryClickEvent event) {
        if (!isEnable()) return;

        Map<String, List<String>> categories = getCommand().getCategories();
        if (categories.get("action") == null) return;
        if (!categories.get("action").contains("anvil")) return;
        if (event.isCancelled()) return;
        if (!(event.getClickedInventory() instanceof AnvilInventory)) return;
        if (event.getSlot() != 2) return;
        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getItemMeta() == null) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(player);

        ItemStack itemStack = event.getCurrentItem();
        ItemMeta itemMeta = itemStack.getItemMeta();

        String itemName = itemMeta == null
                ? itemStack.getType().name()
                : itemMeta.getDisplayName();

        spy(fPlayer, "anvil", itemName);
    }

    @Async
    public void check(PlayerEditBookEvent event) {
        if (!isEnable()) return;

        Map<String, List<String>> categories = getCommand().getCategories();
        if (categories.get("action") == null) return;
        if (!categories.get("action").contains("book")) return;

        Player player = event.getPlayer();
        FPlayer fPlayer = fPlayerService.getFPlayer(player);

        BookMeta bookMeta = event.getNewBookMeta();
        spy(fPlayer, "book", String.join(" ", bookMeta.getPages()));

        if (bookMeta.getTitle() == null) return;
        spy(fPlayer, "book", bookMeta.getTitle());
    }

    @Async
    public void check(SignChangeEvent event) {
        if (!isEnable()) return;

        Map<String, List<String>> categories = getCommand().getCategories();
        if (categories.get("action") == null) return;
        if (!categories.get("action").contains("sign")) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer());

        String message = String.join(" ", event.getLines());
        spy(fPlayer, "sign", message);
    }

    @Async
    public void check(AsyncPlayerChatEvent event) {
        if (!isEnable()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer());

        String message = event.getMessage();

        checkChat(fPlayer, "chat", message);
    }
}
