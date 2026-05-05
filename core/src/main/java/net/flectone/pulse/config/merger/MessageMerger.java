package net.flectone.pulse.config.merger;

import net.flectone.pulse.config.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for merging {@link Message} configuration objects.
 * <p>
 * This interface defines mapping methods for deep merging message configurations,
 * handling nested structures through builder patterns.
 * </p>
 *
 * @author TheFaser
 * @since 1.7.1
 */
@Mapper(config = MapstructMergerConfig.class)
public interface MessageMerger {

    @Mapping(target = "afk", expression = "java(mergeAfk(target.build().afk().toBuilder(), source.afk()))")
    @Mapping(target = "anvil", expression = "java(mergeAnvil(target.build().anvil().toBuilder(), source.anvil()))")
    @Mapping(target = "auto", expression = "java(mergeAuto(target.build().auto().toBuilder(), source.auto()))")
    @Mapping(target = "book", expression = "java(mergeBook(target.build().book().toBuilder(), source.book()))")
    @Mapping(target = "bossbar", expression = "java(mergeBossbar(target.build().bossbar().toBuilder(), source.bossbar()))")
    @Mapping(target = "brand", expression = "java(mergeBrand(target.build().brand().toBuilder(), source.brand()))")
    @Mapping(target = "bubble", expression = "java(mergeBubble(target.build().bubble().toBuilder(), source.bubble()))")
    @Mapping(target = "chat", expression = "java(mergeChat(target.build().chat().toBuilder(), source.chat()))")
    @Mapping(target = "format", expression = "java(mergeFormat(target.build().format().toBuilder(), source.format()))")
    @Mapping(target = "greeting", expression = "java(mergeGreeting(target.build().greeting().toBuilder(), source.greeting()))")
    @Mapping(target = "join", expression = "java(mergeJoin(target.build().join().toBuilder(), source.join()))")
    @Mapping(target = "quit", expression = "java(mergeQuit(target.build().quit().toBuilder(), source.quit()))")
    @Mapping(target = "rightclick", expression = "java(mergeRightclick(target.build().rightclick().toBuilder(), source.rightclick()))")
    @Mapping(target = "scoreboard", expression = "java(mergeScoreboard(target.build().scoreboard().toBuilder(), source.scoreboard()))")
    @Mapping(target = "serverlink", expression = "java(mergeServerlink(target.build().serverlink().toBuilder(), source.serverlink()))")
    @Mapping(target = "sidebar", expression = "java(mergeSidebar(target.build().sidebar().toBuilder(), source.sidebar()))")
    @Mapping(target = "sign", expression = "java(mergeSign(target.build().sign().toBuilder(), source.sign()))")
    @Mapping(target = "status", expression = "java(mergeStatus(target.build().status().toBuilder(), source.status()))")
    @Mapping(target = "tab", expression = "java(mergeTab(target.build().tab().toBuilder(), source.tab()))")
    @Mapping(target = "update", expression = "java(mergeUpdate(target.build().update().toBuilder(), source.update()))")
    @Mapping(target = "vanilla", expression = "java(mergeVanilla(target.build().vanilla().toBuilder(), source.vanilla()))")
    Message merge(@MappingTarget Message.MessageBuilder target, Message source);

    Message.Afk mergeAfk(@MappingTarget Message.Afk.AfkBuilder target, Message.Afk source);

    Message.Anvil mergeAnvil(@MappingTarget Message.Anvil.AnvilBuilder target, Message.Anvil source);

    Message.Auto mergeAuto(@MappingTarget Message.Auto.AutoBuilder target, Message.Auto source);

    Message.Book mergeBook(@MappingTarget Message.Book.BookBuilder target, Message.Book source);

    Message.Bossbar mergeBossbar(@MappingTarget Message.Bossbar.BossbarBuilder target, Message.Bossbar source);

    Message.Brand mergeBrand(@MappingTarget Message.Brand.BrandBuilder target, Message.Brand source);

    @Mapping(target = "interaction", expression = "java(mergeBubbleInteraction(target.build().interaction().toBuilder(), source.interaction()))")
    @Mapping(target = "modern", expression = "java(mergeBubbleModern(target.build().modern().toBuilder(), source.modern()))")
    Message.Bubble mergeBubble(@MappingTarget Message.Bubble.BubbleBuilder target, Message.Bubble source);

    Message.Bubble.Interaction mergeBubbleInteraction(@MappingTarget Message.Bubble.Interaction.InteractionBuilder target, Message.Bubble.Interaction source);

    Message.Bubble.Modern mergeBubbleModern(@MappingTarget Message.Bubble.Modern.ModernBuilder target, Message.Bubble.Modern source);

    Message.Chat mergeChat(@MappingTarget Message.Chat.ChatBuilder target, Message.Chat source);

