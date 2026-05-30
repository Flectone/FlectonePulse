package net.flectone.pulse.module.message.sign.listener;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.sign.SignModule;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Optional;

public class PaperSignListener implements Listener {

    private final FPlayerService fPlayerService;
    private final SignModule signModule;

    public PaperSignListener(FPlayerService fPlayerService, SignModule signModule) {
        this.fPlayerService = fPlayerService;
        this.signModule = signModule;
    }

    @EventHandler
    public void signChangeEvent(SignChangeEvent event) {
        if (event.isCancelled()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer().getUniqueId());

        try {
            // try use paper methods
            for (int i = 0; i < event.lines().size(); i++) {
                Component componentLine = event.line(i);
                if (componentLine == null) continue;

                String line;
                try {
                    line = PlainTextComponentSerializer.plainText().serialize(componentLine);
                } catch (Exception _) {
                    line = event.getLine(i);
                }

                // skip empty string
                if (StringUtils.isEmpty(line)) continue;

                Optional<String> formatted = signModule.paperFormat(fPlayer, line);
                if (formatted.isEmpty()) continue;

                event.line(i, GsonComponentSerializer.gson().deserialize(formatted.get()));
            }
        } catch (Exception _) {
            // use deprecated methods
            for (int i = 0; i < event.getLines().length; i++) {
                String string = event.getLine(i);

                Optional<String> formattedString = signModule.legacyFormat(fPlayer, string);
                if (formattedString.isPresent()) {
                    event.setLine(i, formattedString.get());
                }
            }
        }

    }

}
