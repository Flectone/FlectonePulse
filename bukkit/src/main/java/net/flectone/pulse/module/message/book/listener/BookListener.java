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

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BookListener implements Listener {

    private final FPlayerService fPlayerService;
    private final BukkitBookModule bookModule;

    @EventHandler
    public void playerEditBookEvent(PlayerEditBookEvent event) {
        if (event.isCancelled()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer());

        BookMeta bookMeta = event.getNewBookMeta();

        for (int x = 1; x <= event.getNewBookMeta().getPages().size(); x++) {
            String string = bookMeta.getPage(x);

            Optional<String> formattedString = bookModule.format(fPlayer, string);
            if (formattedString.isPresent()) {
                bookMeta.setPage(x, formattedString.get());
            }
        }

        if (event.isSigning()) {
            Optional<String> formattedTitle = bookModule.format(fPlayer, bookMeta.getTitle());
            if (formattedTitle.isPresent()) {
                bookMeta.setTitle(formattedTitle.get());
            }
        }

        event.setNewBookMeta(bookMeta);
    }
}