    @Mapping(target = "animation", expression = "java(mergeFormatAnimation(target.build().animation().toBuilder(), source.animation()))")
    @Mapping(target = "condition", expression = "java(mergeFormatCondition(target.build().condition().toBuilder(), source.condition()))")
    @Mapping(target = "fcolor", expression = "java(mergeFormatFColor(target.build().fcolor().toBuilder(), source.fcolor()))")
    @Mapping(target = "fixation", expression = "java(mergeFormatFixation(target.build().fixation().toBuilder(), source.fixation()))")
    @Mapping(target = "mention", expression = "java(mergeFormatMention(target.build().mention().toBuilder(), source.mention()))")
    @Mapping(target = "moderation", expression = "java(mergeFormatModeration(target.build().moderation().toBuilder(), source.moderation()))")
    @Mapping(target = "names", expression = "java(mergeFormatNames(target.build().names().toBuilder(), source.names()))")
    @Mapping(target = "object", expression = "java(mergeFormatObject(target.build().object().toBuilder(), source.object()))")
    @Mapping(target = "questionAnswer", expression = "java(mergeFormatQuestionAnswer(target.build().questionAnswer().toBuilder(), source.questionAnswer()))")
    @Mapping(target = "replacement", expression = "java(mergeFormatReplacement(target.build().replacement().toBuilder(), source.replacement()))")
    @Mapping(target = "translate", expression = "java(mergeFormatTranslate(target.build().translate().toBuilder(), source.translate()))")
    @Mapping(target = "world", expression = "java(mergeFormatWorld(target.build().world().toBuilder(), source.world()))")
    Message.Format mergeFormat(@MappingTarget Message.Format.FormatBuilder target, Message.Format source);

    Message.Format.Animation mergeFormatAnimation(@MappingTarget Message.Format.Animation.AnimationBuilder target, Message.Format.Animation source);

    Message.Format.Condition mergeFormatCondition(@MappingTarget Message.Format.Condition.ConditionBuilder target, Message.Format.Condition source);

    Message.Format.FColor mergeFormatFColor(@MappingTarget Message.Format.FColor.FColorBuilder target, Message.Format.FColor source);

    Message.Format.Fixation mergeFormatFixation(@MappingTarget Message.Format.Fixation.FixationBuilder target, Message.Format.Fixation source);

    Message.Format.Mention mergeFormatMention(@MappingTarget Message.Format.Mention.MentionBuilder target, Message.Format.Mention source);

    @Mapping(target = "caps", expression = "java(mergeFormatModerationCaps(target.build().caps().toBuilder(), source.caps()))")
    @Mapping(target = "delete", expression = "java(mergeFormatModerationDelete(target.build().delete().toBuilder(), source.delete()))")
    @Mapping(target = "newbie", expression = "java(mergeFormatModerationNewbie(target.build().newbie().toBuilder(), source.newbie()))")
    @Mapping(target = "flood", expression = "java(mergeFormatModerationFlood(target.build().flood().toBuilder(), source.flood()))")
    @Mapping(target = "swear", expression = "java(mergeFormatModerationSwear(target.build().swear().toBuilder(), source.swear()))")
    Message.Format.Moderation mergeFormatModeration(@MappingTarget Message.Format.Moderation.ModerationBuilder target, Message.Format.Moderation source);

    Message.Format.Moderation.Caps mergeFormatModerationCaps(@MappingTarget Message.Format.Moderation.Caps.CapsBuilder target, Message.Format.Moderation.Caps source);

    Message.Format.Moderation.Delete mergeFormatModerationDelete(@MappingTarget Message.Format.Moderation.Delete.DeleteBuilder target, Message.Format.Moderation.Delete source);

    Message.Format.Moderation.Newbie mergeFormatModerationNewbie(@MappingTarget Message.Format.Moderation.Newbie.NewbieBuilder target, Message.Format.Moderation.Newbie source);

    Message.Format.Moderation.Flood mergeFormatModerationFlood(@MappingTarget Message.Format.Moderation.Flood.FloodBuilder target, Message.Format.Moderation.Flood source);

    Message.Format.Moderation.Swear mergeFormatModerationSwear(@MappingTarget Message.Format.Moderation.Swear.SwearBuilder target, Message.Format.Moderation.Swear source);

    Message.Format.Names mergeFormatNames(@MappingTarget Message.Format.Names.NamesBuilder target, Message.Format.Names source);

    Message.Format.Object mergeFormatObject(@MappingTarget Message.Format.Object.ObjectBuilder target, Message.Format.Object source);

    Message.Format.QuestionAnswer mergeFormatQuestionAnswer(@MappingTarget Message.Format.QuestionAnswer.QuestionAnswerBuilder target, Message.Format.QuestionAnswer source);

    Message.Format.Replacement mergeFormatReplacement(@MappingTarget Message.Format.Replacement.ReplacementBuilder target, Message.Format.Replacement source);

