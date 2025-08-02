package net.flectone.pulse.model.util;

public record Toast(String icon, Type style) {
    public enum Type {
        GOAL,
        TASK,
        CHALLENGE
    }
}
