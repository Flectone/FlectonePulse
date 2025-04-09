package net.flectone.pulse.handler;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.manager.FileManager;
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
import net.flectone.pulse.module.message.afk.AfkModule;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.module.message.death.DeathModule;
import net.flectone.pulse.module.message.death.model.Death;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.module.message.quit.QuitModule;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.Range;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Singleton
public class ProxyMessageHandler {

    private final Injector injector;
    private final FileManager fileManager;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final Gson gson;

    @Inject
    public ProxyMessageHandler(Injector injector,
                               FileManager fileManager,
                               FPlayerService fPlayerService,
                               ModerationService moderationService,
                               Gson gson) {
        this.injector = injector;
        this.fileManager = fileManager;
        this.fPlayerService = fPlayerService;
        this.moderationService = moderationService;
        this.gson = gson;
    }

    @Async
    public void handleProxyMessage(byte[] bytes) {
        ByteArrayDataInput input = ByteStreams.newDataInput(bytes);
        MessageTag tag = MessageTag.fromProxyString(input.readUTF());
        if (tag == null) return;

        switch (tag) {
            case SYSTEM_ONLINE -> handleSystemOnline(input);
            case SYSTEM_OFFLINE -> handleSystemOffline(input);
            default -> handleTaggedMessage(input, tag);
        }
    }

    private void handleSystemOnline(ByteArrayDataInput input) {
        fPlayerService.invalidateOffline(UUID.fromString(input.readUTF()));
    }

    private void handleSystemOffline(ByteArrayDataInput input) {
        fPlayerService.invalidateOnline(UUID.fromString(input.readUTF()));
    }

    private void handleTaggedMessage(ByteArrayDataInput input, MessageTag tag) {
        int clustersCount = input.readInt();
        boolean isPlayer = input.readBoolean();
        FEntity fEntity = gson.fromJson(input.readUTF(), isPlayer ? FPlayer.class : FEntity.class);

        if (handleModerationInvalidation(tag, fEntity)) {
            return;
        }

        Set<String> proxyClusters = readClusters(input, clustersCount);
        Set<String> configClusters = fileManager.getConfig().getClusters();
        if (configClusters.isEmpty() || configClusters.stream().anyMatch(proxyClusters::contains)) {
            return;
        }

        switch (tag) {
            case COMMAND_ME -> handleMeCommand(input, fEntity);
            case COMMAND_BALL -> handleBallCommand(input, fEntity);
            case COMMAND_BAN -> handleBanCommand(input, fEntity);
            case COMMAND_BROADCAST -> handleBroadcastCommand(input, fEntity);
            case COMMAND_CHATCOLOR -> handleChatColorCommand(fEntity);
            case COMMAND_COIN -> handleCoinCommand(input, fEntity);
            case COMMAND_DICE -> handleDiceCommand(input, fEntity);
            case COMMAND_DO -> handleDoCommand(input, fEntity);
            case COMMAND_HELPER -> handleHelperCommand(input, fEntity);
            case COMMAND_MUTE -> handleMuteCommand(input, fEntity);
            case COMMAND_UNBAN -> handleUnbanCommand(input, fEntity);
            case COMMAND_UNMUTE -> handleUnmuteCommand(input, fEntity);
            case COMMAND_UNWARN -> handleUnwarnCommand(input, fEntity);
            case COMMAND_POLL_VOTE -> handlePollVote(input, fEntity);
            case COMMAND_POLL_CREATE_MESSAGE -> handlePollCreate(input, fEntity);
            case COMMAND_SPY -> handleSpyCommand(input, fEntity);
            case COMMAND_STREAM -> handleStreamCommand(input, fEntity);
            case COMMAND_TELL -> handleTellCommand(input, fEntity);
            case COMMAND_TRANSLATETO -> handleTranslateToCommand(input, fEntity);
            case COMMAND_TRY -> handleTryCommand(input, fEntity);
            case COMMAND_WARN -> handleWarnCommand(input, fEntity);
            case COMMAND_KICK -> handleKickCommand(input, fEntity);
            case COMMAND_TICTACTOE_CREATE -> handleTicTacToeCreate(input, fEntity);
            case COMMAND_TICTACTOE_MOVE -> handleTicTacToeMove(input, fEntity);
            case CHAT -> handleChatMessage(input, fEntity);
            case COMMAND_ROCKPAPERSCISSORS_CREATE -> handleRockPaperScissorsCreate(input, fEntity);
            case COMMAND_ROCKPAPERSCISSORS_MOVE -> handleRockPaperScissorsMove(input, fEntity);
            case COMMAND_ROCKPAPERSCISSORS_FINAL -> handleRockPaperScissorsFinal(input, fEntity);
            case FROM_DISCORD_TO_MINECRAFT -> handleDiscordMessage(input, fEntity);
            case FROM_TWITCH_TO_MINECRAFT -> handleTwitchMessage(input, fEntity);
            case FROM_TELEGRAM_TO_MINECRAFT -> handleTelegramMessage(input, fEntity);
            case ADVANCEMENT -> handleAdvancement(input, fEntity);
            case DEATH -> handleDeath(input, fEntity);
            case JOIN -> handleJoin(input, fEntity);
            case QUIT -> handleQuit(input, fEntity);
            case AFK -> handleAfk(input, fEntity);
        }
    }

