package net.flectone.pulse.module.message.bubble;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.util.constant.ModuleName;
import org.jspecify.annotations.NonNull;

import java.util.Set;

/**
 * Shows chat messages as floating text above the speaker's head.
 * @author TheFaser
 */
public interface BubbleModule extends ModuleSimple {

    @Override
    ModuleName name();

    @Override
    Message.Bubble config();

    @Override
    Permission.Message.Bubble permission();

    /**
     * Shows a bubble above a player's head.
     *
     * @param fPlayer the speaker
     * @param inputString the text
     * @param receivers who should see it
     */
    void add(@NonNull FPlayer fPlayer, @NonNull String inputString, Set<FPlayer> receivers);

    /**
     * Shows a bubble, keeping the raw text separate from the formatted one so the length limit is
     * measured on what the player actually typed.
     *
     * @param fPlayer the speaker
     * @param rawString the text as typed
     * @param inputString the formatted text
     * @param receivers who should see it
     */
    void add(@NonNull FPlayer fPlayer, @NonNull String rawString, @NonNull String inputString, Set<FPlayer> receivers);

    /**
     * How a bubble turns to face the viewer.
     */
    enum Billboard {

        FIXED,
        VERTICAL,
        HORIZONTAL,
        CENTER

    }

}
