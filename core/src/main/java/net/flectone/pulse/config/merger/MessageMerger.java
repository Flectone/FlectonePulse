package net.flectone.pulse.config.merger;

import net.flectone.pulse.config.Message;
import org.mapstruct.*;

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
    @Mapping(target = "objective", expression = "java(mergeObjective(target.build().objective().toBuilder(), source.objective()))")
    @Mapping(target = "quit", expression = "java(mergeQuit(target.build().quit().toBuilder(), source.quit()))")
    @Mapping(target = "rightclick", expression = "java(mergeRightclick(target.build().rightclick().toBuilder(), source.rightclick()))")
    @Mapping(target = "sidebar", expression = "java(mergeSidebar(target.build().sidebar().toBuilder(), source.sidebar()))")
    @Mapping(target = "sign", expression = "java(mergeSign(target.build().sign().toBuilder(), source.sign()))")
    @Mapping(target = "status", expression = "java(mergeStatus(target.build().status().toBuilder(), source.status()))")
    @Mapping(target = "tab", expression = "java(mergeTab(target.build().tab().toBuilder(), source.tab()))")
    @Mapping(target = "update", expression = "java(mergeUpdate(target.build().update().toBuilder(), source.update()))")
    @Mapping(target = "vanilla", expression = "java(mergeVanilla(target.build().vanilla().toBuilder(), source.vanilla()))")
    Message merge(@MappingTarget Message.MessageBuilder target, Message source);

    Message.Afk mergeAfk(@MappingTarget Message.Afk.AfkBuilder target, Message.Afk afk);

    Message.Anvil mergeAnvil(@MappingTarget Message.Anvil.AnvilBuilder target, Message.Anvil anvil);

    Message.Auto mergeAuto(@MappingTarget Message.Auto.AutoBuilder target, Message.Auto auto);

    Message.Book mergeBook(@MappingTarget Message.Book.BookBuilder target, Message.Book book);

    Message.Bossbar mergeBossbar(@MappingTarget Message.Bossbar.BossbarBuilder target, Message.Bossbar bossbar);

    Message.Brand mergeBrand(@MappingTarget Message.Brand.BrandBuilder target, Message.Brand brand);

    @Mapping(target = "interaction", expression = "java(mergeInteraction(target.build().interaction().toBuilder(), source.interaction()))")
    @Mapping(target = "modern", expression = "java(mergeModern(target.build().modern().toBuilder(), source.modern()))")
    Message.Bubble mergeBubble(@MappingTarget Message.Bubble.BubbleBuilder target, Message.Bubble source);

    Message.Bubble.Interaction mergeInteraction(@MappingTarget Message.Bubble.Interaction.InteractionBuilder target, Message.Bubble.Interaction interaction);

    Message.Bubble.Modern mergeModern(@MappingTarget Message.Bubble.Modern.ModernBuilder target, Message.Bubble.Modern modern);

    Message.Chat mergeChat(@MappingTarget Message.Chat.ChatBuilder target, Message.Chat chat);

    @Mapping(target = "fcolor", expression = "java(mergeFColor(target.build().fcolor().toBuilder(), source.fcolor()))")
    @Mapping(target = "fixation", expression = "java(mergeFixation(target.build().fixation().toBuilder(), source.fixation()))")
    @Mapping(target = "mention", expression = "java(mergeMention(target.build().mention().toBuilder(), source.mention()))")
    @Mapping(target = "moderation", expression = "java(mergeModeration(target.build().moderation().toBuilder(), source.moderation()))")
    @Mapping(target = "names", expression = "java(mergeNames(target.build().names().toBuilder(), source.names()))")
    @Mapping(target = "object", expression = "java(mergeObject(target.build().object().toBuilder(), source.object()))")
    @Mapping(target = "questionAnswer", expression = "java(mergeQuestionAnswer(target.build().questionAnswer().toBuilder(), source.questionAnswer()))")
    @Mapping(target = "replacement", expression = "java(mergeReplacement(target.build().replacement().toBuilder(), source.replacement()))")
    @Mapping(target = "scoreboard", expression = "java(mergeScoreboard(target.build().scoreboard().toBuilder(), source.scoreboard()))")
    @Mapping(target = "translate", expression = "java(mergeTranslate(target.build().translate().toBuilder(), source.translate()))")
    @Mapping(target = "world", expression = "java(mergeWorld(target.build().world().toBuilder(), source.world()))")
    Message.Format mergeFormat(@MappingTarget Message.Format.FormatBuilder target, Message.Format source);

    Message.Format.FColor mergeFColor(@MappingTarget Message.Format.FColor.FColorBuilder target, Message.Format.FColor fcolor);

    Message.Format.Fixation mergeFixation(@MappingTarget Message.Format.Fixation.FixationBuilder target, Message.Format.Fixation fixation);

    Message.Format.Mention mergeMention(@MappingTarget Message.Format.Mention.MentionBuilder target, Message.Format.Mention mention);

    @Mapping(target = "caps", expression = "java(mergeCaps(target.build().caps().toBuilder(), source.caps()))")
    @Mapping(target = "delete", expression = "java(mergeDelete(target.build().delete().toBuilder(), source.delete()))")
    @Mapping(target = "newbie", expression = "java(mergeNewbie(target.build().newbie().toBuilder(), source.newbie()))")
    @Mapping(target = "flood", expression = "java(mergeFlood(target.build().flood().toBuilder(), source.flood()))")
    @Mapping(target = "swear", expression = "java(mergeSwear(target.build().swear().toBuilder(), source.swear()))")
    Message.Format.Moderation mergeModeration(@MappingTarget Message.Format.Moderation.ModerationBuilder target, Message.Format.Moderation source);

    Message.Format.Moderation.Caps mergeCaps(@MappingTarget Message.Format.Moderation.Caps.CapsBuilder target, Message.Format.Moderation.Caps caps);

    Message.Format.Moderation.Delete mergeDelete(@MappingTarget Message.Format.Moderation.Delete.DeleteBuilder target, Message.Format.Moderation.Delete delete);

    Message.Format.Moderation.Newbie mergeNewbie(@MappingTarget Message.Format.Moderation.Newbie.NewbieBuilder target, Message.Format.Moderation.Newbie newbie);

    Message.Format.Moderation.Flood mergeFlood(@MappingTarget Message.Format.Moderation.Flood.FloodBuilder target, Message.Format.Moderation.Flood flood);

    Message.Format.Moderation.Swear mergeSwear(@MappingTarget Message.Format.Moderation.Swear.SwearBuilder target, Message.Format.Moderation.Swear swear);

    Message.Format.Names mergeNames(@MappingTarget Message.Format.Names.NamesBuilder target, Message.Format.Names names);

    Message.Format.Object mergeObject(@MappingTarget Message.Format.Object.ObjectBuilder target, Message.Format.Object object);

    Message.Format.QuestionAnswer mergeQuestionAnswer(@MappingTarget Message.Format.QuestionAnswer.QuestionAnswerBuilder target, Message.Format.QuestionAnswer questionAnswer);

    Message.Format.Replacement mergeReplacement(@MappingTarget Message.Format.Replacement.ReplacementBuilder target, Message.Format.Replacement replacement);

    Message.Format.Scoreboard mergeScoreboard(@MappingTarget Message.Format.Scoreboard.ScoreboardBuilder target, Message.Format.Scoreboard scoreboard);

    Message.Format.Translate mergeTranslate(@MappingTarget Message.Format.Translate.TranslateBuilder target, Message.Format.Translate translate);

    Message.Format.World mergeWorld(@MappingTarget Message.Format.World.WorldBuilder target, Message.Format.World world);

    Message.Greeting mergeGreeting(@MappingTarget Message.Greeting.GreetingBuilder target, Message.Greeting greeting);

    Message.Join mergeJoin(@MappingTarget Message.Join.JoinBuilder target, Message.Join join);

    @Mapping(target = "belowname", expression = "java(mergeBelowname(target.build().belowname().toBuilder(), source.belowname()))")
    @Mapping(target = "tabname", expression = "java(mergeTabname(target.build().tabname().toBuilder(), source.tabname()))")
    Message.Objective mergeObjective(@MappingTarget Message.Objective.ObjectiveBuilder target, Message.Objective source);

    Message.Objective.Belowname mergeBelowname(@MappingTarget Message.Objective.Belowname.BelownameBuilder target, Message.Objective.Belowname belowname);

    Message.Objective.Tabname mergeTabname(@MappingTarget Message.Objective.Tabname.TabnameBuilder target, Message.Objective.Tabname tabname);

    Message.Quit mergeQuit(@MappingTarget Message.Quit.QuitBuilder target, Message.Quit quit);

    Message.Rightclick mergeRightclick(@MappingTarget Message.Rightclick.RightclickBuilder target, Message.Rightclick rightclick);

    Message.Sidebar mergeSidebar(@MappingTarget Message.Sidebar.SidebarBuilder target, Message.Sidebar sidebar);

    Message.Sign mergeSign(@MappingTarget Message.Sign.SignBuilder target, Message.Sign sign);

    @Mapping(target = "icon", expression = "java(mergeIcon(target.build().icon().toBuilder(), source.icon()))")
    @Mapping(target = "motd", expression = "java(mergeMOTD(target.build().motd().toBuilder(), source.motd()))")
    @Mapping(target = "players", expression = "java(mergePlayers(target.build().players().toBuilder(), source.players()))")
    @Mapping(target = "version", expression = "java(mergeVersion(target.build().version().toBuilder(), source.version()))")
    Message.Status mergeStatus(@MappingTarget Message.Status.StatusBuilder target, Message.Status source);

    Message.Status.Icon mergeIcon(@MappingTarget Message.Status.Icon.IconBuilder target, Message.Status.Icon icon);

    Message.Status.MOTD mergeMOTD(@MappingTarget Message.Status.MOTD.MOTDBuilder target, Message.Status.MOTD motd);

    Message.Status.Players mergePlayers(@MappingTarget Message.Status.Players.PlayersBuilder target, Message.Status.Players players);

    Message.Status.Version mergeVersion(@MappingTarget Message.Status.Version.VersionBuilder target, Message.Status.Version version);

    @Mapping(target = "header", expression = "java(mergeHeader(target.build().header().toBuilder(), source.header()))")
    @Mapping(target = "footer", expression = "java(mergeFooter(target.build().footer().toBuilder(), source.footer()))")
    @Mapping(target = "playerlistname", expression = "java(mergePlayerlistname(target.build().playerlistname().toBuilder(), source.playerlistname()))")
    Message.Tab mergeTab(@MappingTarget Message.Tab.TabBuilder target, Message.Tab source);

    Message.Tab.Header mergeHeader(@MappingTarget Message.Tab.Header.HeaderBuilder target, Message.Tab.Header header);

    Message.Tab.Footer mergeFooter(@MappingTarget Message.Tab.Footer.FooterBuilder target, Message.Tab.Footer footer);

    Message.Tab.Playerlistname mergePlayerlistname(@MappingTarget Message.Tab.Playerlistname.PlayerlistnameBuilder target, Message.Tab.Playerlistname playerlistname);

    Message.Update mergeUpdate(@MappingTarget Message.Update.UpdateBuilder target, Message.Update update);

    Message.Vanilla mergeVanilla(@MappingTarget Message.Vanilla.VanillaBuilder target, Message.Vanilla vanilla);

}