    Message.Format.Translate mergeFormatTranslate(@MappingTarget Message.Format.Translate.TranslateBuilder target, Message.Format.Translate source);

    Message.Format.World mergeFormatWorld(@MappingTarget Message.Format.World.WorldBuilder target, Message.Format.World source);

    Message.Greeting mergeGreeting(@MappingTarget Message.Greeting.GreetingBuilder target, Message.Greeting source);

    Message.Join mergeJoin(@MappingTarget Message.Join.JoinBuilder target, Message.Join source);

    Message.Quit mergeQuit(@MappingTarget Message.Quit.QuitBuilder target, Message.Quit source);

    Message.Rightclick mergeRightclick(@MappingTarget Message.Rightclick.RightclickBuilder target, Message.Rightclick sourcesource);

    @Mapping(target = "objective", expression = "java(mergeScoreboardObjective(target.build().objective().toBuilder(), source.objective()))")
    Message.Scoreboard mergeScoreboard(@MappingTarget Message.Scoreboard.ScoreboardBuilder target, Message.Scoreboard source);

    @Mapping(target = "belowname", expression = "java(mergeScoreboardObjectiveBelowname(target.build().belowname().toBuilder(), source.belowname()))")
    @Mapping(target = "tabname", expression = "java(mergeScoreboardObjectiveTabname(target.build().tabname().toBuilder(), source.tabname()))")
    Message.Scoreboard.Objective mergeScoreboardObjective(@MappingTarget Message.Scoreboard.Objective.ObjectiveBuilder target, Message.Scoreboard.Objective source);

    Message.Scoreboard.Objective.Belowname mergeScoreboardObjectiveBelowname(@MappingTarget Message.Scoreboard.Objective.Belowname.BelownameBuilder target, Message.Scoreboard.Objective.Belowname source);

    Message.Scoreboard.Objective.Tabname mergeScoreboardObjectiveTabname(@MappingTarget Message.Scoreboard.Objective.Tabname.TabnameBuilder target, Message.Scoreboard.Objective.Tabname source);

    Message.Serverlink mergeServerlink(@MappingTarget Message.Serverlink.ServerlinkBuilder target, Message.Serverlink source);

    Message.Sidebar mergeSidebar(@MappingTarget Message.Sidebar.SidebarBuilder target, Message.Sidebar source);

    Message.Sign mergeSign(@MappingTarget Message.Sign.SignBuilder target, Message.Sign source);

    @Mapping(target = "icon", expression = "java(mergeStatusIcon(target.build().icon().toBuilder(), source.icon()))")
    @Mapping(target = "motd", expression = "java(mergeStatusMOTD(target.build().motd().toBuilder(), source.motd()))")
    @Mapping(target = "players", expression = "java(mergeStatusPlayers(target.build().players().toBuilder(), source.players()))")
    @Mapping(target = "version", expression = "java(mergeStatusVersion(target.build().version().toBuilder(), source.version()))")
    Message.Status mergeStatus(@MappingTarget Message.Status.StatusBuilder target, Message.Status source);

    Message.Status.Icon mergeStatusIcon(@MappingTarget Message.Status.Icon.IconBuilder target, Message.Status.Icon source);

    Message.Status.MOTD mergeStatusMOTD(@MappingTarget Message.Status.MOTD.MOTDBuilder target, Message.Status.MOTD source);

    Message.Status.Players mergeStatusPlayers(@MappingTarget Message.Status.Players.PlayersBuilder target, Message.Status.Players source);

    Message.Status.Version mergeStatusVersion(@MappingTarget Message.Status.Version.VersionBuilder target, Message.Status.Version source);

    @Mapping(target = "header", expression = "java(mergeTabHeader(target.build().header().toBuilder(), source.header()))")
    @Mapping(target = "footer", expression = "java(mergeTabFooter(target.build().footer().toBuilder(), source.footer()))")
    @Mapping(target = "playerlistname", expression = "java(mergeTabPlayerlistname(target.build().playerlistname().toBuilder(), source.playerlistname()))")
    Message.Tab mergeTab(@MappingTarget Message.Tab.TabBuilder target, Message.Tab source);

    Message.Tab.Header mergeTabHeader(@MappingTarget Message.Tab.Header.HeaderBuilder target, Message.Tab.Header source);

    Message.Tab.Footer mergeTabFooter(@MappingTarget Message.Tab.Footer.FooterBuilder target, Message.Tab.Footer source);

    Message.Tab.Playerlistname mergeTabPlayerlistname(@MappingTarget Message.Tab.Playerlistname.PlayerlistnameBuilder target, Message.Tab.Playerlistname source);

    Message.Update mergeUpdate(@MappingTarget Message.Update.UpdateBuilder target, Message.Update source);

    Message.Vanilla mergeVanilla(@MappingTarget Message.Vanilla.VanillaBuilder target, Message.Vanilla source);

}