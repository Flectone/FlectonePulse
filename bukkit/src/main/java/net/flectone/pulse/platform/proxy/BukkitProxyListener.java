package net.flectone.pulse.platform.proxy;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.database.dao.ColorsDAO;
import net.flectone.pulse.database.dao.ModerationDAO;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.connector.ProxyConnector;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.command.ball.BallModule;
import net.flectone.pulse.module.command.ban.BanModule;
import net.flectone.pulse.module.command.broadcast.BroadcastModule;
import net.flectone.pulse.module.command.coin.CoinModule;
import net.flectone.pulse.module.command.dice.DiceModule;
import net.flectone.pulse.module.command.do_.DoModule;
import net.flectone.pulse.module.command.helper.HelperModule;
import net.flectone.pulse.module.command.kick.KickModule;
import net.flectone.pulse.module.command.me.MeModule;
import net.flectone.pulse.module.command.mute.MuteModule;
import net.flectone.pulse.module.command.poll.PollModule;
import net.flectone.pulse.module.command.poll.model.Poll;
import net.flectone.pulse.module.command.rockpaperscissors.RockpaperscissorsModule;
import net.flectone.pulse.module.command.spy.SpyModule;
import net.flectone.pulse.module.command.stream.StreamModule;
import net.flectone.pulse.module.command.tell.TellModule;
import net.flectone.pulse.module.command.tictactoe.TictactoeModule;
import net.flectone.pulse.module.command.tictactoe.manager.TictactoeManager;
import net.flectone.pulse.module.command.tictactoe.model.TicTacToe;
import net.flectone.pulse.module.command.translateto.TranslatetoModule;
import net.flectone.pulse.module.command.try_.TryModule;
import net.flectone.pulse.module.command.unban.UnbanModule;
import net.flectone.pulse.module.command.unmute.UnmuteModule;
import net.flectone.pulse.module.command.unwarn.UnwarnModule;
import net.flectone.pulse.module.command.warn.WarnModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.integration.discord.listener.MessageCreateListener;
import net.flectone.pulse.module.integration.telegram.listener.MessageListener;
import net.flectone.pulse.module.integration.twitch.listener.ChannelMessageListener;
import net.flectone.pulse.module.message.advancement.AdvancementModule;
import net.flectone.pulse.module.message.advancement.model.Advancement;
import net.flectone.pulse.module.message.chat.BukkitChatModule;
import net.flectone.pulse.module.message.afk.BukkitAfkModule;
import net.flectone.pulse.module.message.death.DeathModule;
import net.flectone.pulse.module.message.death.model.Death;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.module.message.quit.QuitModule;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.Range;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Singleton
public class BukkitProxyListener implements PluginMessageListener {

    private final ColorsDAO colorsDAO;
    private final ModerationDAO moderationDAO;
    private final FileManager fileManager;
    private final FPlayerManager fPlayerManager;
    private final TaskScheduler taskScheduler;
    private final ProxyConnector proxyConnector;
    private final Gson gson;
    private final Injector injector;

