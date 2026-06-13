package net.flectone.pulse.module.message.sign.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.sign.SignModule;
import net.flectone.pulse.processing.PaperComponentSerializer;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PaperSignListener implements Listener {

    private final FPlayerService fPlayerService;
    private final SignModule signModule;
    private final PaperComponentSerializer paperComponentSerializer;

    @EventHandler
    public void signChangeEvent(SignChangeEvent event) {
        if (event.isCancelled()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer().getUniqueId());

        try {
            // try paper format
            for (int i = 0; i < event.lines().size(); i++) {
                Component componentLine = event.line(i);
                if (componentLine == null) continue;

                String line = paperComponentSerializer.toPlain(componentLine).orElse(event.getLine(i));

                // skip empty string
                if (StringUtils.isEmpty(line)) continue;

                int lineIndex = i;
                signModule.paperFormat(fPlayer, line)
                        .flatMap(paperComponentSerializer::fromJson)
                        .ifPresent(component -> event.line(lineIndex, component));
            }
        } catch (Exception _) {
            // use legacy format
            for (int i = 0; i < event.getLines().length; i++) {
                String line = event.getLine(i);

                int lineIndex = i;
                signModule.legacyFormat(fPlayer, line)
                        .ifPresent(string -> event.setLine(lineIndex, string));
            }
        }

    }

}
