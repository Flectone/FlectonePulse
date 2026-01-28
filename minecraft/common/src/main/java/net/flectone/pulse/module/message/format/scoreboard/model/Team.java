package net.flectone.pulse.module.message.format.scoreboard.model;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;

public record Team(String name, String owner, WrapperPlayServerTeams.ScoreBoardTeamInfo info) {
}
