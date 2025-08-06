package net.flectone.pulse.platform.handler;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.module.message.format.moderation.delete.DeleteModule;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.command.anon.AnonModule;
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
import net.flectone.pulse.module.message.advancement.model.ChatAdvancement;
import net.flectone.pulse.module.message.afk.AfkModule;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.module.message.death.DeathModule;
import net.flectone.pulse.module.message.death.model.Death;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.module.message.quit.QuitModule;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Singleton
public class ProxyMessageHandler {

    private final Injector injector;
    private final FileResolver fileResolver;
    private final FPlayerService fPlayerService;
    private final FLogger fLogger;
    private final ModerationService moderationService;
    private final Gson gson;

    @Inject
    public ProxyMessageHandler(Injector injector,
                               FileResolver fileResolver,
                               FPlayerService fPlayerService,
                               FLogger fLogger,
                               ModerationService moderationService,
                               Gson gson) {
        this.injector = injector;
        this.fileResolver = fileResolver;
        this.fPlayerService = fPlayerService;
        this.fLogger = fLogger;
        this.moderationService = moderationService;
        this.gson = gson;
    }

    @Async
    public void handleProxyMessage(byte[] bytes) {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
             DataInputStream input = new DataInputStream(byteStream)) {

            MessageType tag = MessageType.fromProxyString(input.readUTF());
            if (tag == null) return;

            switch (tag) {
                case SYSTEM_ONLINE -> handleSystemOnline(input);
                case SYSTEM_OFFLINE -> handleSystemOffline(input);
                default -> handleTaggedMessage(input, tag);
            }
        } catch (IOException e) {
            fLogger.warning(e);
        }
    }

    private void handleSystemOnline(DataInputStream input) throws IOException {
        fPlayerService.invalidateOffline(UUID.fromString(input.readUTF()));
    }

    private void handleSystemOffline(DataInputStream input) throws IOException {
        fPlayerService.invalidateOnline(UUID.fromString(input.readUTF()));
    }

    private void handleTaggedMessage(DataInputStream input, MessageType tag) throws IOException {
        int clustersCount = input.readInt();
        Set<String> proxyClusters = readClusters(input, clustersCount);

        boolean isPlayer = input.readBoolean();

        FEntity fEntity = gson.fromJson(input.readUTF(), isPlayer ? FPlayer.class : FEntity.class);

        if (handleModerationInvalidation(tag, fEntity)) {
            return;
        }

        Set<String> configClusters = fileResolver.getConfig().getClusters();
        if (!configClusters.isEmpty() && configClusters.stream().noneMatch(proxyClusters::contains)) {
            return;
        }

        switch (tag) {
            case COMMAND_ANON -> handleAnonCommand(input, fEntity);
            case COMMAND_ME -> handleMeCommand(input, fEntity);
            case COMMAND_BALL -> handleBallCommand(input, fEntity);
            case COMMAND_BAN -> handleBanCommand(input, fEntity);
            case COMMAND_BROADCAST -> handleBroadcastCommand(input, fEntity);
            case COMMAND_CHATCOLOR -> handleChatColorCommand(fEntity);
            case COMMAND_CHATSETTING -> handleChatSettingCommand(fEntity);
            case COMMAND_COIN -> handleCoinCommand(input, fEntity);
            case COMMAND_DELETE -> handleDeleteCommand(input, fEntity);
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

    private boolean handleModerationInvalidation(MessageType tag, FEntity fEntity) {
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

    private Set<String> readClusters(DataInputStream input, int clustersCount) throws IOException {
        Set<String> clusters = HashSet.newHashSet(clustersCount);
        for (int i = 0; i < clustersCount; i++) {
            clusters.add(input.readUTF());
        }

        return clusters;
    }

    private void handleAnonCommand(DataInputStream input, FEntity fEntity) throws IOException {
        AnonModule module = injector.getInstance(AnonModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        String message = input.readUTF();

        module.builder(fEntity)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getAnon().getDestination())
                .format(Localization.Command.Anon::getFormat)
                .message(message)
                .sound(module.getSound())
                .sendBuilt();
    }

    private void handleMeCommand(DataInputStream input, FEntity fEntity) throws IOException {
        MeModule module = injector.getInstance(MeModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        String message = input.readUTF();

        module.builder(fEntity)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getMe().getDestination())
                .format(Localization.Command.Me::getFormat)
                .message(message)
                .sound(module.getSound())
                .sendBuilt();
    }

    private void handleBallCommand(DataInputStream input, FEntity fEntity) throws IOException {
        BallModule module = injector.getInstance(BallModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        int answer = input.readInt();
        String message = input.readUTF();

        module.builder(fEntity)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getBall().getDestination())
                .format(module.replaceAnswer(answer))
                .message(message)
                .sound(module.getSound())
                .sendBuilt();
    }

    private void handleBanCommand(DataInputStream input, FEntity fEntity) throws IOException {
        BanModule module = injector.getInstance(BanModule.class);
        FPlayer moderator = gson.fromJson(input.readUTF(), FPlayer.class);
        if (module.isModuleDisabledFor(moderator)) return;

        Moderation ban = gson.fromJson(input.readUTF(), Moderation.class);
        module.kick(moderator, (FPlayer) fEntity, ban);

        module.builder(fEntity)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getBan().getDestination())
                .format(module.buildFormat(ban))
                .sound(module.getSound())
                .sendBuilt();
    }

    private void handleBroadcastCommand(DataInputStream input, FEntity fEntity) throws IOException {
        BroadcastModule broadcastModule = injector.getInstance(BroadcastModule.class);
        if (broadcastModule.isModuleDisabledFor(fEntity)) return;

        String message = input.readUTF();

        broadcastModule.builder(fEntity)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getBroadcast().getDestination())
                .format(Localization.Command.Broadcast::getFormat)
                .message(message)
                .sound(broadcastModule.getSound())
                .sendBuilt();
    }

    private void handleChatColorCommand(FEntity fEntity) {
        fPlayerService.loadColors(fPlayerService.getFPlayer(fEntity.getUuid()));
    }

    private void handleChatSettingCommand(FEntity fEntity) {
        fPlayerService.loadSettings(fPlayerService.getFPlayer(fEntity.getUuid()));
    }

    private void handleCoinCommand(DataInputStream input, FEntity fEntity) throws IOException {
        CoinModule coinModule = injector.getInstance(CoinModule.class);
        if (coinModule.isModuleDisabledFor(fEntity)) return;

        int percent = input.readInt();

        coinModule.builder(fEntity)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getCoin().getDestination())
                .format(coinModule.replaceResult(percent))
                .sound(coinModule.getSound())
                .sendBuilt();
    }

    private void handleDeleteCommand(DataInputStream input, FEntity fEntity) throws IOException {
        // skip delete command checking, only format.moderation.delete module
        DeleteModule deleteModule = injector.getInstance(DeleteModule.class);

        UUID messageUUID = UUID.fromString(input.readUTF());
        deleteModule.remove(fEntity, messageUUID);
    }

    private void handleDiceCommand(DataInputStream input, FEntity fEntity) throws IOException {
        DiceModule diceModule = injector.getInstance(DiceModule.class);
        if (diceModule.isModuleDisabledFor(fEntity)) return;

        List<Integer> cubes = gson.fromJson(input.readUTF(), new TypeToken<List<Integer>>() {}.getType());

        diceModule.builder(fEntity)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getDice().getDestination())
                .format(diceModule.replaceResult(cubes))
                .sound(diceModule.getSound())
                .sendBuilt();
    }

    private void handleDoCommand(DataInputStream input, FEntity fEntity) throws IOException {
        DoModule doModule = injector.getInstance(DoModule.class);
        if (doModule.isModuleDisabledFor(fEntity)) return;

        String message = input.readUTF();

        doModule.builder(fEntity)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getDo().getDestination())
                .format(Localization.Command.Do::getFormat)
                .message(message)
                .sound(doModule.getSound())
                .sendBuilt();
    }

    private void handleHelperCommand(DataInputStream input, FEntity fEntity) throws IOException {
        HelperModule helperModule = injector.getInstance(HelperModule.class);
        if (helperModule.isModuleDisabledFor(fEntity)) return;

        String message = input.readUTF();

        helperModule.builder(fEntity)
                .destination(fileResolver.getCommand().getHelper().getDestination())
                .filter(helperModule.getFilterSee())
                .format(Localization.Command.Helper::getGlobal)
                .message(message)
                .sound(helperModule.getSound())
                .sendBuilt();
    }

    private void handleMuteCommand(DataInputStream input, FEntity fEntity) throws IOException {
        MuteModule muteModule = injector.getInstance(MuteModule.class);

        FPlayer fModerator = gson.fromJson(input.readUTF(), FPlayer.class);
        if (muteModule.isModuleDisabledFor(fModerator)) return;

        Moderation mute = gson.fromJson(input.readUTF(), Moderation.class);

        muteModule.builder(fEntity)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getMute().getDestination())
                .format(muteModule.buildFormat(mute))
                .sound(muteModule.getSound())
                .sendBuilt();

        muteModule.sendForTarget(fModerator, (FPlayer) fEntity, mute);
    }

    private void handleUnbanCommand(DataInputStream input, FEntity fEntity) throws IOException {
        UnbanModule unbanModule = injector.getInstance(UnbanModule.class);

        FPlayer fPlayer = gson.fromJson(input.readUTF(), FPlayer.class);
        if (unbanModule.isModuleDisabledFor(fPlayer)) return;

        unbanModule.builder(fEntity)
                .destination(fileResolver.getCommand().getUnban().getDestination())
                .range(Range.get(Range.Type.SERVER))
                .filter(filter -> filter.isSetting(FPlayer.Setting.BAN))
                .format(unwarn -> unwarn.getFormat().replace("<moderator>", fPlayer.getName()))
                .sound(unbanModule.getSound())
                .sendBuilt();
    }

    private void handleUnmuteCommand(DataInputStream input, FEntity fEntity) throws IOException {
        UnmuteModule unmuteModule = injector.getInstance(UnmuteModule.class);

        FPlayer fPlayer = gson.fromJson(input.readUTF(), FPlayer.class);
        if (unmuteModule.isModuleDisabledFor(fPlayer)) return;

        unmuteModule.builder(fEntity)
                .destination(fileResolver.getCommand().getUnmute().getDestination())
                .range(Range.get(Range.Type.SERVER))
                .filter(filter -> filter.isSetting(FPlayer.Setting.MUTE))
                .format(unwarn -> unwarn.getFormat().replace("<moderator>", fPlayer.getName()))
                .sound(unmuteModule.getSound())
                .sendBuilt();
    }

    private void handleUnwarnCommand(DataInputStream input, FEntity fEntity) throws IOException {
        UnwarnModule unwarnModule = injector.getInstance(UnwarnModule.class);

        FPlayer fPlayer = gson.fromJson(input.readUTF(), FPlayer.class);
        if (unwarnModule.isModuleDisabledFor(fPlayer)) return;

        unwarnModule.builder(fEntity)
                .destination(fileResolver.getCommand().getUnwarn().getDestination())
                .range(Range.get(Range.Type.SERVER))
                .filter(filter -> filter.isSetting(FPlayer.Setting.WARN))
                .format(unwarn -> unwarn.getFormat().replace("<moderator>", fPlayer.getName()))
                .sound(unwarnModule.getSound())
                .sendBuilt();
    }

    private void handlePollVote(DataInputStream input, FEntity fEntity) throws IOException {
        injector.getInstance(PollModule.class).vote(fEntity, input.readInt(), input.readInt());
    }

    private void handlePollCreate(DataInputStream input, FEntity fEntity) throws IOException {
        PollModule pollModule = injector.getInstance(PollModule.class);
        if (pollModule.isModuleDisabledFor(fEntity)) return;

        Poll poll = gson.fromJson(input.readUTF(), Poll.class);
        pollModule.saveAndUpdateLast(poll);

        pollModule.builder(fEntity)
                .range(Range.get(Range.Type.SERVER))
                .format(pollModule.resolvePollFormat(fEntity, poll, PollModule.Status.START))
                .message(poll.getTitle())
                .sound(pollModule.getSound())
                .sendBuilt();
    }

    private void handleSpyCommand(DataInputStream input, FEntity fEntity) throws IOException {
        String action = input.readUTF();
        String string = input.readUTF();

        SpyModule spyModule = injector.getInstance(SpyModule.class);
        if (!spyModule.isEnable()) return;

        spyModule.builder(fEntity)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getSpy().getDestination())
                .filter(fReceiver -> !fEntity.equals(fReceiver)
                        && !spyModule.isModuleDisabledFor(fReceiver)
                        && fReceiver.isSetting(FPlayer.Setting.SPY)
                        && fReceiver.isOnline()
                )
                .format(spyModule.replaceAction(action))
                .message(string)
                .sendBuilt();
    }

    private void handleStreamCommand(DataInputStream input, FEntity fEntity) throws IOException {
        StreamModule streamModule = injector.getInstance(StreamModule.class);
        if (streamModule.isModuleDisabledFor(fEntity)) return;

        String message = input.readUTF();
        streamModule.builder(fEntity)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getStream().getDestination())
                .format(streamModule.replaceUrls(message))
                .sendBuilt();
    }

    private void handleTellCommand(DataInputStream input, FEntity fEntity) throws IOException {
        TellModule tellModule = injector.getInstance(TellModule.class);
        if (tellModule.isModuleDisabledFor(fEntity)) return;

        UUID receiverUUID = UUID.fromString(input.readUTF());
        String message = input.readUTF();

        FPlayer fReceiver = fPlayerService.getFPlayer(receiverUUID);
        if (fReceiver.isUnknown()) return;

        IntegrationModule integrationModule = injector.getInstance(IntegrationModule.class);
        if (!integrationModule.isVanishedVisible(fReceiver, fEntity)) return;

        tellModule.send(fEntity, fReceiver, (fResolver, s) -> s.getReceiver(), message);
    }

    private void handleTranslateToCommand(DataInputStream input, FEntity fEntity) throws IOException {
        TranslatetoModule translatetoModule = injector.getInstance(TranslatetoModule.class);
        if (translatetoModule.isModuleDisabledFor(fEntity)) return;

        String targetLang = input.readUTF();
        String message = input.readUTF();

        translatetoModule.builder(fEntity)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getTranslateto().getDestination())
                .format(translatetoModule.replaceLanguage(targetLang))
                .message(message)
                .sound(translatetoModule.getSound())
                .sendBuilt();
    }

    private void handleTryCommand(DataInputStream input, FEntity fEntity) throws IOException {
        TryModule tryModule = injector.getInstance(TryModule.class);
        if (tryModule.isModuleDisabledFor(fEntity)) return;

        int value = input.readInt();
        String message = input.readUTF();

        tryModule.builder(fEntity)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getTry().getDestination())
                .tag(MessageType.COMMAND_TRY)
                .format(tryModule.replacePercent(value))
                .message(message)
                .sound(tryModule.getSound())
                .sendBuilt();
    }

    private void handleWarnCommand(DataInputStream input, FEntity fEntity) throws IOException {
        WarnModule warnModule = injector.getInstance(WarnModule.class);

        FPlayer fModerator = gson.fromJson(input.readUTF(), FPlayer.class);
        if (warnModule.isModuleDisabledFor(fModerator)) return;

        Moderation warn = gson.fromJson(input.readUTF(), Moderation.class);

        warnModule.builder(fEntity)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getWarn().getDestination())
                .format(warnModule.buildFormat(warn))
                .sound(warnModule.getSound())
                .sendBuilt();

        warnModule.send(fModerator, (FPlayer) fEntity, warn);
    }

    private void handleKickCommand(DataInputStream input, FEntity fEntity) throws IOException {
        KickModule kickModule = injector.getInstance(KickModule.class);

        FPlayer fModerator = gson.fromJson(input.readUTF(), FPlayer.class);
        if (kickModule.isModuleDisabledFor(fModerator)) return;

        Moderation kick = gson.fromJson(input.readUTF(), Moderation.class);

        kickModule.builder(fEntity)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getKick().getDestination())
                .format(kickModule.buildFormat(kick))
                .sound(kickModule.getSound())
                .sendBuilt();

        kickModule.kick(fModerator, (FPlayer) fEntity, kick);
    }

    private void handleTicTacToeCreate(DataInputStream input, FEntity fEntity) throws IOException {
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

    private void handleTicTacToeMove(DataInputStream input, FEntity fEntity) throws IOException {
        if (!(fEntity instanceof FPlayer fPlayer)) return;

        FPlayer fReceiver = gson.fromJson(input.readUTF(), FPlayer.class);
        TicTacToe ticTacToe = injector.getInstance(TictactoeManager.class).fromString(input.readUTF());
        int typeTitle = input.readInt();
        String move = input.readUTF();

        injector.getInstance(TictactoeModule.class).sendMoveMessage(fPlayer, fReceiver, ticTacToe, typeTitle, move);
    }

    private void handleChatMessage(DataInputStream input, FEntity fEntity) throws IOException {
        String chat = input.readUTF();
        String message = input.readUTF();

        injector.getInstance(ChatModule.class).send(fEntity, chat, message);
    }

    private void handleRockPaperScissorsCreate(DataInputStream input, FEntity fEntity) throws IOException {
        UUID id = UUID.fromString(input.readUTF());
        UUID receiver = UUID.fromString(input.readUTF());
        injector.getInstance(RockpaperscissorsModule.class).create(id, fEntity, receiver);
    }

    private void handleRockPaperScissorsMove(DataInputStream input, FEntity fEntity) throws IOException {
        UUID id = UUID.fromString(input.readUTF());
        String move = input.readUTF();

        injector.getInstance(RockpaperscissorsModule.class).move(id, fEntity, move);
    }

    private void handleRockPaperScissorsFinal(DataInputStream input, FEntity fEntity) throws IOException {
        if (!(fEntity instanceof FPlayer fPlayer)) return;

        UUID id = UUID.fromString(input.readUTF());
        String move = input.readUTF();

        injector.getInstance(RockpaperscissorsModule.class).sendFinalMessage(id, fPlayer, move);
    }

    private void handleDiscordMessage(DataInputStream input, FEntity fEntity) throws IOException {
        String nickname = input.readUTF();
        String string = input.readUTF();

        MessageCreateListener messageCreateListener = injector.getInstance(MessageCreateListener.class);
        messageCreateListener.builder(fEntity)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getIntegration().getDiscord().getDestination())
                .tag(MessageType.FROM_DISCORD_TO_MINECRAFT)
                .format(s -> s.getForMinecraft().replace("<name>", nickname))
                .message(string)
                .sound(messageCreateListener.getSound())
                .sendBuilt();
    }

    private void handleTwitchMessage(DataInputStream input, FEntity fEntity) throws IOException {
        String nickname = input.readUTF();
        String channelName = input.readUTF();
        String string = input.readUTF();

        ChannelMessageListener channelMessageListener = injector.getInstance(ChannelMessageListener.class);
        channelMessageListener.builder(fEntity)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getIntegration().getTwitch().getDestination())
                .tag(MessageType.FROM_TWITCH_TO_MINECRAFT)
                .format(s -> s.getForMinecraft()
                        .replace("<name>", nickname)
                        .replace("<channel>", channelName)
                )
                .message(string)
                .sound(channelMessageListener.getSound())
                .sendBuilt();
    }

    private void handleTelegramMessage(DataInputStream input, FEntity fEntity) throws IOException {
        String author = input.readUTF();
        String chat = input.readUTF();
        String text = input.readUTF();

        MessageListener messageListener = injector.getInstance(MessageListener.class);
        messageListener.builder(fEntity)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getIntegration().getTelegram().getDestination())
                .tag(MessageType.FROM_TELEGRAM_TO_MINECRAFT)
                .format(s -> s.getForMinecraft()
                        .replace("<name>", author)
                        .replace("<chat>", chat)
                )
                .message(text)
                .sound(messageListener.getSound())
                .sendBuilt();
    }

    private void handleAdvancement(DataInputStream input, FEntity fEntity) throws IOException {
        AdvancementModule advancementModule = injector.getInstance(AdvancementModule.class);
        if (advancementModule.isModuleDisabledFor(fEntity)) return;

        ChatAdvancement chatAdvancement = gson.fromJson(input.readUTF(), ChatAdvancement.class);

        advancementModule.builder(fEntity)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getMessage().getAdvancement().getDestination())
                .tag(MessageType.ADVANCEMENT)
                .format((fResolver, s) -> advancementModule.convert(s, chatAdvancement))
                .tagResolvers(fResolver -> new TagResolver[]{advancementModule.advancementTag(fEntity, fResolver, chatAdvancement)})
                .sound(advancementModule.getSound())
                .sendBuilt();
    }

    private void handleDeath(DataInputStream input, FEntity fEntity) throws IOException {
        DeathModule deathModule = injector.getInstance(DeathModule.class);
        if (deathModule.isModuleDisabledFor(fEntity)) return;

        Death death = gson.fromJson(input.readUTF(), Death.class);

        deathModule.builder(fEntity)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getMessage().getDeath().getDestination())
                .tag(MessageType.DEATH)
                .format((fResolver, s) -> s.getTypes().get(death.getKey()))
                .tagResolvers(fResolver -> new TagResolver[]{deathModule.killerTag(fResolver, death.getKiller()), deathModule.byItemTag(death.getItem())})
                .sound(deathModule.getSound())
                .sendBuilt();
    }

    private void handleJoin(DataInputStream input, FEntity fEntity) throws IOException {
        JoinModule joinModule = injector.getInstance(JoinModule.class);
        if (joinModule.isModuleDisabledFor(fEntity)) return;

        boolean hasPlayedBefore = input.readBoolean();

        Message.Join message = fileResolver.getMessage().getJoin();

        joinModule.builder(fEntity)
                .tag(MessageType.JOIN)
                .destination(message.getDestination())
                .range(Range.get(Range.Type.SERVER))
                .filter(fReceiver -> fReceiver.isSetting(FPlayer.Setting.JOIN))
                .format(s -> hasPlayedBefore || !message.isFirst() ? s.getFormat() : s.getFormatFirstTime())
                .sound(joinModule.getSound())
                .sendBuilt();
    }

    private void handleQuit(DataInputStream input, FEntity fEntity) throws IOException {
        QuitModule quitModule = injector.getInstance(QuitModule.class);
        if (quitModule.isModuleDisabledFor(fEntity)) return;

        quitModule.builder(fEntity)
                .tag(MessageType.QUIT)
                .destination(fileResolver.getMessage().getQuit().getDestination())
                .range(Range.get(Range.Type.SERVER))
                .filter(fReceiver -> fReceiver.isSetting(FPlayer.Setting.QUIT))
                .format(Localization.Message.Quit::getFormat)
                .sound(quitModule.getSound())
                .sendBuilt();
    }

    private void handleAfk(DataInputStream input, FEntity fEntity) throws IOException {
        AfkModule afkModule = injector.getInstance(AfkModule.class);
        if (afkModule.isModuleDisabledFor(fEntity)) return;

        boolean isAfk = input.readBoolean();

        afkModule.builder(fEntity)
                .tag(MessageType.AFK)
                .destination(fileResolver.getMessage().getAfk().getDestination())
                .filter(fReceiver -> fReceiver.isSetting(FPlayer.Setting.AFK))
                .format(s -> isAfk
                        ? s.getFormatFalse().getGlobal()
                        : s.getFormatTrue().getGlobal()
                )
                .sound(afkModule.getSound())
                .sendBuilt();
    }

}
