package net.flectone.pulse.processing.parser.moderation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.service.ModerationService;

@Singleton
public class WarnModerationParserImpl extends ModerationParserImpl implements WarnModerationParser {

    @Inject
    public WarnModerationParserImpl(ModerationService moderationService) {
        super(Moderation.Type.WARN, moderationService);
    }

}
