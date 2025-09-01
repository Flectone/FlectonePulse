package net.flectone.pulse.module.command.poll.model;

import java.util.List;

public record NBTPoll(String input, boolean multiple, float endTime, float repeatTime, List<String> answers) {
}
