package net.flectone.pulse.module.message.format.object.texture.model;

import java.util.List;

public record Texture(long lastModified, List<Frame> frames) {
}
