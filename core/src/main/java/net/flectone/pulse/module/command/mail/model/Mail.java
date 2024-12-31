package net.flectone.pulse.module.command.mail.model;


public record Mail(int id, long date, int sender, int receiver, String message) {

}