    private boolean handleModerationInvalidation(MessageTag tag, FEntity fEntity) {
        return switch (tag) {
            case SYSTEM_BAN -> {
                moderationService.invalidateBans(fEntity.getUuid());
                yield true;
            }
            case SYSTEM_MUTE -> {
                moderationService.invalidateMutes(fEntity.getUuid());
                yield true;
            }
            case SYSTEM_WARN -> {
                moderationService.invalidateWarns(fEntity.getUuid());
                yield true;
            }
            default -> false;
        };
    }

    private Set<String> readClusters(ByteArrayDataInput input, int clustersCount) {
        Set<String> clusters = new HashSet<>(clustersCount);
        for (int i = 0; i < clustersCount; i++) {
            clusters.add(input.readUTF());
        }

        return clusters;
    }

    private void handleMeCommand(ByteArrayDataInput input, FEntity fEntity) {
        MeModule module = injector.getInstance(MeModule.class);
        if (module.checkModulePredicates(fEntity)) return;

        module.builder(fEntity)
                .range(Range.SERVER)
                .destination(module.getCommand().getDestination())
                .format((fResolver, s) -> s.getFormat())
                .message((fResolver, s) -> input.readUTF())
                .sound(module.getSound())
                .sendBuilt();
    }

    private void handleBallCommand(ByteArrayDataInput input, FEntity fEntity) {
        BallModule module = injector.getInstance(BallModule.class);
        if (module.checkModulePredicates(fEntity)) return;

        module.builder(fEntity)
                .range(Range.SERVER)
                .destination(module.getCommand().getDestination())
                .format(module.replaceAnswer(input.readInt()))
                .message((fResolver, s) -> input.readUTF())
                .sound(module.getSound())
                .sendBuilt();
    }

    private void handleBanCommand(ByteArrayDataInput input, FEntity fEntity) {
        BanModule module = injector.getInstance(BanModule.class);
        FPlayer moderator = gson.fromJson(input.readUTF(), FPlayer.class);
        if (module.checkModulePredicates(moderator)) return;

        Moderation ban = gson.fromJson(input.readUTF(), Moderation.class);
        module.kick(moderator, (FPlayer) fEntity, ban);

        module.builder(fEntity)
                .range(Range.SERVER)
                .destination(module.getCommand().getDestination())
                .format(module.buildFormat(ban))
                .sound(module.getSound())
                .sendBuilt();
    }

