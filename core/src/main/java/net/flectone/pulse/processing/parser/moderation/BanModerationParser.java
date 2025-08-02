package net.flectone.pulse.processing.parser.moderation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.service.ModerationService;

@Singleton
public class BanModerationParser extends ModerationParser {

    @Inject
    public BanModerationParser(ModerationService moderationService) {
        super(Moderation.Type.BAN, moderationService);
    }

}
