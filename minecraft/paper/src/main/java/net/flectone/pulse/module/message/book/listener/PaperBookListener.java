package net.flectone.pulse.module.message.book.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.book.BookModule;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PaperBookListener implements Listener {

    private final FPlayerService fPlayerService;
    private final BookModule bookModule;

    @EventHandler
    public void playerEditBookEvent(PlayerEditBookEvent event) {
        if (event.isCancelled()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer().getUniqueId());

        BookMeta bookMeta = event.getNewBookMeta();

        // pages
        try {
            for (int i = 1; i <= bookMeta.pages().size(); i++) {
                Component componentPage = bookMeta.page(i);

                String page;
                try {
                    page = PlainTextComponentSerializer.plainText().serialize(componentPage);
                } catch (Exception _) {
                    page = bookMeta.getPage(i);
                }

                // skip empty string
                if (StringUtils.isEmpty(page)) continue;

                Optional<String> formatted = bookModule.paperFormat(fPlayer, page);
                if (formatted.isEmpty()) continue;

                bookMeta.page(i, GsonComponentSerializer.gson().deserialize(formatted.get()));
            }

        } catch (Exception _) {
            for (int i = 1; i <= bookMeta.getPages().size(); i++) {
                String string = bookMeta.getPage(i);

                Optional<String> formattedString = bookModule.legacyFormat(fPlayer, string);
                if (formattedString.isPresent()) {
                    bookMeta.setPage(i, formattedString.get());
                }
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
            Component componentTitle = bookMeta.title();
            if (componentTitle == null) return;

            String title;
            try {
                title = PlainTextComponentSerializer.plainText().serialize(componentTitle);
            } catch (Exception _) {
                title = bookMeta.getTitle();
            }

            // skip empty string
            if (StringUtils.isEmpty(title)) return;

            Optional<String> formatted = bookModule.paperFormat(fPlayer, title);
            if (formatted.isEmpty()) return;

            bookMeta.title(GsonComponentSerializer.gson().deserialize(formatted.get()));
        } catch (Exception _) {
            Optional<String> formattedTitle = bookModule.legacyFormat(fPlayer, bookMeta.getTitle());
            formattedTitle.ifPresent(bookMeta::setTitle);
        }
    }
}
