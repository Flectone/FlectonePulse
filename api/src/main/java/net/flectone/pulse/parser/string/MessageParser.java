package net.flectone.pulse.parser.string;

import net.flectone.pulse.model.entity.FPlayer;
import org.incendo.cloud.parser.ArgumentParser;

/**
 * Command argument that parses the rest of the command line as one message.
 * @author TheFaser
 */
public interface MessageParser extends ArgumentParser<FPlayer, String> {
}
