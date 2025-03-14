package net.flectone.pulse.model;

public record Toast(String icon, Type style) {
    public enum Type {
        GOAL,
        TASK,
        CHALLENGE
    }
}
