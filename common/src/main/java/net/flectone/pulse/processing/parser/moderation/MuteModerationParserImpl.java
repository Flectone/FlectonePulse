package net.flectone.pulse.processing.parser.moderation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.service.ModerationService;

@Singleton
public class MuteModerationParserImpl extends ModerationParserImpl implements MuteModerationParser {

    @Inject
    public MuteModerationParserImpl(ModerationService moderationService) {
        super(Moderation.Type.MUTE, moderationService);
    }

}