    private void handleBroadcastCommand(ByteArrayDataInput input, FEntity fEntity) {
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

    private void handleChatColorCommand(FEntity fEntity) {
        fPlayerService.loadColors(fPlayerService.getFPlayer(fEntity.getUuid()));
    }

    private void handleCoinCommand(ByteArrayDataInput input, FEntity fEntity) {
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

    private void handleDiceCommand(ByteArrayDataInput input, FEntity fEntity) {
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

    private void handleDoCommand(ByteArrayDataInput input, FEntity fEntity) {
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

    private void handleHelperCommand(ByteArrayDataInput input, FEntity fEntity) {
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

    private void handleMuteCommand(ByteArrayDataInput input, FEntity fEntity) {
        MuteModule muteModule = injector.getInstance(MuteModule.class);

        FPlayer fModerator = gson.fromJson(input.readUTF(), FPlayer.class);
        if (muteModule.checkModulePredicates(fModerator)) return;

        Moderation mute = gson.fromJson(input.readUTF(), Moderation.class);

        muteModule.builder(fEntity)
                .range(Range.SERVER)
                .destination(muteModule.getCommand().getDestination())
                .format(muteModule.buildFormat(mute))
                .sound(muteModule.getSound())
                .sendBuilt();

        muteModule.sendForTarget(fModerator, (FPlayer) fEntity, mute);
    }

    private void handleUnbanCommand(ByteArrayDataInput input, FEntity fEntity) {
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

    private void handleUnmuteCommand(ByteArrayDataInput input, FEntity fEntity) {
        UnmuteModule unmuteModule = injector.getInstance(UnmuteModule.class);

        FPlayer fPlayer = gson.fromJson(input.readUTF(), FPlayer.class);
        if (unmuteModule.checkModulePredicates(fPlayer)) return;

        unmuteModule.builder(fEntity)
                .destination(unmuteModule.getCommand().getDestination())
                .range(Range.SERVER)
                .filter(filter -> filter.isSetting(FPlayer.Setting.MUTE))
                .format(unwarn -> unwarn.getFormat().replace("<moderator>", fPlayer.getName()))
                .sound(unmuteModule.getCommand().getSound())
                .sendBuilt();
    }

    private void handleUnwarnCommand(ByteArrayDataInput input, FEntity fEntity) {
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

    private void handlePollVote(ByteArrayDataInput input, FEntity fEntity) {
        injector.getInstance(PollModule.class).vote(fEntity, input.readInt(), input.readInt());
    }

    private void handlePollCreate(ByteArrayDataInput input, FEntity fEntity) {
        PollModule pollModule = injector.getInstance(PollModule.class);
        if (pollModule.checkModulePredicates(fEntity)) return;

        Poll poll = gson.fromJson(input.readUTF(), Poll.class);
        pollModule.saveAndUpdateLast(poll);

        pollModule.builder(fEntity)
                .range(Range.SERVER)
                .format(pollModule.resolvePollFormat(fEntity, poll, PollModule.Status.START))
                .message((fResolver, s) -> poll.getTitle())
                .sound(pollModule.getSound())
                .sendBuilt();
    }

    private void handleSpyCommand(ByteArrayDataInput input, FEntity fEntity) {
        String action = input.readUTF();
        String string = input.readUTF();

        SpyModule spyModule = injector.getInstance(SpyModule.class);
        if (!spyModule.isEnable()) return;

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

    private void handleStreamCommand(ByteArrayDataInput input, FEntity fEntity) {
        StreamModule streamModule = injector.getInstance(StreamModule.class);
        if (streamModule.checkModulePredicates(fEntity)) return;

        String message = input.readUTF();
        streamModule.builder(fEntity)
                .range(Range.SERVER)
                .destination(streamModule.getCommand().getDestination())
                .format(streamModule.replaceUrls(message))
                .sendBuilt();
    }

    private void handleTellCommand(ByteArrayDataInput input, FEntity fEntity) {
        TellModule tellModule = injector.getInstance(TellModule.class);
        if (tellModule.checkModulePredicates(fEntity)) return;

        UUID receiverUUID = UUID.fromString(input.readUTF());
        String message = input.readUTF();

        FPlayer fReceiver = fPlayerService.getFPlayer(receiverUUID);
        if (fReceiver.isUnknown()) return;

        IntegrationModule integrationModule = injector.getInstance(IntegrationModule.class);
        if (integrationModule.isVanished(fReceiver)) return;

        tellModule.send(fEntity, fReceiver, (fResolver, s) -> s.getReceiver(), message);
    }

    private void handleTranslateToCommand(ByteArrayDataInput input, FEntity fEntity) {
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

    private void handleTryCommand(ByteArrayDataInput input, FEntity fEntity) {
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

    private void handleWarnCommand(ByteArrayDataInput input, FEntity fEntity) {
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

    private void handleKickCommand(ByteArrayDataInput input, FEntity fEntity) {
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

    private void handleTicTacToeCreate(ByteArrayDataInput input, FEntity fEntity) {
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

    private void handleTicTacToeMove(ByteArrayDataInput input, FEntity fEntity) {
        if (!(fEntity instanceof FPlayer fPlayer)) return;

        FPlayer fReceiver = gson.fromJson(input.readUTF(), FPlayer.class);
        TicTacToe ticTacToe = injector.getInstance(TictactoeManager.class).fromString(input.readUTF());
        int typeTitle = input.readInt();
        String move = input.readUTF();

        injector.getInstance(TictactoeModule.class).sendMoveMessage(fPlayer, fReceiver, ticTacToe, typeTitle, move);
    }

    private void handleChatMessage(ByteArrayDataInput input, FEntity fEntity) {
        String chat = input.readUTF();
        String message = input.readUTF();

        injector.getInstance(ChatModule.class).send(fEntity, chat, message);
    }

    private void handleRockPaperScissorsCreate(ByteArrayDataInput input, FEntity fEntity) {
        UUID id = UUID.fromString(input.readUTF());
        UUID receiver = UUID.fromString(input.readUTF());
        injector.getInstance(RockpaperscissorsModule.class).create(id, fEntity, receiver);
    }

    private void handleRockPaperScissorsMove(ByteArrayDataInput input, FEntity fEntity) {
        UUID id = UUID.fromString(input.readUTF());
        String move = input.readUTF();

        injector.getInstance(RockpaperscissorsModule.class).move(id, fEntity, move);
    }

    private void handleRockPaperScissorsFinal(ByteArrayDataInput input, FEntity fEntity) {
        if (!(fEntity instanceof FPlayer fPlayer)) return;

        UUID id = UUID.fromString(input.readUTF());
        String move = input.readUTF();

        injector.getInstance(RockpaperscissorsModule.class).sendFinalMessage(id, fPlayer, move);
    }

    private void handleDiscordMessage(ByteArrayDataInput input, FEntity fEntity) {
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

    private void handleTwitchMessage(ByteArrayDataInput input, FEntity fEntity) {
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

    private void handleTelegramMessage(ByteArrayDataInput input, FEntity fEntity) {
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

    private void handleAdvancement(ByteArrayDataInput input, FEntity fEntity) {
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

    private void handleDeath(ByteArrayDataInput input, FEntity fEntity) {
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

    private void handleJoin(ByteArrayDataInput input, FEntity fEntity) {
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

    private void handleQuit(ByteArrayDataInput input, FEntity fEntity) {
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

    private void handleAfk(ByteArrayDataInput input, FEntity fEntity) {
        AfkModule afkModule = injector.getInstance(AfkModule.class);
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
