package net.flectone.pulse.module.message.ride.model;

import net.flectone.pulse.model.entity.FEntity;

public record Ride(FEntity target, FEntity secondTarget) {
}