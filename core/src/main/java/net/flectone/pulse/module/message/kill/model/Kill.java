package net.flectone.pulse.module.message.kill.model;

import net.flectone.pulse.model.entity.FEntity;

import java.util.UUID;

public record Kill(String value, FEntity fEntity) {

    public UUID getEntityUUID() {
        return fEntity == null ? null : fEntity.getUuid();
    }

}
