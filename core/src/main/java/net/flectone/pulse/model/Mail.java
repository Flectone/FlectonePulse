package net.flectone.pulse.model;


public record Mail(int id, long date, int sender, int receiver, String message) {

}
