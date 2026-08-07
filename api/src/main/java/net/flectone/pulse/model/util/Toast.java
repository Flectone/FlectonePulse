package net.flectone.pulse.model.util;

/**
 * An advancement popup shown in the corner of the screen.
 *
 * @param icon the namespaced item id drawn on the popup
 * @param style the frame style, which also decides the sound the client plays
 * @author TheFaser
 */
public record Toast(String icon, Type style) {

    /**
     * The advancement frame a toast is drawn with.
     */
    public enum Type {
        GOAL,
        TASK,
        CHALLENGE
    }

}
