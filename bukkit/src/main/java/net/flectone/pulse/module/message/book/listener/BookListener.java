package net.flectone.pulse.module.message.book.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.book.BukkitBookModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

@Singleton
public class BookListener implements Listener {

    private final FPlayerManager fPlayerManager;
    private final BukkitBookModule bookModule;

    @Inject
    public BookListener(FPlayerManager fPlayerManager,
                        BukkitBookModule bookModule) {
        this.fPlayerManager = fPlayerManager;
        this.bookModule = bookModule;
    }

    @EventHandler
    public void playerEditBookEvent(PlayerEditBookEvent event) {
        if (event.isCancelled()) return;

        FPlayer fPlayer = fPlayerManager.get(event.getPlayer());

        BookMeta bookMeta = event.getNewBookMeta();

        for (int x = 1; x <= event.getNewBookMeta().getPages().size(); x++) {
            String string = bookMeta.getPage(x);

            if (string.isEmpty()) continue;

            string = bookModule.format(fPlayer, string);
            if (string == null) continue;

            bookMeta.setPage(x, string);
        }

        if (event.isSigning() && bookMeta.getTitle() != null) {
            String title = bookModule.format(fPlayer, bookMeta.getTitle());
            if (title != null) {
                bookMeta.setTitle(title);
            }
        }

        event.setNewBookMeta(bookMeta);
    }
}
