package net.flectone.pulse.module.integration.icu;

import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleSimple;

/**
 * Applies ICU message formatting, so plural and gender forms come out right in every language.
 * @author TheFaser
 */
public interface ICUModule extends ModuleSimple {

    @Override
    ModuleName name();

    @Override
    Integration.Icu config();

    @Override
    Permission.Integration.Icu permission();

    /**
     * Applies ICU formatting so plural and gender forms come out right for the reader.
     *
     * @param sender who wrote it
     * @param receiver who reads it
     * @param text the text
     * @return the formatted text
     */
    String process(FEntity sender, FPlayer receiver, String text);

}
