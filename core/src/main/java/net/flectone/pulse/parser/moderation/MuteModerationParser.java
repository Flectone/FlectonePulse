package net.flectone.pulse.parser.moderation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.service.ModerationService;

@Singleton
public class MuteModerationParser extends ModerationParser {

    @Inject
    public MuteModerationParser(ModerationService moderationService) {
        super(Moderation.Type.MUTE, moderationService);
    }

}
