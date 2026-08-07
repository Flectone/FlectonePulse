package net.flectone.pulse.constant;

/**
 * How far a connecting player has got through the login handshake. Used on proxies, where a
 * player may disconnect between stages.
 * @author TheFaser
 */
public enum LoginStatus {

    PRE_LOGIN,
    LOGIN,
    POST_LOGIN,
    CONNECTED

}