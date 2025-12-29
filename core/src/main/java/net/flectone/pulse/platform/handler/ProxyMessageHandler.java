package net.flectone.pulse.platform.handler;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.leangen.geantyref.TypeToken;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.ModerationMetadata;
import net.flectone.pulse.model.event.UnModerationMetadata;
import net.flectone.pulse.model.util.Destination;
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
import net.flectone.pulse.module.command.emit.EmitModule;
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
import net.flectone.pulse.module.command.tictactoe.model.TicTacToe;
import net.flectone.pulse.module.command.tictactoe.service.TictactoeService;
import net.flectone.pulse.module.command.translateto.TranslatetoModule;
import net.flectone.pulse.module.command.translateto.model.TranslatetoMetadata;
import net.flectone.pulse.module.command.try_.TryModule;
import net.flectone.pulse.module.command.try_.model.TryMetadata;
import net.flectone.pulse.module.command.unban.UnbanModule;
import net.flectone.pulse.module.command.unmute.UnmuteModule;
import net.flectone.pulse.module.command.unwarn.UnwarnModule;
import net.flectone.pulse.module.command.warn.WarnModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.afk.AfkModule;
import net.flectone.pulse.module.message.afk.model.AFKMetadata;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.module.message.chat.model.Chat;
import net.flectone.pulse.module.message.chat.model.ChatMetadata;
import net.flectone.pulse.module.message.format.moderation.delete.DeleteModule;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.module.message.join.model.JoinMetadata;
import net.flectone.pulse.module.message.quit.QuitModule;
import net.flectone.pulse.module.message.quit.model.QuitMetadata;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModule;
import net.flectone.pulse.module.message.vanilla.VanillaModule;
import net.flectone.pulse.module.message.vanilla.extractor.Extractor;
import net.flectone.pulse.module.message.vanilla.model.ParsedComponent;
import net.flectone.pulse.module.message.vanilla.model.VanillaMetadata;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ProxyMessageHandler {

    private final Injector injector;
    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final FLogger fLogger;
    private final ModerationService moderationService;
    private final Gson gson;
    private final TaskScheduler taskScheduler;

    public void handleProxyMessage(byte[] bytes) {
        taskScheduler.runAsync(() -> {
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
        });
    }

    private void handleSystemOnline(DataInputStream input) throws IOException {
        UUID uuid = UUID.fromString(input.readUTF());

        fPlayerService.invalidateOffline(uuid);

        injector.getInstance(PlayerlistnameModule.class).add(uuid);
    }

    private void handleSystemOffline(DataInputStream input) throws IOException {
        UUID uuid = UUID.fromString(input.readUTF());

        fPlayerService.invalidateOnline(uuid);

        injector.getInstance(PlayerlistnameModule.class).remove(uuid);
    }

    private void handleTaggedMessage(DataInputStream input, MessageType tag) throws IOException {
        UUID metadataUUID = UUID.fromString(input.readUTF());
        Set<String> proxyClusters = gson.fromJson(input.readUTF(), new TypeToken<Set<String>>() {}.getType());

        Optional<FEntity> optionalFEntity = parseFEntity(readAsJsonObject(input));
        if (optionalFEntity.isEmpty()) return;

        FEntity fEntity = optionalFEntity.get();
        if (handleModerationInvalidation(tag, fEntity)) {
            return;
        }

        Set<String> configClusters = fileFacade.config().proxy().clusters();
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
            case COMMAND_EMIT -> handleEmitCommand(input, fEntity, metadataUUID);
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
            case JOIN -> handleJoin(input, fEntity, metadataUUID);
            case QUIT -> handleQuit(input, fEntity, metadataUUID);
            case AFK -> handleAfk(input, fEntity, metadataUUID);
            case VANILLA -> handleVanilla(input, fEntity, metadataUUID);
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

    private void handleAnonCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        AnonModule module = injector.getInstance(AnonModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        String message = input.readUTF();

        module.sendMessage(module.metadataBuilder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(Localization.Command.Anon::format)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileFacade.command().anon().destination())
                .sound(module.soundOrThrow())
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
                .format(Localization.Command.Me::format)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileFacade.command().me().destination())
                .message(message)
                .sound(module.soundOrThrow())
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
                .destination(fileFacade.command().ball().destination())
                .range(Range.get(Range.Type.SERVER))
                .sound(module.soundOrThrow())
                .build()
        );
    }

    private void handleBanCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        BanModule module = injector.getInstance(BanModule.class);

        Moderation ban = gson.fromJson(input.readUTF(), Moderation.class);

        FPlayer fModerator = fPlayerService.getFPlayer(ban.moderator());
        if (module.isModuleDisabledFor(fModerator)) return;

        module.kick(fModerator, (FPlayer) fEntity, ban);

        module.sendMessage(ModerationMetadata.<Localization.Command.Ban>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(module.buildFormat(ban))
                .moderation(ban)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileFacade.command().ban().destination())
                .sound(module.soundOrThrow())
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
                .format(Localization.Command.Broadcast::format)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileFacade.command().broadcast().destination())
                .message(message)
                .sound(module.soundOrThrow())
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
                .destination(fileFacade.command().coin().destination())
                .sound(module.soundOrThrow())
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
                .format(dice -> module.replaceResult(cubes, dice.symbols(), dice.format()))
                .cubes(cubes)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileFacade.command().dice().destination())
                .sound(module.soundOrThrow())
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
                .format(Localization.Command.CommandDo::format)
                .message(message)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileFacade.command().commandDo().destination())
                .sound(module.soundOrThrow())
                .build()
        );
    }

    private void handleEmitCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        EmitModule module = injector.getInstance(EmitModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        FPlayer fTarget = gson.fromJson(input.readUTF(), FPlayer.class);
        Destination destination = gson.fromJson(input.readUTF(), Destination.class);
        String message = input.readUTF();

        if (fTarget.isConsole()) {
            module.sendMessage(module.metadataBuilder()
                    .uuid(metadataUUID)
                    .sender(fEntity)
                    .range(Range.get(Range.Type.SERVER))
                    .format(Localization.Command.Emit::format)
                    .message(message)
                    .destination(destination)
                    .sound(module.soundOrThrow())
                    .build()
            );
        } else {
            module.sendMessage(module.metadataBuilder()
                    .uuid(metadataUUID)
                    .sender(fEntity)
                    .filterPlayer(fTarget)
                    .format(Localization.Command.Emit::format)
                    .message(message)
                    .destination(destination)
                    .sound(module.soundOrThrow())
                    .build()
            );
        }
    }

    private void handleHelperCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        HelperModule module = injector.getInstance(HelperModule.class);
        if (!module.isEnable()) return;

        String message = input.readUTF();

        module.sendMessage(module.metadataBuilder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(Localization.Command.Helper::global)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileFacade.command().helper().destination())
                .message(message)
                .sound(module.soundOrThrow())
                .filter(module.getFilterSee())
                .build()
        );
    }

    private void handleMuteCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        MuteModule module = injector.getInstance(MuteModule.class);

        Moderation mute = gson.fromJson(input.readUTF(), Moderation.class);

        FPlayer fModerator = fPlayerService.getFPlayer(mute.moderator());
        if (module.isModuleDisabledFor(fModerator)) return;

        module.sendMessage(ModerationMetadata.<Localization.Command.Mute>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(module.buildFormat(mute))
                .moderation(mute)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileFacade.command().mute().destination())
                .sound(module.soundOrThrow())
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
                .format(unban -> Strings.CS.replace(unban.format(), "<moderator>", fModerator.getName()))
                .moderator(fModerator)
                .moderations(bans)
                .destination(fileFacade.command().unban().destination())
                .range(Range.get(Range.Type.SERVER))
                .sound(module.soundOrThrow())
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
                .format(unwarn -> Strings.CS.replace(unwarn.format(), "<moderator>", fModerator.getName()))
                .moderator(fModerator)
                .moderations(mutes)
                .destination(fileFacade.command().unmute().destination())
                .range(Range.get(Range.Type.SERVER))
                .sound(module.soundOrThrow())
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
                .format(unwarn -> Strings.CS.replace(unwarn.format(), "<moderator>", fModerator.getName()))
                .moderator(fModerator)
                .moderations(warns)
                .destination(fileFacade.command().unwarn().destination())
                .range(Range.get(Range.Type.SERVER))
                .sound(module.soundOrThrow())
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
                        .sound(module.soundOrThrow())
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
                .destination(fileFacade.command().spy().destination())
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
                .destination(fileFacade.command().stream().destination())
                .sound(module.soundOrThrow())
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

        module.send(fEntity, fReceiver, fReceiver, Localization.Command.Tell::receiver, message, metadataUUID);
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
                .destination(fileFacade.command().translateto().destination())
                .message(message)
                .sound(module.soundOrThrow())
                .build()
        );
    }

    private void handleTryCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        TryModule module = injector.getInstance(TryModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        int value = input.readInt();
        String message = input.readUTF();

        module.sendMessage(TryMetadata.<Localization.Command.CommandTry>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(module.replacePercent(value))
                .percent(value)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileFacade.command().commandTry().destination())
                .message(message)
                .sound(module.soundOrThrow())
                .build()
        );
    }

    private void handleWarnCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        WarnModule module = injector.getInstance(WarnModule.class);

        Moderation warn = gson.fromJson(input.readUTF(), Moderation.class);

        FPlayer fModerator = fPlayerService.getFPlayer(warn.moderator());
        if (module.isModuleDisabledFor(fModerator)) return;

        module.sendMessage(ModerationMetadata.<Localization.Command.Warn>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(module.buildFormat(warn))
                .moderation(warn)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileFacade.command().warn().destination())
                .sound(module.soundOrThrow())
                .build()
        );

        module.send(fModerator, (FPlayer) fEntity, warn);
    }

    private void handleKickCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        KickModule module = injector.getInstance(KickModule.class);

        Moderation kick = gson.fromJson(input.readUTF(), Moderation.class);

        FPlayer fModerator = fPlayerService.getFPlayer(kick.moderator());
        if (module.isModuleDisabledFor(fModerator)) return;

        module.sendMessage(ModerationMetadata.<Localization.Command.Kick>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(module.buildFormat(kick))
                .moderation(kick)
                .destination(fileFacade.command().kick().destination())
                .range(Range.get(Range.Type.SERVER))
                .sound(module.soundOrThrow())
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

        Optional<Map.Entry<String, Message.Chat.Type>> optionalChat = fileFacade.message().chat().types()
                .entrySet()
                .stream()
                .filter(chat -> chat.getKey().equals(proxyChatName))
                .findAny();

        if (optionalChat.isEmpty()) return;
        String chatName = optionalChat.get().getKey();
        Message.Chat.Type chatType = optionalChat.get().getValue();

        Chat playerChat = new Chat(chatName, chatType, chatModule.permission().types().get(chatName));

        chatModule.sendMessage(ChatMetadata.<Localization.Message.Chat>builder()
                .uuid(metadataUUID)
                .sender(fPlayer)
                .format(s -> s.types().get(chatName))
                .chat(playerChat)
                .range(Range.get(Range.Type.SERVER))
                .destination(chatType.destination())
                .message(message)
                .sound(playerChat.sound())
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

    private void handleJoin(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        JoinModule module = injector.getInstance(JoinModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        boolean hasPlayedBefore = input.readBoolean();
        boolean ignoreVanish = input.readBoolean();

        Message.Join message = fileFacade.message().join();

        module.sendMessage(JoinMetadata.<Localization.Message.Join>builder()
                .uuid(metadataUUID)
                .sender(fEntity)
                .format(s -> hasPlayedBefore || !message.first() ? s.format() : s.formatFirstTime())
                .ignoreVanish(ignoreVanish)
                .playedBefore(hasPlayedBefore)
                .destination(message.destination())
                .range(Range.get(Range.Type.SERVER))
                .sound(module.soundOrThrow())
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
                .format(Localization.Message.Quit::format)
                .ignoreVanish(ignoreVanish)
                .destination(fileFacade.message().quit().destination())
                .range(Range.get(Range.Type.SERVER))
                .sound(module.soundOrThrow())
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
                        ? s.formatFalse().global()
                        : s.formatTrue().global()
                )
                .newStatus(isAfk)
                .range(Range.get(Range.Type.SERVER))
                .destination(fileFacade.message().afk().destination())
                .sound(module.soundOrThrow())
                .build()
        );
    }

    private void handleVanilla(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        VanillaModule module = injector.getInstance(VanillaModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        String translationKey = input.readUTF();
        Map<Integer, Object> arguments = parseVanillaArguments(readAsJsonObject(input));

        Message.Vanilla.VanillaMessage vanillaMessage = injector.getInstance(Extractor.class).getVanillaMessage(translationKey);

        ParsedComponent parsedComponent = new ParsedComponent(translationKey, vanillaMessage, arguments);

        String vanillaMessageName = vanillaMessage.name();

        module.sendMessage(VanillaMetadata.<Localization.Message.Vanilla>builder()
                .uuid(metadataUUID)
                .parsedComponent(parsedComponent)
                .sender(fEntity)
                .format(localization -> StringUtils.defaultString(localization.types().get(parsedComponent.translationKey())))
                .tagResolvers(fResolver -> new TagResolver[]{module.argumentTag(fResolver, parsedComponent)})
                .range(Range.get(Range.Type.SERVER))
                .filter(fResolver -> vanillaMessageName.isEmpty() || fResolver.isSetting(vanillaMessageName))
                .destination(parsedComponent.vanillaMessage().destination())
                .build()
        );
    }

    private Map<Integer, Object> parseVanillaArguments(JsonObject jsonObject) {
        Map<Integer, Object> result = new HashMap<>();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            Integer key = Integer.parseInt(entry.getKey());
            JsonObject argumentJson = entry.getValue().getAsJsonObject();

            Optional<FEntity> entity = parseFEntity(argumentJson);
            result.put(key, entity.isPresent() ? entity.get() : gson.fromJson(argumentJson, Component.class));
        }

        return result;
    }

    private JsonObject readAsJsonObject(DataInputStream input) throws IOException {
        return gson.fromJson(input.readUTF(), JsonObject.class);
    }

    private Optional<FEntity> parseFEntity(JsonObject jsonObject) {
        if (jsonObject.has("name") && jsonObject.has("uuid") && jsonObject.has("type")) {
            boolean isPlayer =  "PLAYER".equals(jsonObject.get("type").getAsString());
            return Optional.of(gson.fromJson(jsonObject, isPlayer ? FPlayer.class : FEntity.class));
        }

        return Optional.empty();
    }

}
