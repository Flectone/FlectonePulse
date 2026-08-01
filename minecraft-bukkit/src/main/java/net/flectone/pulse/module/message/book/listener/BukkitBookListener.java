package net.flectone.pulse.module.message.book.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.book.BukkitBookModule;
import net.flectone.pulse.service.FPlayerService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitBookListener implements Listener {

    private final FPlayerService fPlayerService;
    private final BukkitBookModule bookModule;

    @EventHandler
    public void onPlayerEditBookEvent(PlayerEditBookEvent event) {
        if (event.isCancelled()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer().getUniqueId());

        BookMeta bookMeta = event.getNewBookMeta();

        for (int i = 1; i <= event.getNewBookMeta().getPages().size(); i++) {
            String page = bookMeta.getPage(i);

            int pageIndex = i;
            bookModule.legacyFormat(fPlayer, page).ifPresent(string -> bookMeta.setPage(pageIndex, string));
        }

        if (event.isSigning()) {
            bookModule.legacyFormat(fPlayer, bookMeta.getTitle()).ifPresent(bookMeta::setTitle);
        }

        event.setNewBookMeta(bookMeta);
    }
}