    @Inject
    public BukkitProxyListener(ColorsDAO colorsDAO,
                               ModerationDAO moderationDAO,
                               FileManager fileManager,
                               FPlayerManager fPlayerManager,
                               TaskScheduler taskScheduler,
                               ProxyConnector proxyConnector,
                               Gson gson,
                               Injector injector) {
        this.colorsDAO = colorsDAO;
        this.moderationDAO = moderationDAO;
        this.fileManager = fileManager;
        this.fPlayerManager = fPlayerManager;
        this.taskScheduler = taskScheduler;
        this.proxyConnector = proxyConnector;
        this.gson = gson;
        this.injector = injector;
    }

    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, byte[] bytes) {
        if (!channel.equals(proxyConnector.getChannel())) return;
        if (!proxyConnector.isEnable()) return;

        taskScheduler.runAsync(() -> {
            ByteArrayDataInput input = ByteStreams.newDataInput(bytes);

            MessageTag tag = MessageTag.fromProxyString(input.readUTF());
            if (tag == null) return;

            int clustersCount = input.readInt();
            Set<String> proxyClusters = new HashSet<>(clustersCount);
            while (clustersCount != 0) {
                proxyClusters.add(input.readUTF());
                clustersCount--;
            }

            Set<String> configClusters = fileManager.getConfig().getClusters();
            if (!configClusters.isEmpty() && configClusters.stream().noneMatch(proxyClusters::contains)) return;

            boolean isPlayer = input.readBoolean();

            FEntity fEntity = gson.fromJson(input.readUTF(), isPlayer ? FPlayer.class : FEntity.class);

            switch (tag) {

                case COMMAND_ME -> {
                    MeModule meModule = injector.getInstance(MeModule.class);
                    if (meModule.checkModulePredicates(fEntity)) return;

                    String message = input.readUTF();

                    meModule.builder(fEntity)
                            .range(Range.SERVER)
                            .destination(meModule.getCommand().getDestination())
                            .format((fResolver, s) -> s.getFormat())
                            .message((fResolver, s) -> message)
                            .sound(meModule.getSound())
                            .sendBuilt();
                }

                case COMMAND_BALL -> {
                    BallModule ballModule = injector.getInstance(BallModule.class);
                    if (ballModule.checkModulePredicates(fEntity)) return;

                    int answer = input.readInt();
                    String string = input.readUTF();

                    ballModule.builder(fEntity)
                            .range(Range.SERVER)
                            .destination(ballModule.getCommand().getDestination())
                            .format(ballModule.replaceAnswer(answer))
                            .message((fResolver, s) -> string)
                            .sound(ballModule.getSound())
                            .sendBuilt();
                }

                case COMMAND_BAN -> {
                    BanModule banModule = injector.getInstance(BanModule.class);

                    FPlayer fModerator = gson.fromJson(input.readUTF(), FPlayer.class);
                    if (banModule.checkModulePredicates(fModerator)) return;

                    Moderation ban = gson.fromJson(input.readUTF(), Moderation.class);

                    banModule.kick(fModerator, (FPlayer) fEntity, ban);

                    banModule.builder(fEntity)
                            .range(Range.SERVER)
                            .destination(banModule.getCommand().getDestination())
                            .format(banModule.buildFormat(ban))
                            .sound(banModule.getSound())
                            .sendBuilt();
                }

                case COMMAND_BROADCAST -> {
                    BroadcastModule broadcastModule = injector.getInstance(BroadcastModule.class);
                    if (broadcastModule.checkModulePredicates(fEntity)) return;

                    String message = input.readUTF();

                    broadcastModule.builder(fEntity)
                            .range(Range.SERVER)
                            .destination(broadcastModule.getCommand().getDestination())
                            .format((fResolver, s) -> s.getFormat())
                            .message((fResolver, s) -> message)
                            .sound(broadcastModule.getSound())
                            .sendBuilt();
                }

                case COMMAND_CHATCOLOR -> colorsDAO.load(fPlayerManager.get(fEntity.getUuid()));

                case COMMAND_COIN -> {
                    CoinModule coinModule = injector.getInstance(CoinModule.class);
                    if (coinModule.checkModulePredicates(fEntity)) return;

                    int percent = input.readInt();

                    coinModule.builder(fEntity)
                            .range(Range.SERVER)
                            .destination(coinModule.getCommand().getDestination())
                            .format(coinModule.replaceResult(percent))
                            .sound(coinModule.getSound())
                            .sendBuilt();
                }

                case COMMAND_DICE -> {
                    DiceModule diceModule = injector.getInstance(DiceModule.class);
                    if (diceModule.checkModulePredicates(fEntity)) return;

                    List<Integer> cubes = gson.fromJson(input.readUTF(), new TypeToken<List<Integer>>() {}.getType());

                    diceModule.builder(fEntity)
                            .range(Range.SERVER)
                            .destination(diceModule.getCommand().getDestination())
                            .format(diceModule.replaceResult(cubes))
                            .sound(diceModule.getSound())
                            .sendBuilt();
                }

                case COMMAND_DO -> {
                    DoModule doModule = injector.getInstance(DoModule.class);
                    if (doModule.checkModulePredicates(fEntity)) return;

                    String message = input.readUTF();

                    doModule.builder(fEntity)
                            .range(Range.SERVER)
                            .destination(doModule.getCommand().getDestination())
                            .format((fResolver, s) -> s.getFormat())
                            .message((fResolver, s) -> message)
                            .sound(doModule.getSound())
                            .sendBuilt();
                }
                case COMMAND_HELPER -> {
                    HelperModule helperModule = injector.getInstance(HelperModule.class);
                    if (helperModule.checkModulePredicates(fEntity)) return;

                    String message = input.readUTF();

                    helperModule.builder(fEntity)
                            .destination(helperModule.getCommand().getDestination())
                            .filter(helperModule.getFilterSee())
                            .format((fResolver, s) -> s.getGlobal())
                            .message((fResolver, s) -> message)
                            .sound(helperModule.getSound())
                            .sendBuilt();
                }
                case COMMAND_MUTE -> {
                    MuteModule muteModule = injector.getInstance(MuteModule.class);

                    FPlayer fModerator = gson.fromJson(input.readUTF(), FPlayer.class);
                    if (muteModule.checkModulePredicates(fModerator)) return;

                    Moderation mute = gson.fromJson(input.readUTF(), Moderation.class);

                    FPlayer localFTarget = fPlayerManager.get(fEntity.getUuid());

                    if (!localFTarget.isUnknown()) {
                        localFTarget.addMute(mute);
                    }

                    muteModule.builder(fEntity)
                            .range(Range.SERVER)
                            .destination(muteModule.getCommand().getDestination())
                            .format(muteModule.buildFormat(mute))
                            .sound(muteModule.getSound())
                            .sendBuilt();

                    muteModule.sendForTarget(fModerator, (FPlayer) fEntity, mute);
                }
                case COMMAND_UNBAN -> {
                    UnbanModule unbanModule = injector.getInstance(UnbanModule.class);

                    FPlayer fPlayer = gson.fromJson(input.readUTF(), FPlayer.class);
                    if (unbanModule.checkModulePredicates(fPlayer)) return;

                    unbanModule.builder(fEntity)
                            .destination(unbanModule.getCommand().getDestination())
                            .range(Range.SERVER)
                            .filter(filter -> filter.isSetting(FPlayer.Setting.BAN))
                            .format(unwarn -> unwarn.getFormat().replace("<moderator>", fPlayer.getName()))
                            .sound(unbanModule.getCommand().getSound())
                            .sendBuilt();
                }
                case COMMAND_UNMUTE -> {
                    UnmuteModule unmuteModule = injector.getInstance(UnmuteModule.class);

                    FPlayer fPlayer = gson.fromJson(input.readUTF(), FPlayer.class);
                    if (unmuteModule.checkModulePredicates(fPlayer)) return;

                    FPlayer localFTarget = fPlayerManager.get(fEntity.getUuid());

                    if (!localFTarget.isUnknown()) {
                        localFTarget.clearMutes();
                        localFTarget.addMutes(moderationDAO.getValid(Moderation.Type.MUTE));
                    }

                    unmuteModule.builder(fEntity)
                            .destination(unmuteModule.getCommand().getDestination())
                            .range(Range.SERVER)
                            .filter(filter -> filter.isSetting(FPlayer.Setting.MUTE))
                            .format(unwarn -> unwarn.getFormat().replace("<moderator>", fPlayer.getName()))
                            .sound(unmuteModule.getCommand().getSound())
                            .sendBuilt();
                }
                case COMMAND_UNWARN -> {
                    UnwarnModule unwarnModule = injector.getInstance(UnwarnModule.class);

                    FPlayer fPlayer = gson.fromJson(input.readUTF(), FPlayer.class);
                    if (unwarnModule.checkModulePredicates(fPlayer)) return;

                    unwarnModule.builder(fEntity)
                            .destination(unwarnModule.getCommand().getDestination())
                            .range(Range.SERVER)
                            .filter(filter -> filter.isSetting(FPlayer.Setting.WARN))
                            .format(unwarn -> unwarn.getFormat().replace("<moderator>", fPlayer.getName()))
                            .sound(unwarnModule.getCommand().getSound())
                            .sendBuilt();
                }
                case COMMAND_POLL_VOTE -> injector.getInstance(PollModule.class).vote(fEntity, input.readInt(), input.readInt());
                case COMMAND_POLL_CREATE_MESSAGE -> {
                    PollModule pollModule = injector.getInstance(PollModule.class);
                    if (pollModule.checkModulePredicates(fEntity)) return;

                    Poll poll = gson.fromJson(input.readUTF(), Poll.class);
                    pollModule.put(poll);

                    String title = input.readUTF();
                    Map<String, String> answerSet = gson.fromJson(input.readUTF(), new TypeToken<Map<String, String>>() {}.getType());

                    pollModule.builder(fEntity)
                            .range(Range.SERVER)
                            .format(pollModule.createFormat(fEntity, answerSet, poll))
                            .message((fResolver, s) -> title)
                            .sound(pollModule.getSound())
                            .sendBuilt();
                }
                case COMMAND_SPY -> {
                    String action = input.readUTF();
                    String string = input.readUTF();

                    SpyModule spyModule = injector.getInstance(SpyModule.class);
                    spyModule.builder(fEntity)
                            .range(Range.SERVER)
                            .destination(spyModule.getCommand().getDestination())
                            .filter(fReceiver -> !fEntity.equals(fReceiver)
                                    && !spyModule.checkModulePredicates(fReceiver)
                                    && fReceiver.isSetting(FPlayer.Setting.SPY)
                                    && fReceiver.isOnline()
                            )
                            .format(spyModule.replaceAction(action))
                            .message((fResolver, s) -> string)
                            .sendBuilt();
                }
                case COMMAND_STREAM -> {
                    StreamModule streamModule = injector.getInstance(StreamModule.class);
                    if (streamModule.checkModulePredicates(fEntity)) return;

                    String message = input.readUTF();
                    streamModule.builder(fEntity)
                            .range(Range.SERVER)
                            .destination(streamModule.getCommand().getDestination())
                            .format(streamModule.replaceUrls(message))
                            .sendBuilt();
                }
                case COMMAND_TELL -> {
                    TellModule tellModule = injector.getInstance(TellModule.class);
                    if (tellModule.checkModulePredicates(fEntity)) return;

                    UUID receiverUUID = UUID.fromString(input.readUTF());
                    String message = input.readUTF();

                    FPlayer fReceiver = fPlayerManager.get(receiverUUID);
                    if (fReceiver.isUnknown()) return;

                    IntegrationModule integrationModule = injector.getInstance(IntegrationModule.class);
                    if (integrationModule.isVanished(fReceiver)) return;

                    tellModule.send(fEntity, fReceiver, (fResolver, s) -> s.getReceiver(), message);
                }
                case COMMAND_TRANSLATETO -> {
                    TranslatetoModule translatetoModule = injector.getInstance(TranslatetoModule.class);
                    if (translatetoModule.checkModulePredicates(fEntity)) return;

                    String targetLang = input.readUTF();
                    String message = input.readUTF();

                    translatetoModule.builder(fEntity)
                            .range(Range.SERVER)
                            .destination(translatetoModule.getCommand().getDestination())
                            .format(translatetoModule.replaceLanguage(targetLang))
                            .message((fResolver, s) -> message)
                            .sound(translatetoModule.getSound())
                            .sendBuilt();
                }
                case COMMAND_TRY -> {
                    TryModule tryModule = injector.getInstance(TryModule.class);
                    if (tryModule.checkModulePredicates(fEntity)) return;

                    int value = input.readInt();
                    String message = input.readUTF();

                    tryModule.builder(fEntity)
                            .range(Range.SERVER)
                            .destination(tryModule.getCommand().getDestination())
                            .tag(MessageTag.COMMAND_TRY)
                            .format(tryModule.replacePercent(value))
                            .message((fResolver, s) -> message)
                            .sound(tryModule.getSound())
                            .sendBuilt();
                }
                case COMMAND_WARN -> {
                    WarnModule warnModule = injector.getInstance(WarnModule.class);

                    FPlayer fModerator = gson.fromJson(input.readUTF(), FPlayer.class);
                    if (warnModule.checkModulePredicates(fModerator)) return;

                    Moderation warn = gson.fromJson(input.readUTF(), Moderation.class);

                    warnModule.builder(fEntity)
                            .range(Range.SERVER)
                            .destination(warnModule.getCommand().getDestination())
                            .format(warnModule.buildFormat(warn))
                            .sound(warnModule.getSound())
                            .sendBuilt();

                    warnModule.send(fModerator, (FPlayer) fEntity, warn);
                }
                case COMMAND_KICK -> {
                    KickModule kickModule = injector.getInstance(KickModule.class);

                    FPlayer fModerator = gson.fromJson(input.readUTF(), FPlayer.class);
                    if (kickModule.checkModulePredicates(fModerator)) return;

                    Moderation kick = gson.fromJson(input.readUTF(), Moderation.class);

                    kickModule.builder(fEntity)
                            .range(Range.SERVER)
                            .destination(kickModule.getCommand().getDestination())
                            .format(kickModule.buildFormat(kick))
                            .sound(kickModule.getSound())
                            .sendBuilt();

                    kickModule.kick(fModerator, (FPlayer) fEntity, kick);
                }
                case COMMAND_TICTACTOE_CREATE -> {
                    if (!(fEntity instanceof FPlayer fPlayer)) return;

                    FPlayer fReceiver = gson.fromJson(input.readUTF(), FPlayer.class);
                    int ticTacToeId = input.readInt();
                    boolean isHard = input.readBoolean();

                    TictactoeManager tictactoeManager = injector.getInstance(TictactoeManager.class);

                    TicTacToe ticTacToe = tictactoeManager.get(ticTacToeId);
                    if (tictactoeManager.get(ticTacToeId) == null) {
                        ticTacToe = tictactoeManager.create(ticTacToeId, fPlayer, fReceiver, isHard);
                    }

                    injector.getInstance(TictactoeModule.class).sendCreateMessage(fPlayer, fReceiver, ticTacToe);
                }
                case COMMAND_TICTACTOE_MOVE -> {
                    if (!(fEntity instanceof FPlayer fPlayer)) return;

                    FPlayer fReceiver = gson.fromJson(input.readUTF(), FPlayer.class);
                    TicTacToe ticTacToe = injector.getInstance(TictactoeManager.class).fromString(input.readUTF());
                    int typeTitle = input.readInt();
                    String move = input.readUTF();

                    injector.getInstance(TictactoeModule.class).sendMoveMessage(fPlayer, fReceiver, ticTacToe, typeTitle, move);
                }
                case CHAT -> {
                    String chat = input.readUTF();
                    String message = input.readUTF();

                    injector.getInstance(BukkitChatModule.class).send(fEntity, chat, message);
                }
                case COMMAND_ROCKPAPERSCISSORS_CREATE -> {
                    UUID id = UUID.fromString(input.readUTF());
                    UUID receiver = UUID.fromString(input.readUTF());
                    injector.getInstance(RockpaperscissorsModule.class).create(id, fEntity, receiver);
                }
                case COMMAND_ROCKPAPERSCISSORS_MOVE -> {
                    UUID id = UUID.fromString(input.readUTF());
                    String move = input.readUTF();

                    injector.getInstance(RockpaperscissorsModule.class).move(id, fEntity, move);
                }
                case COMMAND_ROCKPAPERSCISSORS_FINAL -> {
                    if (!(fEntity instanceof FPlayer fPlayer)) return;

                    UUID id = UUID.fromString(input.readUTF());
                    String move = input.readUTF();

                    injector.getInstance(RockpaperscissorsModule.class).finalMove(id, fPlayer, move);
                }
                case FROM_DISCORD_TO_MINECRAFT -> {
                    String nickname = input.readUTF();
                    String string = input.readUTF();

                    MessageCreateListener messageCreateListener = injector.getInstance(MessageCreateListener.class);
                    messageCreateListener.builder(fEntity)
                            .range(Range.SERVER)
                            .destination(messageCreateListener.getIntegration().getDestination())
                            .tag(MessageTag.FROM_DISCORD_TO_MINECRAFT)
                            .format((fResolver, s) -> s.getForMinecraft().replace("<name>", nickname))
                            .message((fResolver, s) -> string)
                            .sound(messageCreateListener.getSound())
                            .sendBuilt();
                }
                case FROM_TWITCH_TO_MINECRAFT -> {
                    String nickname = input.readUTF();
                    String channelName = input.readUTF();
                    String string = input.readUTF();

                    ChannelMessageListener channelMessageListener = injector.getInstance(ChannelMessageListener.class);
                    channelMessageListener.builder(fEntity)
                            .range(Range.SERVER)
                            .destination(channelMessageListener.getIntegration().getDestination())
                            .tag(MessageTag.FROM_TWITCH_TO_MINECRAFT)
                            .format((fResolver, s) -> s.getForMinecraft()
                                    .replace("<name>", nickname)
                                    .replace("<channel>", channelName)
                            )
                            .message((fResolver, s) -> string)
                            .sound(channelMessageListener.getSound())
                            .sendBuilt();
                }
                case FROM_TELEGRAM_TO_MINECRAFT -> {
                    String author = input.readUTF();
                    String chat = input.readUTF();
                    String text = input.readUTF();

                    MessageListener messageListener = injector.getInstance(MessageListener.class);
                    messageListener.builder(fEntity)
                            .range(Range.PROXY)
                            .destination(messageListener.getIntegration().getDestination())
                            .tag(MessageTag.FROM_TELEGRAM_TO_MINECRAFT)
                            .format((fResolver, s) -> s.getForMinecraft()
                                    .replace("<name>", author)
                                    .replace("<chat>", chat)
                            )
                            .message((fResolver, s) -> text)
                            .sound(messageListener.getSound())
                            .sendBuilt();
                }
                case ADVANCEMENT -> {
                    AdvancementModule advancementModule = injector.getInstance(AdvancementModule.class);
                    if (advancementModule.checkModulePredicates(fEntity)) return;

                    Advancement advancement = gson.fromJson(input.readUTF(), Advancement.class);

                    advancementModule.builder(fEntity)
                            .range(Range.SERVER)
                            .destination(advancementModule.getMessage().getDestination())
                            .tag(MessageTag.ADVANCEMENT)
                            .format((fResolver, s) -> advancementModule.convert(s, advancement))
                            .tagResolvers(fResolver -> new TagResolver[]{advancementModule.advancementTag(fEntity, fResolver, advancement)})
                            .sound(advancementModule.getSound())
                            .sendBuilt();
                }
                case DEATH -> {
                    DeathModule deathModule = injector.getInstance(DeathModule.class);
                    if (deathModule.checkModulePredicates(fEntity)) return;

                    Death death = gson.fromJson(input.readUTF(), Death.class);

                    deathModule.builder(fEntity)
                            .range(Range.SERVER)
                            .destination(deathModule.getMessage().getDestination())
                            .tag(MessageTag.DEATH)
                            .format((fResolver, s) -> s.getTypes().get(death.getKey()))
                            .tagResolvers(fResolver -> new TagResolver[]{deathModule.killerTag(fResolver, death.getKiller()), deathModule.byItemTag(death.getItem())})
                            .sound(deathModule.getSound())
                            .sendBuilt();
                }
                case JOIN -> {
                    JoinModule joinModule = injector.getInstance(JoinModule.class);
                    if (joinModule.checkModulePredicates(fEntity)) return;

                    boolean hasPlayedBefore = input.readBoolean();

                    joinModule.builder(fEntity)
                            .tag(MessageTag.JOIN)
                            .destination(joinModule.getMessage().getDestination())
                            .range(Range.SERVER)
                            .filter(fReceiver -> fReceiver.isSetting(FPlayer.Setting.JOIN))
                            .format(s -> hasPlayedBefore || !joinModule.getMessage().isFirst() ? s.getFormat() : s.getFormatFirstTime())
                            .sound(joinModule.getSound())
                            .sendBuilt();
                }
                case QUIT -> {
                    QuitModule quitModule = injector.getInstance(QuitModule.class);
                    if (quitModule.checkModulePredicates(fEntity)) return;

                    quitModule.builder(fEntity)
                            .tag(MessageTag.QUIT)
                            .destination(quitModule.getMessage().getDestination())
                            .range(Range.SERVER)
                            .filter(fReceiver -> fReceiver.isSetting(FPlayer.Setting.QUIT))
                            .format(Localization.Message.Quit::getFormat)
                            .sound(quitModule.getSound())
                            .sendBuilt();
                }
                case AFK -> {
                    BukkitAfkModule afkModule = injector.getInstance(BukkitAfkModule.class);
                    if (afkModule.checkModulePredicates(fEntity)) return;

                    boolean isAfk = input.readBoolean();

                    afkModule.builder(fEntity)
                            .tag(MessageTag.AFK)
                            .destination(afkModule.getMessage().getDestination())
                            .filter(fReceiver -> fReceiver.isSetting(FPlayer.Setting.AFK))
                            .format(s -> isAfk
                                    ? s.getFormatFalse().getGlobal()
                                    : s.getFormatTrue().getGlobal()
                            )
                            .sound(afkModule.getSound())
                            .sendBuilt();
                }
            }
        });
    }
}
