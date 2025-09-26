package net.flectone.pulse.platform.handler;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.leangen.geantyref.TypeToken;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.ModerationMetadata;
import net.flectone.pulse.model.event.UnModerationMetadata;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.command.anon.AnonModule;
import net.flectone.pulse.module.command.ball.BallModule;
import net.flectone.pulse.module.command.ball.model.BallMetadata;
import net.flectone.pulse.module.command.ban.BanModule;
import net.flectone.pulse.module.command.broadcast.BroadcastModule;
import net.flectone.pulse.module.command.chatcolor.ChatcolorModule;
import net.flectone.pulse.module.command.clearchat.ClearchatModule;
import net.flectone.pulse.module.command.coin.CoinModule;
import net.flectone.pulse.module.command.coin.model.CoinMetadata;
import net.flectone.pulse.module.command.dice.DiceModule;
import net.flectone.pulse.module.command.dice.model.DiceMetadata;
import net.flectone.pulse.module.command.do_.DoModule;
import net.flectone.pulse.module.command.helper.HelperModule;
import net.flectone.pulse.module.command.kick.KickModule;
import net.flectone.pulse.module.command.me.MeModule;
import net.flectone.pulse.module.command.mute.MuteModule;
import net.flectone.pulse.module.command.poll.PollModule;
import net.flectone.pulse.module.command.poll.model.Poll;
import net.flectone.pulse.module.command.poll.model.PollMetadata;
import net.flectone.pulse.module.command.rockpaperscissors.RockpaperscissorsModule;
import net.flectone.pulse.module.command.spy.SpyModule;
import net.flectone.pulse.module.command.spy.model.SpyMetadata;
import net.flectone.pulse.module.command.stream.StreamModule;
import net.flectone.pulse.module.command.stream.model.StreamMetadata;
import net.flectone.pulse.module.command.tell.TellModule;
import net.flectone.pulse.module.command.tictactoe.TictactoeModule;
import net.flectone.pulse.module.command.tictactoe.service.TictactoeService;
import net.flectone.pulse.module.command.tictactoe.model.TicTacToe;
import net.flectone.pulse.module.command.translateto.TranslatetoModule;
import net.flectone.pulse.module.command.translateto.model.TranslatetoMetadata;
import net.flectone.pulse.module.command.try_.TryModule;
import net.flectone.pulse.module.command.try_.model.TryMetadata;
import net.flectone.pulse.module.command.unban.UnbanModule;
import net.flectone.pulse.module.command.unmute.UnmuteModule;
import net.flectone.pulse.module.command.unwarn.UnwarnModule;
import net.flectone.pulse.module.command.warn.WarnModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.advancement.AdvancementModule;
import net.flectone.pulse.module.message.advancement.model.Advancement;
import net.flectone.pulse.module.message.advancement.model.AdvancementMetadata;
import net.flectone.pulse.module.message.afk.AfkModule;
import net.flectone.pulse.module.message.afk.model.AFKMetadata;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.module.message.chat.model.ChatMetadata;
import net.flectone.pulse.module.message.death.DeathModule;
import net.flectone.pulse.module.message.death.model.Death;
import net.flectone.pulse.module.message.death.model.DeathMetadata;
import net.flectone.pulse.module.message.format.moderation.delete.DeleteModule;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.module.message.join.model.JoinMetadata;
import net.flectone.pulse.module.message.quit.QuitModule;
import net.flectone.pulse.module.message.quit.model.QuitMetadata;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.Strings;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

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
        UUID metadataUUID = UUID.fromString(input.readUTF());
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
            case COMMAND_ANON -> handleAnonCommand(input, fEntity, metadataUUID);
            case COMMAND_ME -> handleMeCommand(input, fEntity, metadataUUID);
            case COMMAND_BALL -> handleBallCommand(input, fEntity, metadataUUID);
            case COMMAND_BAN -> handleBanCommand(input, fEntity, metadataUUID);
            case COMMAND_BROADCAST -> handleBroadcastCommand(input, fEntity, metadataUUID);
            case COMMAND_CHATCOLOR -> handleChatColorCommand(fEntity, metadataUUID);
            case COMMAND_CHATSETTING -> handleChatSettingCommand(fEntity);
            case COMMAND_COIN -> handleCoinCommand(input, fEntity, metadataUUID);
            case COMMAND_DELETE -> handleDeleteCommand(input, fEntity);
            case COMMAND_DICE -> handleDiceCommand(input, fEntity, metadataUUID);
            case COMMAND_DO -> handleDoCommand(input, fEntity, metadataUUID);
            case COMMAND_HELPER -> handleHelperCommand(input, fEntity, metadataUUID);
            case COMMAND_MUTE -> handleMuteCommand(input, fEntity, metadataUUID);
            case COMMAND_UNBAN -> handleUnbanCommand(input, fEntity, metadataUUID);
            case COMMAND_UNMUTE -> handleUnmuteCommand(input, fEntity, metadataUUID);
            case COMMAND_UNWARN -> handleUnwarnCommand(input, fEntity, metadataUUID);
            case COMMAND_POLL -> handlePollCommand(input, fEntity, metadataUUID);
            case COMMAND_SPY -> handleSpyCommand(input, fEntity, metadataUUID);
            case COMMAND_STREAM -> handleStreamCommand(input, fEntity, metadataUUID);
            case COMMAND_TELL -> handleTellCommand(input, fEntity, metadataUUID);
            case COMMAND_TRANSLATETO -> handleTranslateToCommand(input, fEntity, metadataUUID);
            case COMMAND_TRY -> handleTryCommand(input, fEntity, metadataUUID);
            case COMMAND_WARN -> handleWarnCommand(input, fEntity, metadataUUID);
            case COMMAND_KICK -> handleKickCommand(input, fEntity, metadataUUID);
            case COMMAND_TICTACTOE -> handleTicTacToeCommand(input, fEntity, metadataUUID);
            case CHAT -> handleChatMessage(input, fEntity, metadataUUID);
            case COMMAND_CLEARCHAT -> handleClearchatCommand(fEntity);
            case COMMAND_ROCKPAPERSCISSORS -> handleRockPaperScissors(input, fEntity, metadataUUID);
            case ADVANCEMENT -> handleAdvancement(input, fEntity, metadataUUID);
            case DEATH -> handleDeath(input, fEntity, metadataUUID);
            case JOIN -> handleJoin(input, fEntity, metadataUUID);
            case QUIT -> handleQuit(input, fEntity, metadataUUID);
            case AFK -> handleAfk(input, fEntity, metadataUUID);
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

    private void handleAnonCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        AnonModule module = injector.getInstance(AnonModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        String message = input.readUTF();

        module.sendMessage(module.metadataBuilder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(Localization.Command.Anon::getFormat)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getAnon().getDestination())
                .sound(module.getModuleSound())
                .message(message)
                .build()
        );
    }

    private void handleMeCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        MeModule module = injector.getInstance(MeModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        String message = input.readUTF();

        module.sendMessage(module.metadataBuilder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(Localization.Command.Me::getFormat)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getMe().getDestination())
                .message(message)
                .sound(module.getModuleSound())
                .build()
        );
    }

    private void handleBallCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        BallModule module = injector.getInstance(BallModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        int answer = input.readInt();
        String message = input.readUTF();

        module.sendMessage(BallMetadata.<Localization.Command.Ball>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(module.replaceAnswer(answer))
                .answer(answer)
                .message(message)
                .destination(fileResolver.getCommand().getBall().getDestination())
                .range(Range.get(Range.Type.SERVER))
                .sound(module.getModuleSound())
                .build()
        );
    }

    private void handleBanCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        BanModule module = injector.getInstance(BanModule.class);

        Moderation ban = gson.fromJson(input.readUTF(), Moderation.class);

        FPlayer fModerator = fPlayerService.getFPlayer(ban.getModerator());
        if (module.isModuleDisabledFor(fModerator)) return;

        module.kick(fModerator, (FPlayer) fEntity, ban);

        module.sendMessage(ModerationMetadata.<Localization.Command.Ban>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(module.buildFormat(ban))
                .moderation(ban)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getBan().getDestination())
                .sound(module.getModuleSound())
                .build()
        );
    }

    private void handleBroadcastCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        BroadcastModule module = injector.getInstance(BroadcastModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        String message = input.readUTF();

        module.sendMessage(module.metadataBuilder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(Localization.Command.Broadcast::getFormat)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getBroadcast().getDestination())
                .message(message)
                .sound(module.getModuleSound())
                .build()
        );
    }

    private void handleChatColorCommand(FEntity fEntity, UUID metadataUUID) {
        FPlayer fPlayer = fPlayerService.getFPlayer(fEntity.getUuid());
        fPlayerService.loadColors(fPlayer);

        ChatcolorModule module = injector.getInstance(ChatcolorModule.class);
        if (!module.isEnable()) return;

        module.sendMessageWithUpdatedColors(fPlayer, metadataUUID);
    }

    private void handleChatSettingCommand(FEntity fEntity) {
        fPlayerService.loadSettings(fPlayerService.getFPlayer(fEntity.getUuid()));
    }

    private void handleCoinCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        CoinModule module = injector.getInstance(CoinModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        int percent = input.readInt();

        module.sendMessage(CoinMetadata.<Localization.Command.Coin>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(module.replaceResult(percent))
                .percent(percent)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getCoin().getDestination())
                .sound(module.getModuleSound())
                .build()
        );
    }

    private void handleDeleteCommand(DataInputStream input, FEntity fEntity) throws IOException {
        // skip delete command checking, only format.moderation.delete module
        DeleteModule module = injector.getInstance(DeleteModule.class);

        UUID metadataUUID = UUID.fromString(input.readUTF());
        module.remove(fEntity, metadataUUID);
    }

    private void handleDiceCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        DiceModule module = injector.getInstance(DiceModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        List<Integer> cubes = gson.fromJson(input.readUTF(), new TypeToken<List<Integer>>() {}.getType());

        module.sendMessage(DiceMetadata.<Localization.Command.Dice>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(dice -> module.replaceResult(cubes, dice.getSymbols(), dice.getFormat()))
                .cubes(cubes)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getDice().getDestination())
                .sound(module.getModuleSound())
                .build()
        );
    }

    private void handleDoCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        DoModule module = injector.getInstance(DoModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        String message = input.readUTF();

        module.sendMessage(module.metadataBuilder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(Localization.Command.Do::getFormat)
                .message(message)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getDo().getDestination())
                .sound(module.getModuleSound())
                .build()
        );
    }

    private void handleHelperCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        HelperModule module = injector.getInstance(HelperModule.class);
        if (!module.isEnable()) return;

        String message = input.readUTF();

        module.sendMessage(module.metadataBuilder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(Localization.Command.Helper::getGlobal)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getHelper().getDestination())
                .message(message)
                .sound(module.getModuleSound())
                .filter(module.getFilterSee())
                .build()
        );
    }

    private void handleMuteCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        MuteModule module = injector.getInstance(MuteModule.class);

        Moderation mute = gson.fromJson(input.readUTF(), Moderation.class);

        FPlayer fModerator = fPlayerService.getFPlayer(mute.getModerator());
        if (module.isModuleDisabledFor(fModerator)) return;

        module.sendMessage(ModerationMetadata.<Localization.Command.Mute>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(module.buildFormat(mute))
                .moderation(mute)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getMute().getDestination())
                .sound(module.getModuleSound())
                .build()
        );

        module.sendForTarget(fModerator, (FPlayer) fEntity, mute);
    }

    private void handleUnbanCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        UnbanModule module = injector.getInstance(UnbanModule.class);

        FPlayer fModerator = gson.fromJson(input.readUTF(), FPlayer.class);
        if (module.isModuleDisabledFor(fModerator)) return;

        List<Moderation> bans = gson.fromJson(input.readUTF(), new TypeToken<List<Moderation>>(){}.getType());

        module.sendMessage(UnModerationMetadata.<Localization.Command.Unban>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(unban -> Strings.CS.replace(unban.getFormat(), "<moderator>", fModerator.getName()))
                .moderator(fModerator)
                .moderations(bans)
                .destination(fileResolver.getCommand().getUnban().getDestination())
                .range(Range.get(Range.Type.SERVER))
                .sound(module.getModuleSound())
                .build()
        );
    }

    private void handleUnmuteCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        UnmuteModule module = injector.getInstance(UnmuteModule.class);

        FPlayer fModerator = gson.fromJson(input.readUTF(), FPlayer.class);
        if (module.isModuleDisabledFor(fModerator)) return;

        List<Moderation> mutes = gson.fromJson(input.readUTF(), new TypeToken<List<Moderation>>(){}.getType());

        module.sendMessage(UnModerationMetadata.<Localization.Command.Unmute>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(unwarn -> Strings.CS.replace(unwarn.getFormat(), "<moderator>", fModerator.getName()))
                .moderator(fModerator)
                .moderations(mutes)
                .destination(fileResolver.getCommand().getUnmute().getDestination())
                .range(Range.get(Range.Type.SERVER))
                .sound(module.getModuleSound())
                .build()
        );
    }

    private void handleUnwarnCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        UnwarnModule module = injector.getInstance(UnwarnModule.class);

        FPlayer fModerator = gson.fromJson(input.readUTF(), FPlayer.class);
        if (module.isModuleDisabledFor(fModerator)) return;

        List<Moderation> warns = gson.fromJson(input.readUTF(), new TypeToken<List<Moderation>>(){}.getType());

        module.sendMessage(UnModerationMetadata.<Localization.Command.Unwarn>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(unwarn -> Strings.CS.replace(unwarn.getFormat(), "<moderator>", fModerator.getName()))
                .moderator(fModerator)
                .moderations(warns)
                .destination(fileResolver.getCommand().getUnwarn().getDestination())
                .range(Range.get(Range.Type.SERVER))
                .sound(module.getModuleSound())
                .build()
        );
    }

    private void handlePollCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        PollModule.Action action = PollModule.Action.valueOf(input.readUTF());
        switch (action) {
            case CREATE -> {
                PollModule module = injector.getInstance(PollModule.class);
                if (module.isModuleDisabledFor(fEntity)) return;

                Poll poll = gson.fromJson(input.readUTF(), Poll.class);
                module.saveAndUpdateLast(poll);

                module.sendMessage(PollMetadata.<Localization.Command.Poll>builder()
                        .uuid(metadataUUID)
                        .sender(fEntity)
                        .format(module.resolvePollFormat(fEntity, poll, PollModule.Status.START))
                        .poll(poll)
                        .range(Range.get(Range.Type.SERVER))
                        .message(poll.getTitle())
                        .sound(module.getModuleSound())
                        .build()
                );
            }
            case VOTE -> injector.getInstance(PollModule.class).vote(fEntity, input.readInt(), input.readInt(), metadataUUID);
        }
    }

    private void handleSpyCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        SpyModule module = injector.getInstance(SpyModule.class);
        if (!module.isEnable()) return;

        String action = input.readUTF();
        String string = input.readUTF();

        module.sendMessage(SpyMetadata.<Localization.Command.Spy>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(module.replaceAction(action))
                .turned(true)
                .action(action)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getSpy().getDestination())
                .message(string)
                .filter(module.createFilter(fEntity instanceof FPlayer fPlayer ? fPlayer : FPlayer.UNKNOWN))
                .build()
        );
    }

    private void handleStreamCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        StreamModule module = injector.getInstance(StreamModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        String message = input.readUTF();

        module.sendMessage(StreamMetadata.<Localization.Command.Stream>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(module.replaceUrls(message))
                .turned(true)
                .urls(message)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getStream().getDestination())
                .sound(module.getModuleSound())
                .build()
        );
    }

    private void handleTellCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        TellModule module = injector.getInstance(TellModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        UUID receiverUUID = UUID.fromString(input.readUTF());
        String message = input.readUTF();

        FPlayer fReceiver = fPlayerService.getFPlayer(receiverUUID);
        if (fReceiver.isUnknown()) return;

        IntegrationModule integrationModule = injector.getInstance(IntegrationModule.class);
        if (!integrationModule.canSeeVanished(fReceiver, fEntity)) return;

        module.send(fEntity, fReceiver, fReceiver, Localization.Command.Tell::getReceiver, message, metadataUUID);
    }

    private void handleTranslateToCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        TranslatetoModule module = injector.getInstance(TranslatetoModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        String targetLang = input.readUTF();
        String message = input.readUTF();
        String messageToTranslate = input.readUTF();

        module.sendMessage(TranslatetoMetadata.<Localization.Command.Translateto>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(module.replaceLanguage(targetLang))
                .targetLanguage(targetLang)
                .messageToTranslate(messageToTranslate)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getTranslateto().getDestination())
                .message(message)
                .sound(module.getModuleSound())
                .build()
        );
    }

    private void handleTryCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        TryModule module = injector.getInstance(TryModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        int value = input.readInt();
        String message = input.readUTF();

        module.sendMessage(TryMetadata.<Localization.Command.Try>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(module.replacePercent(value))
                .percent(value)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getTry().getDestination())
                .message(message)
                .sound(module.getModuleSound())
                .build()
        );
    }

    private void handleWarnCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        WarnModule module = injector.getInstance(WarnModule.class);

        Moderation warn = gson.fromJson(input.readUTF(), Moderation.class);

        FPlayer fModerator = fPlayerService.getFPlayer(warn.getModerator());
        if (module.isModuleDisabledFor(fModerator)) return;

        module.sendMessage(ModerationMetadata.<Localization.Command.Warn>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(module.buildFormat(warn))
                .moderation(warn)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getCommand().getWarn().getDestination())
                .sound(module.getModuleSound())
                .build()
        );

        module.send(fModerator, (FPlayer) fEntity, warn);
    }

    private void handleKickCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        KickModule module = injector.getInstance(KickModule.class);

        Moderation kick = gson.fromJson(input.readUTF(), Moderation.class);

        FPlayer fModerator = fPlayerService.getFPlayer(kick.getModerator());
        if (module.isModuleDisabledFor(fModerator)) return;

        module.sendMessage(ModerationMetadata.<Localization.Command.Kick>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(module.buildFormat(kick))
                .moderation(kick)
                .destination(fileResolver.getCommand().getKick().getDestination())
                .range(Range.get(Range.Type.SERVER))
                .sound(module.getModuleSound())
                .build()
        );

        module.kick(fModerator, (FPlayer) fEntity, kick);
    }

    private void handleTicTacToeCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        if (!(fEntity instanceof FPlayer fPlayer)) return;

        TictactoeModule.GamePhase gamePhase = TictactoeModule.GamePhase.valueOf(input.readUTF());
        switch (gamePhase) {
            case CREATE -> {
                FPlayer fReceiver = gson.fromJson(input.readUTF(), FPlayer.class);
                int ticTacToeId = input.readInt();
                boolean isHard = input.readBoolean();

                TictactoeService tictactoeService = injector.getInstance(TictactoeService.class);

                TicTacToe ticTacToe = tictactoeService.get(ticTacToeId);
                if (tictactoeService.get(ticTacToeId) == null) {
                    ticTacToe = tictactoeService.create(ticTacToeId, fPlayer, fReceiver, isHard);
                }

                injector.getInstance(TictactoeModule.class).sendCreateMessage(fPlayer, fReceiver, ticTacToe, metadataUUID);
            }
            case MOVE -> {
                FPlayer fReceiver = gson.fromJson(input.readUTF(), FPlayer.class);
                TicTacToe ticTacToe = injector.getInstance(TictactoeService.class).fromString(input.readUTF());
                int typeTitle = input.readInt();
                String move = input.readUTF();

                injector.getInstance(TictactoeModule.class).sendMoveMessage(fPlayer, fReceiver, ticTacToe, typeTitle, move, metadataUUID);
            }
        }
    }

    private void handleChatMessage(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        String proxyChatName = input.readUTF();
        String message = input.readUTF();

        FPlayer fPlayer = fPlayerService.getFPlayer(fEntity.getUuid());

        ChatModule chatModule = injector.getInstance(ChatModule.class);
        if (chatModule.isModuleDisabledFor(fPlayer)) return;

        Optional<Map.Entry<String, Message.Chat.Type>> optionalChat = fileResolver.getMessage().getChat().getTypes()
                .entrySet()
                .stream()
                .filter(chat -> chat.getKey().equals(proxyChatName))
                .findAny();

        if (optionalChat.isEmpty()) return;

        String chatName = optionalChat.get().getKey();
        Message.Chat.Type chatType = optionalChat.get().getValue();

        chatModule.sendMessage(ChatMetadata.<Localization.Message.Chat>builder()
                .uuid(metadataUUID)
                .sender(fPlayer)
                .format(s -> s.getTypes().get(chatName))
                .chatName(chatName)
                .chatType(chatType)
                .range(Range.get(Range.Type.SERVER))
                .destination(chatType.getDestination())
                .message(message)
                .sound(chatModule.getModuleSound())
                .filter(chatModule.permissionFilter(chatName))
                .build()
        );
    }

    private void handleClearchatCommand(FEntity fEntity) {
        ClearchatModule module = injector.getInstance(ClearchatModule.class);
        if (!module.isEnable()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(fEntity.getUuid());
        module.clearChat(fPlayer, false);
    }

    private void handleRockPaperScissors(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        RockpaperscissorsModule.GamePhase gamePhase = RockpaperscissorsModule.GamePhase.valueOf(input.readUTF());
        switch (gamePhase) {
            case CREATE -> {
                UUID id = UUID.fromString(input.readUTF());
                UUID receiver = UUID.fromString(input.readUTF());
                injector.getInstance(RockpaperscissorsModule.class).create(id, fEntity, receiver);
            }
            case MOVE -> {
                UUID id = UUID.fromString(input.readUTF());
                String move = input.readUTF();

                injector.getInstance(RockpaperscissorsModule.class).move(id, fEntity, move, metadataUUID);
            }
            case END -> {
                if (!(fEntity instanceof FPlayer fPlayer)) return;

                UUID id = UUID.fromString(input.readUTF());
                String move = input.readUTF();

                injector.getInstance(RockpaperscissorsModule.class).end(id, fPlayer, move, metadataUUID);
            }
        }
    }

    private void handleAdvancement(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        AdvancementModule module = injector.getInstance(AdvancementModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        MinecraftTranslationKey translationKey = MinecraftTranslationKey.fromString(input.readUTF());
        Advancement advancement = gson.fromJson(input.readUTF(), Advancement.class);

        module.sendMessage(AdvancementMetadata.<Localization.Message.Advancement>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(localization -> switch (translationKey) {
                    case CHAT_TYPE_ADVANCEMENT_TASK -> localization.getFormatTask();
                    case CHAT_TYPE_ADVANCEMENT_GOAL -> localization.getFormatGoal();
                    case CHAT_TYPE_ADVANCEMENT_CHALLENGE -> localization.getFormatChallenge();
                    case CHAT_TYPE_ACHIEVEMENT_TAKEN -> localization.getFormatTaken();
                    default -> "";
                })
                .advancement(advancement)
                .translationKey(translationKey)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getMessage().getAdvancement().getDestination())
                .sound(module.getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{module.advancementTag(fEntity, fResolver, advancement.getAdvancementComponent())})
                .build()
        );
    }

    private void handleDeath(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        DeathModule module = injector.getInstance(DeathModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        MinecraftTranslationKey translationKey = MinecraftTranslationKey.fromString(input.readUTF());
        Death death = gson.fromJson(input.readUTF(), Death.class);

        module.sendMessage(DeathMetadata.<Localization.Message.Death>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(s -> s.getTypes().get(translationKey.toString()))
                .death(death)
                .translationKey(translationKey)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getMessage().getDeath().getDestination())
                .sound(module.getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{
                        module.targetTag(fResolver, death.getTarget()),
                        module.targetTag("killer", fResolver, death.getKiller()),
                        module.killerItemTag(death.getKillerItem())
                })
                .build()
        );
    }

    private void handleJoin(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        JoinModule module = injector.getInstance(JoinModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        boolean hasPlayedBefore = input.readBoolean();
        boolean ignoreVanish = input.readBoolean();

        Message.Join message = fileResolver.getMessage().getJoin();

        module.sendMessage(JoinMetadata.<Localization.Message.Join>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(s -> hasPlayedBefore || !message.isFirst() ? s.getFormat() : s.getFormatFirstTime())
                .ignoreVanish(ignoreVanish)
                .playedBefore(hasPlayedBefore)
                .destination(message.getDestination())
                .range(Range.get(Range.Type.SERVER))
                .sound(module.getModuleSound())
                .build()
        );
    }

    private void handleQuit(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        QuitModule module = injector.getInstance(QuitModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        boolean ignoreVanish = input.readBoolean();

        module.sendMessage(QuitMetadata.<Localization.Message.Quit>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(Localization.Message.Quit::getFormat)
                .ignoreVanish(ignoreVanish)
                .destination(fileResolver.getMessage().getQuit().getDestination())
                .range(Range.get(Range.Type.SERVER))
                .sound(module.getModuleSound())
                .build()
        );
    }

    private void handleAfk(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        AfkModule module = injector.getInstance(AfkModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        boolean isAfk = input.readBoolean();

        module.sendMessage(AFKMetadata.<Localization.Message.Afk>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(s -> isAfk
                        ? s.getFormatFalse().getGlobal()
                        : s.getFormatTrue().getGlobal()
                )
                .newStatus(isAfk)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileResolver.getMessage().getAfk().getDestination())
                .sound(module.getModuleSound())
                .build()
        );
    }

}
