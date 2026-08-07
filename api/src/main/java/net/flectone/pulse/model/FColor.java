package net.flectone.pulse.model;

/**
 * One color a player picked with /chatcolor.
 *
 * @param number the slot the color occupies
 * @param name the color value
 * @author TheFaser
 */
public record FColor(int number, String name) {

    /**
     * Which side of a message the color applies to, what the player sees or what others see from them.
     */
    public enum Type {
        SEE, // always first
        OUT // always second
    }

}
