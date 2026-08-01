package net.flectone.pulse.module.message.book.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.book.BookModule;
import net.flectone.pulse.processing.PaperComponentSerializer;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PaperBookListener implements Listener {

    private final FPlayerService fPlayerService;
    private final BookModule bookModule;
    private final PaperComponentSerializer paperComponentSerializer;

    @EventHandler
    public void onPlayerEditBookEvent(PlayerEditBookEvent event) {
        if (event.isCancelled()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer().getUniqueId());

        BookMeta bookMeta = event.getNewBookMeta();

        // pages
        try {
            // try paper format
            for (int i = 1; i <= bookMeta.pages().size(); i++) {
                Component componentPage = bookMeta.page(i);

                String page = paperComponentSerializer.toPlain(componentPage).orElse(bookMeta.getPage(i));

                // skip empty string
                if (StringUtils.isEmpty(page)) continue;

                int pageIndex = i;
                bookModule.paperFormat(fPlayer, page)
                        .flatMap(paperComponentSerializer::fromJson)
                        .ifPresent(component -> bookMeta.page(pageIndex, component));
            }

        } catch (Exception _) {
            // use legacy format
            for (int i = 1; i <= bookMeta.getPages().size(); i++) {
                String page = bookMeta.getPage(i);

                int pageIndex = i;
                bookModule.legacyFormat(fPlayer, page).ifPresent(string -> bookMeta.setPage(pageIndex, string));
            }
        }

        // title
        if (event.isSigning()) {
            sign(fPlayer, bookMeta);
        }

        event.setNewBookMeta(bookMeta);
    }

    private void sign(FPlayer fPlayer, BookMeta bookMeta) {
        try {
            // try paper format
            Component componentTitle = bookMeta.title();
            if (componentTitle == null) return;

            String title = paperComponentSerializer.toPlain(componentTitle).orElse(bookMeta.getTitle());

            // skip empty string
            if (StringUtils.isEmpty(title)) return;

            bookModule.paperFormat(fPlayer, title)
                    .flatMap(paperComponentSerializer::fromJson)
                    .ifPresent(bookMeta::title);
        } catch (Exception _) {
            // use legacy format
            bookModule.legacyFormat(fPlayer, bookMeta.getTitle()).ifPresent(bookMeta::setTitle);
        }
    }
}
