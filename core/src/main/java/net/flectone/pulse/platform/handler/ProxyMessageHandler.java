package net.flectone.pulse.platform.handler;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.leangen.geantyref.TypeToken;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.data.repository.CooldownRepository;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
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
import net.flectone.pulse.module.message.vanilla.VanillaModule;
import net.flectone.pulse.module.message.vanilla.extractor.ComponentExtractor;
import net.flectone.pulse.module.message.vanilla.model.ParsedComponent;
import net.flectone.pulse.module.message.vanilla.model.VanillaMetadata;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;

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
    private final CooldownRepository cooldownRepository;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;

    public void handleProxyMessage(byte[] bytes) {
        taskScheduler.runAsync(() -> {
            try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
                 DataInputStream input = new DataInputStream(byteStream)) {

                ModuleName tag = ModuleName.fromProxyString(input.readUTF());
                if (tag == null) return;

                UUID uuid = UUID.fromString(input.readUTF());

                switch (tag) {
                    case SYSTEM_ONLINE -> handleSystemOnline(uuid);
                    case SYSTEM_OFFLINE -> handleSystemOffline(uuid);
                    default -> handleProxyMessage(input, uuid, tag);
                }
            } catch (IOException e) {
                fLogger.warning(e);
            }
        });
    }

    public void handleSystemOnline(UUID uuid) throws IOException {
        fPlayerService.invalidateOffline(uuid);
    }

    public void handleSystemOffline(UUID uuid) throws IOException {
        fPlayerService.invalidateOnline(uuid);
    }

    public void handleProxyMessage(DataInputStream input, UUID metadataUUID, ModuleName tag) throws IOException {
        Set<String> proxyClusters = gson.fromJson(input.readUTF(), new TypeToken<Set<String>>() {}.getType());

        Optional<FEntity> optionalFEntity = parseFEntity(readAsJsonObject(input));
        if (optionalFEntity.isEmpty()) return;
        if (handleSystemCooldown(tag, input)) return;

        FEntity fEntity = optionalFEntity.get();
        if (handleModerationInvalidation(tag, fEntity)) {
            return;
        }

        Set<String> configClusters = fileFacade.config().proxy().clusters();
        if (!configClusters.isEmpty() && configClusters.stream().noneMatch(proxyClusters::contains)) {
            return;
        }

        handleModuleMessage(input, fEntity, metadataUUID, tag);
    }

    public void handleModuleMessage(DataInputStream input, FEntity fEntity, UUID metadataUUID, ModuleName tag) throws IOException {
        switch (tag) {
            case COMMAND_ANON -> handleAnonCommand(input, fEntity, metadataUUID);
            case COMMAND_ME -> handleMeCommand(input, fEntity, metadataUUID);
            case COMMAND_BALL -> handleBallCommand(input, fEntity, metadataUUID);
            case COMMAND_BAN -> handleBanCommand(input, fEntity, metadataUUID);
            case COMMAND_BROADCAST -> handleBroadcastCommand(input, fEntity, metadataUUID);
            case COMMAND_CHATCOLOR -> handleChatColorCommand(fEntity, metadataUUID);
            case COMMAND_CHATSETTING -> handleChatSettingCommand(fEntity);
            case COMMAND_COIN -> handleCoinCommand(input, fEntity, metadataUUID);
            case COMMAND_DELETEMESSAGE -> handleDeleteCommand(input, fEntity);
            case COMMAND_DICE -> handleDiceCommand(input, fEntity, metadataUUID);
            case COMMAND_DO -> handleDoCommand(input, fEntity, metadataUUID);
            case COMMAND_EMIT -> handleEmitCommand(input, fEntity, metadataUUID);
            case COMMAND_HELPER -> handleHelperCommand(input, fEntity, metadataUUID);
            case COMMAND_MUTE -> handleMuteCommand(input, fEntity, metadataUUID);
            case COMMAND_NICKNAME -> handleNicknameCommand(fEntity);
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
            case MESSAGE_CHAT -> handleChatMessage(input, fEntity, metadataUUID);
            case COMMAND_CLEARCHAT -> handleClearchatCommand(fEntity);
            case COMMAND_ROCKPAPERSCISSORS -> handleRockPaperScissors(input, fEntity, metadataUUID);
            case MESSAGE_JOIN -> handleJoin(input, fEntity, metadataUUID);
            case MESSAGE_QUIT -> handleQuit(input, fEntity, metadataUUID);
            case COMMAND_AFK -> handleAfk(input, fEntity, metadataUUID);
            case MESSAGE_VANILLA -> handleVanilla(input, fEntity, metadataUUID);
        }
    }

    private boolean handleModerationInvalidation(ModuleName tag, FEntity fEntity) {
        return switch (tag) {
            case SYSTEM_BAN -> {
                moderationService.invalidateBans(fEntity.uuid());
                yield true;
            }
            case SYSTEM_MUTE -> {
                moderationService.invalidateMutes(fEntity.uuid());
                yield true;
            }
            case SYSTEM_WARN -> {
                moderationService.invalidateWarns(fEntity.uuid());
                yield true;
            }
            default -> false;
        };
    }

    private boolean handleSystemCooldown(ModuleName tag, DataInputStream input) throws IOException {
        if (tag != ModuleName.SYSTEM_COOLDOWN) return false;

        UUID uuid = UUID.fromString(input.readUTF());
        String cooldownClass = input.readUTF();
        long newExpireTime = input.readLong();

        cooldownRepository.updateCache(uuid, cooldownClass, newExpireTime);
        return true;
    }

    private void handleAnonCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        AnonModule module = injector.getInstance(AnonModule.class);
        if (moduleController.isDisabledFor(module, fEntity)) return;

        String message = input.readUTF();

        messageDispatcher.dispatch(module, EventMetadata.<Localization.Command.Anon>builder()
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
        if (moduleController.isDisabledFor(module, fEntity)) return;

        String message = input.readUTF();

        messageDispatcher.dispatch(module, EventMetadata.<Localization.Command.Me>builder()
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
        if (moduleController.isDisabledFor(module, fEntity)) return;

        int answer = input.readInt();
        String message = input.readUTF();

        messageDispatcher.dispatch(module, BallMetadata.<Localization.Command.Ball>builder()
                .base(EventMetadata.<Localization.Command.Ball>builder()
                        .uuid(metadataUUID)
                        .sender(fEntity)
                        .format(module.replaceAnswer(answer))
                        .message(message)
                        .destination(fileFacade.command().ball().destination())
                        .range(Range.get(Range.Type.SERVER))
                        .sound(module.soundOrThrow())
                        .build()
                )
                .answer(answer)
                .build()
        );
    }

    private void handleBanCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        BanModule module = injector.getInstance(BanModule.class);

        Moderation ban = gson.fromJson(input.readUTF(), Moderation.class);

        FPlayer fModerator = fPlayerService.getFPlayer(ban.moderator());
        if (moduleController.isDisabledFor(module, fModerator)) return;

        ModerationMessageFormatter moderationMessageFormatter = injector.getInstance(ModerationMessageFormatter.class);

        module.kick(fModerator, (FPlayer) fEntity, ban);

        messageDispatcher.dispatch(module, ModerationMetadata.<Localization.Command.Ban>builder()
                .base(EventMetadata.<Localization.Command.Ban>builder()
                        .uuid(metadataUUID)
                        .sender(fEntity)
                        .format((fReceiver, message) ->
                                moderationMessageFormatter.replacePlaceholders(message.server(), fReceiver, ban)
                        )
                        .range(Range.get(Range.Type.SERVER))
                        .destination(fileFacade.command().ban().destination())
                        .sound(module.soundOrThrow())
                        .tagResolvers(fResolver -> new TagResolver[]{messagePipeline.targetTag("moderator", fResolver, fModerator)})
                        .build()
                )
                .moderation(ban)
                .build()
        );
    }

    private void handleBroadcastCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        BroadcastModule module = injector.getInstance(BroadcastModule.class);
        if (moduleController.isDisabledFor(module, fEntity)) return;

        String message = input.readUTF();

        messageDispatcher.dispatch(module, EventMetadata.<Localization.Command.Broadcast>builder()
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
        FPlayer fPlayer = fPlayerService.updateCache(fPlayerService.loadColors(fPlayerService.getFPlayer(fEntity)));

        ChatcolorModule module = injector.getInstance(ChatcolorModule.class);
        if (!moduleController.isEnable(module)) return;

        module.sendMessageWithUpdatedColors(fPlayer, metadataUUID);
    }

    private void handleChatSettingCommand(FEntity fEntity) {
        fPlayerService.updateCache(fPlayerService.loadSettings(fPlayerService.getFPlayer(fEntity)));
    }

    private void handleCoinCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        CoinModule module = injector.getInstance(CoinModule.class);
        if (moduleController.isDisabledFor(module, fEntity)) return;

        int percent = input.readInt();

        messageDispatcher.dispatch(module, CoinMetadata.<Localization.Command.Coin>builder()
                .base(EventMetadata.<Localization.Command.Coin>builder()
                        .uuid(metadataUUID)
                        .sender(fEntity)
                        .format(module.replaceResult(percent))
                        .range(Range.get(Range.Type.SERVER))
                        .destination(fileFacade.command().coin().destination())
                        .sound(module.soundOrThrow())
                        .build()
                )
                .percent(percent)
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
        if (moduleController.isDisabledFor(module, fEntity)) return;

        List<Integer> cubes = gson.fromJson(input.readUTF(), new TypeToken<List<Integer>>() {}.getType());

        messageDispatcher.dispatch(module, DiceMetadata.<Localization.Command.Dice>builder()
                .base(EventMetadata.<Localization.Command.Dice>builder()
                        .uuid(metadataUUID)
                        .sender(fEntity)
                        .format(dice -> module.replaceResult(cubes, dice.symbols(), dice.format()))
                        .range(Range.get(Range.Type.SERVER))
                        .destination(fileFacade.command().dice().destination())
                        .sound(module.soundOrThrow())
                        .build()
                )
                .cubes(cubes)
                .build()
        );
    }

    private void handleDoCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        DoModule module = injector.getInstance(DoModule.class);
        if (moduleController.isDisabledFor(module, fEntity)) return;

        String message = input.readUTF();

        messageDispatcher.dispatch(module, EventMetadata.<Localization.Command.CommandDo>builder()
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
        if (moduleController.isDisabledFor(module, fEntity)) return;

        FPlayer fTarget = gson.fromJson(input.readUTF(), FPlayer.class);

        Map<String, Object> destinationMap = gson.fromJson(input.readUTF(), new TypeToken<Map<String, Object>>(){}.getType());
        Destination destination = Destination.fromJson(destinationMap);
        String message = input.readUTF();

        if (fTarget.isConsole()) {
            messageDispatcher.dispatch(module, EventMetadata.<Localization.Command.Emit>builder()
                    .uuid(metadataUUID)
                    .sender(fEntity)
                    .flag(MessageFlag.PLACEHOLDER_CONTEXT_SENDER, false)
                    .range(Range.get(Range.Type.SERVER))
                    .format(Localization.Command.Emit::format)
                    .message(message)
                    .destination(destination)
                    .sound(module.soundOrThrow())
                    .build()
            );
        } else {
            messageDispatcher.dispatch(module, EventMetadata.<Localization.Command.Emit>builder()
                    .uuid(metadataUUID)
                    .sender(fEntity)
                    .filterPlayer(fTarget)
                    .flag(MessageFlag.PLACEHOLDER_CONTEXT_SENDER, false)
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
        if (!moduleController.isEnable(module)) return;

        String message = input.readUTF();

        messageDispatcher.dispatch(module, EventMetadata.<Localization.Command.Helper>builder()
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
        if (moduleController.isDisabledFor(module, fModerator)) return;

        ModerationMessageFormatter moderationMessageFormatter = injector.getInstance(ModerationMessageFormatter.class);

        messageDispatcher.dispatch(module, ModerationMetadata.<Localization.Command.Mute>builder()
                .base(EventMetadata.<Localization.Command.Mute>builder()
                        .uuid(metadataUUID)
                        .sender(fEntity)
                        .format((fReceiver, localization) ->
                                moderationMessageFormatter.replacePlaceholders(localization.server(), fReceiver, mute)
                        )
                        .range(Range.get(Range.Type.SERVER))
                        .destination(fileFacade.command().mute().destination())
                        .sound(module.soundOrThrow())
                        .tagResolvers(fResolver -> new TagResolver[]{messagePipeline.targetTag("moderator", fResolver, fModerator)})
                        .build()
                )
                .moderation(mute)
                .build()
        );

        module.sendForTarget(fModerator, (FPlayer) fEntity, mute);
    }

    private void handleNicknameCommand(FEntity fEntity) {
        fPlayerService.updateCache(fPlayerService.loadSettings(fPlayerService.getFPlayer(fEntity)));
    }

    private void handleUnbanCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        UnbanModule module = injector.getInstance(UnbanModule.class);

        FPlayer fModerator = gson.fromJson(input.readUTF(), FPlayer.class);
        if (moduleController.isDisabledFor(module, fModerator)) return;

        List<Moderation> bans = gson.fromJson(input.readUTF(), new TypeToken<List<Moderation>>(){}.getType());

        messageDispatcher.dispatch(module, UnModerationMetadata.<Localization.Command.Unban>builder()
                .base(EventMetadata.<Localization.Command.Unban>builder()
                        .uuid(metadataUUID)
                        .sender(fEntity)
                        .format(Localization.Command.Unban::format)
                        .destination(fileFacade.command().unban().destination())
                        .range(Range.get(Range.Type.SERVER))
                        .sound(module.soundOrThrow())
                        .tagResolvers(fResolver -> new TagResolver[]{messagePipeline.targetTag("moderator", fResolver, fModerator)})
                        .build()
                )
                .moderator(fModerator)
                .moderations(bans)
                .build()
        );
    }

    private void handleUnmuteCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        UnmuteModule module = injector.getInstance(UnmuteModule.class);

        FPlayer fModerator = gson.fromJson(input.readUTF(), FPlayer.class);
        if (moduleController.isDisabledFor(module, fModerator)) return;

        List<Moderation> mutes = gson.fromJson(input.readUTF(), new TypeToken<List<Moderation>>(){}.getType());

        messageDispatcher.dispatch(module, UnModerationMetadata.<Localization.Command.Unmute>builder()
                .base(EventMetadata.<Localization.Command.Unmute>builder()
                        .uuid(metadataUUID)
                        .sender(fEntity)
                        .format(Localization.Command.Unmute::format)
                        .destination(fileFacade.command().unmute().destination())
                        .range(Range.get(Range.Type.SERVER))
                        .sound(module.soundOrThrow())
                        .tagResolvers(fResolver -> new TagResolver[]{messagePipeline.targetTag("moderator", fResolver, fModerator)})
                        .build()
                )
                .moderator(fModerator)
                .moderations(mutes)
                .build()
        );
    }

    private void handleUnwarnCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        UnwarnModule module = injector.getInstance(UnwarnModule.class);

        FPlayer fModerator = gson.fromJson(input.readUTF(), FPlayer.class);
        if (moduleController.isDisabledFor(module, fModerator)) return;

        List<Moderation> warns = gson.fromJson(input.readUTF(), new TypeToken<List<Moderation>>(){}.getType());

        messageDispatcher.dispatch(module, UnModerationMetadata.<Localization.Command.Unwarn>builder()
                .base(EventMetadata.<Localization.Command.Unwarn>builder()
                        .uuid(metadataUUID)
                        .sender(fEntity)
                        .format(Localization.Command.Unwarn::format)
                        .destination(fileFacade.command().unwarn().destination())
                        .range(Range.get(Range.Type.SERVER))
                        .sound(module.soundOrThrow())
                        .tagResolvers(fResolver -> new TagResolver[]{messagePipeline.targetTag("moderator", fResolver, fModerator)})
                        .build()
                )
                .moderator(fModerator)
                .moderations(warns)
                .build()
        );
    }

    private void handlePollCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        PollModule.Action action = PollModule.Action.valueOf(input.readUTF());
        switch (action) {
            case CREATE -> {
                PollModule module = injector.getInstance(PollModule.class);
                if (moduleController.isDisabledFor(module, fEntity)) return;

                Poll poll = gson.fromJson(input.readUTF(), Poll.class);
                module.saveAndUpdateLast(poll);

                messageDispatcher.dispatch(module, PollMetadata.<Localization.Command.Poll>builder()
                        .base(EventMetadata.<Localization.Command.Poll>builder()
                                .uuid(metadataUUID)
                                .sender(fEntity)
                                .format(module.resolvePollFormat(fEntity, poll, PollModule.Status.START))
                                .range(Range.get(Range.Type.SERVER))
                                .message(poll.getTitle())
                                .sound(module.soundOrThrow())
                                .build()
                        )
                        .poll(poll)
                        .build()
                );
            }
            case VOTE -> injector.getInstance(PollModule.class).vote(fEntity, input.readInt(), input.readInt(), metadataUUID);
        }
    }

    private void handleSpyCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        SpyModule module = injector.getInstance(SpyModule.class);
        if (!moduleController.isEnable(module)) return;

        String action = input.readUTF();
        String string = input.readUTF();

        messageDispatcher.dispatch(module, SpyMetadata.<Localization.Command.Spy>builder()
                .base(EventMetadata.<Localization.Command.Spy>builder()
                        .uuid(metadataUUID)
                        .sender(fEntity)
                        .format(Localization.Command.Spy::formatLog)
                        .range(Range.get(Range.Type.SERVER))
                        .destination(fileFacade.command().spy().destination())
                        .message(string)
                        .filter(module.createFilter(fEntity instanceof FPlayer fPlayer ? fPlayer : FPlayer.UNKNOWN, Collections.emptyList()))
                        .build()
                )
                .turned(true)
                .action(action)
                .build()
        );
    }

    private void handleStreamCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        StreamModule module = injector.getInstance(StreamModule.class);
        if (moduleController.isDisabledFor(module, fEntity)) return;

        String message = input.readUTF();

        messageDispatcher.dispatch(module, StreamMetadata.<Localization.Command.Stream>builder()
                .base(EventMetadata.<Localization.Command.Stream>builder()
                        .uuid(metadataUUID)
                        .sender(fEntity)
                        .format(module.replaceUrls(message))
                        .range(Range.get(Range.Type.SERVER))
                        .destination(fileFacade.command().stream().destination())
                        .sound(module.soundOrThrow())
                        .build()
                )
                .turned(true)
                .urls(message)
                .build()
        );
    }

    private void handleTellCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        TellModule module = injector.getInstance(TellModule.class);
        if (moduleController.isDisabledFor(module, fEntity)) return;

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
        if (moduleController.isDisabledFor(module, fEntity)) return;

        String targetLang = input.readUTF();
        String message = input.readUTF();
        String messageToTranslate = input.readUTF();

        messageDispatcher.dispatch(module, TranslatetoMetadata.<Localization.Command.Translateto>builder()
                .base(EventMetadata.<Localization.Command.Translateto>builder()
                        .uuid(metadataUUID)
                        .sender(fEntity)
                        .format(module.replaceLanguage(targetLang))
                        .range(Range.get(Range.Type.SERVER))
                        .destination(fileFacade.command().translateto().destination())
                        .message(message)
                        .sound(module.soundOrThrow())
                        .build()
                )
                .targetLanguage(targetLang)
                .messageToTranslate(messageToTranslate)
                .build()
        );
    }

    private void handleTryCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        TryModule module = injector.getInstance(TryModule.class);
        if (moduleController.isDisabledFor(module, fEntity)) return;

        int value = input.readInt();
        String message = input.readUTF();

        messageDispatcher.dispatch(module, TryMetadata.<Localization.Command.CommandTry>builder()
                .base(EventMetadata.<Localization.Command.CommandTry>builder()
                        .uuid(metadataUUID)
                        .sender(fEntity)
                        .format(module.replacePercent(value))
                        .range(Range.get(Range.Type.SERVER))
                        .destination(fileFacade.command().commandTry().destination())
                        .message(message)
                        .sound(module.soundOrThrow())
                        .build()
                )
                .percent(value)
                .build()
        );
    }

    private void handleWarnCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        WarnModule module = injector.getInstance(WarnModule.class);

        Moderation warn = gson.fromJson(input.readUTF(), Moderation.class);

        FPlayer fModerator = fPlayerService.getFPlayer(warn.moderator());
        if (moduleController.isDisabledFor(module, fModerator)) return;

        ModerationMessageFormatter moderationMessageFormatter = injector.getInstance(ModerationMessageFormatter.class);

        messageDispatcher.dispatch(module, ModerationMetadata.<Localization.Command.Warn>builder()
                .base(EventMetadata.<Localization.Command.Warn>builder()
                        .uuid(metadataUUID)
                        .sender(fEntity)
                        .format((fReceiver, localization) ->
                                moderationMessageFormatter.replacePlaceholders(localization.server(), fReceiver, warn)
                        )
                        .range(Range.get(Range.Type.SERVER))
                        .destination(fileFacade.command().warn().destination())
                        .sound(module.soundOrThrow())
                        .tagResolvers(fResolver -> new TagResolver[]{messagePipeline.targetTag("moderator", fResolver, fModerator)})
                        .build()
                )
                .moderation(warn)
                .build()
        );

        module.sendForTarget(fModerator, (FPlayer) fEntity, warn);
    }

    private void handleKickCommand(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        KickModule module = injector.getInstance(KickModule.class);

        Moderation kick = gson.fromJson(input.readUTF(), Moderation.class);

        FPlayer fModerator = fPlayerService.getFPlayer(kick.moderator());
        if (moduleController.isDisabledFor(module, fModerator)) return;

        ModerationMessageFormatter moderationMessageFormatter = injector.getInstance(ModerationMessageFormatter.class);

        module.kick(fModerator, (FPlayer) fEntity, kick);

        messageDispatcher.dispatch(module, ModerationMetadata.<Localization.Command.Kick>builder()
                .base(EventMetadata.<Localization.Command.Kick>builder()
                        .uuid(metadataUUID)
                        .sender(fEntity)
                        .format((fReceiver, message) ->
                                moderationMessageFormatter.replacePlaceholders(message.server(), fReceiver, kick)
                        )
                        .destination(fileFacade.command().kick().destination())
                        .range(Range.get(Range.Type.SERVER))
                        .sound(module.soundOrThrow())
                        .tagResolvers(fResolver -> new TagResolver[]{messagePipeline.targetTag("moderator", fResolver, fModerator)})
                        .build()
                )
                .moderation(kick)
                .build()
        );
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
        if (!(fEntity instanceof FPlayer fPlayer)) return;

        ChatModule module = injector.getInstance(ChatModule.class);
        if (moduleController.isDisabledFor(module, fPlayer)) return;

        String proxyChatName = input.readUTF();
        String message = input.readUTF();

        Optional<Map.Entry<String, Message.Chat.Type>> optionalChat = fileFacade.message().chat().types()
                .entrySet()
                .stream()
                .filter(chat -> chat.getKey().equals(proxyChatName))
                .findAny();

        if (optionalChat.isEmpty()) return;
        String chatName = optionalChat.get().getKey();
        Message.Chat.Type chatType = optionalChat.get().getValue();

        Chat playerChat = new Chat(chatName, chatType, module.permission().types().get(chatName));

        messageDispatcher.dispatch(module, ChatMetadata.<Localization.Message.Chat>builder()
                .base(EventMetadata.<Localization.Message.Chat>builder()
                        .uuid(metadataUUID)
                        .sender(fPlayer)
                        .format(s -> s.types().get(chatName))
                        .range(Range.get(Range.Type.SERVER))
                        .destination(chatType.destination())
                        .message(message)
                        .sound(playerChat.sound())
                        .filter(module.permissionFilter(chatName))
                        .build()
                )
                .chat(playerChat)
                .build()
        );
    }

    private void handleClearchatCommand(FEntity fEntity) {
        if (!(fEntity instanceof FPlayer fPlayer)) return;

        ClearchatModule module = injector.getInstance(ClearchatModule.class);
        if (!moduleController.isEnable(module)) return;

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
        if (moduleController.isDisabledFor(module, fEntity)) return;

        boolean hasPlayedBefore = input.readBoolean();
        boolean ignoreVanish = input.readBoolean();

        Message.Join message = fileFacade.message().join();

        messageDispatcher.dispatch(module, JoinMetadata.<Localization.Message.Join>builder()
                .base(EventMetadata.<Localization.Message.Join>builder()
                        .uuid(metadataUUID)
                        .sender(fEntity)
                        .format(s -> hasPlayedBefore || !message.first() ? s.format() : s.formatFirstTime())
                        .destination(message.destination())
                        .range(Range.get(Range.Type.SERVER))
                        .sound(module.soundOrThrow())
                        .build()
                )
                .ignoreVanish(ignoreVanish)
                .playedBefore(hasPlayedBefore)
                .build()
        );
    }

    private void handleQuit(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        QuitModule module = injector.getInstance(QuitModule.class);
        if (moduleController.isDisabledFor(module, fEntity)) return;

        boolean ignoreVanish = input.readBoolean();

        messageDispatcher.dispatch(module, QuitMetadata.<Localization.Message.Quit>builder()
                .base(EventMetadata.<Localization.Message.Quit>builder()
                        .uuid(metadataUUID)
                        .sender(fEntity)
                        .format(Localization.Message.Quit::format)
                        .destination(fileFacade.message().quit().destination())
                        .range(Range.get(Range.Type.SERVER))
                        .sound(module.soundOrThrow())
                        .build()
                )
                .ignoreVanish(ignoreVanish)
                .build()
        );
    }

    private void handleAfk(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        AfkModule module = injector.getInstance(AfkModule.class);
        if (moduleController.isDisabledFor(module, fEntity)) return;

        boolean isAfk = input.readBoolean();

        messageDispatcher.dispatch(module, AFKMetadata.<Localization.Message.Afk>builder()
                .base(EventMetadata.<Localization.Message.Afk>builder()
                        .uuid(metadataUUID)
                        .sender(fEntity)
                        .format(localization -> isAfk
                                ? localization.formatTrue().global()
                                : localization.formatFalse().global()
                        )
                        .range(Range.get(Range.Type.SERVER))
                        .destination(fileFacade.message().afk().destination())
                        .sound(module.soundOrThrow())
                        .build()
                )
                .newStatus(isAfk)
                .build()
        );
    }

    private void handleVanilla(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        VanillaModule module = injector.getInstance(VanillaModule.class);
        if (moduleController.isDisabledFor(module, fEntity)) return;

        String translationKey = input.readUTF();
        Map<Integer, Object> arguments = parseVanillaArguments(readAsJsonObject(input));

        Message.Vanilla.VanillaMessage vanillaMessage = injector.getInstance(ComponentExtractor.class).getVanillaMessage(translationKey);

        ParsedComponent parsedComponent = new ParsedComponent(translationKey, vanillaMessage, arguments);

        String vanillaMessageName = vanillaMessage.name();

        messageDispatcher.dispatch(module, VanillaMetadata.<Localization.Message.Vanilla>builder()
                .base(EventMetadata.<Localization.Message.Vanilla>builder()
                        .uuid(metadataUUID)
                        .sender(fEntity)
                        .format(localization -> StringUtils.defaultString(localization.types().get(parsedComponent.translationKey())))
                        .tagResolvers(fResolver -> new TagResolver[]{module.argumentTag(fResolver, parsedComponent)})
                        .range(Range.get(Range.Type.SERVER))
                        .filter(fResolver -> vanillaMessageName.isEmpty() || fResolver.isSetting(vanillaMessageName))
                        .destination(parsedComponent.vanillaMessage().destination())
                        .build()
                )
                .parsedComponent(parsedComponent)
                .build()
        );
    }

    private Map<Integer, Object> parseVanillaArguments(JsonObject jsonObject) {
        Int2ObjectOpenHashMap<Object> result = new Int2ObjectOpenHashMap<>();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            int key = Integer.parseInt(entry.getKey());
            JsonObject argumentJson = entry.getValue().getAsJsonObject();

            Optional<FEntity> entity = parseFEntity(argumentJson);
            result.put(key, entity.isPresent() ? entity.get() : gson.fromJson(argumentJson, Component.class));
        }

        return result;
    }

    protected JsonObject readAsJsonObject(DataInputStream input) throws IOException {
        return gson.fromJson(input.readUTF(), JsonObject.class);
    }

    protected Optional<FEntity> parseFEntity(JsonObject jsonObject) {
        if (jsonObject.has("name") && jsonObject.has("uuid") && jsonObject.has("type")) {
            boolean isPlayer = jsonObject.has("id");
            return Optional.of(gson.fromJson(jsonObject, isPlayer ? FPlayer.class : FEntity.class));
        }

        return Optional.empty();
    }

}
