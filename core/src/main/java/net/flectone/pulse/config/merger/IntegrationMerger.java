package net.flectone.pulse.config.merger;

import net.flectone.pulse.config.Integration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for merging {@link Integration} configuration objects.
 * <p>
 * This interface defines mapping methods for deep merging plugin integration configurations,
 * handling nested structures through builder patterns.
 * </p>
 *
 * @author TheFaser
 * @since 1.7.1
 */
@Mapper(config = MapstructMergerConfig.class)
public interface IntegrationMerger {

    @Mapping(target = "advancedban", expression = "java(mergeAdvancedban(target.build().advancedban().toBuilder(), source.advancedban()))")
    @Mapping(target = "blazeandcave", expression = "java(mergeBlazeandcave(target.build().blazeandcave().toBuilder(), source.blazeandcave()))")
    @Mapping(target = "cmi", expression = "java(mergeCmi(target.build().cmi().toBuilder(), source.cmi()))")
    @Mapping(target = "libertybans", expression = "java(mergeLibertybans(target.build().libertybans().toBuilder(), source.libertybans()))")
    @Mapping(target = "deepl", expression = "java(mergeDeepl(target.build().deepl().toBuilder(), source.deepl()))")
    @Mapping(target = "discord", expression = "java(mergeDiscord(target.build().discord().toBuilder(), source.discord()))")
    @Mapping(target = "floodgate", expression = "java(mergeFloodgate(target.build().floodgate().toBuilder(), source.floodgate()))")
    @Mapping(target = "geyser", expression = "java(mergeGeyser(target.build().geyser().toBuilder(), source.geyser()))")
    @Mapping(target = "icu", expression = "java(mergeIcu(target.build().icu().toBuilder(), source.icu()))")
    @Mapping(target = "interactivechat", expression = "java(mergeInteractivechat(target.build().interactivechat().toBuilder(), source.interactivechat()))")
    @Mapping(target = "itemsadder", expression = "java(mergeItemsadder(target.build().itemsadder().toBuilder(), source.itemsadder()))")
    @Mapping(target = "litebans", expression = "java(mergeLitebans(target.build().litebans().toBuilder(), source.litebans()))")
    @Mapping(target = "luckperms", expression = "java(mergeLuckperms(target.build().luckperms().toBuilder(), source.luckperms()))")
    @Mapping(target = "maintenance", expression = "java(mergeMaintenance(target.build().maintenance().toBuilder(), source.maintenance()))")
    @Mapping(target = "minimotd", expression = "java(mergeMiniMOTD(target.build().minimotd().toBuilder(), source.minimotd()))")
    @Mapping(target = "miniplaceholders", expression = "java(mergeMiniPlaceholders(target.build().miniplaceholders().toBuilder(), source.miniplaceholders()))")
    @Mapping(target = "motd", expression = "java(mergeMOTD(target.build().motd().toBuilder(), source.motd()))")
    @Mapping(target = "placeholderapi", expression = "java(mergePlaceholderapi(target.build().placeholderapi().toBuilder(), source.placeholderapi()))")
    @Mapping(target = "plasmovoice", expression = "java(mergePlasmovoice(target.build().plasmovoice().toBuilder(), source.plasmovoice()))")
    @Mapping(target = "simplevoice", expression = "java(mergeSimplevoice(target.build().simplevoice().toBuilder(), source.simplevoice()))")
    @Mapping(target = "skinsrestorer", expression = "java(mergeSkinsrestorer(target.build().skinsrestorer().toBuilder(), source.skinsrestorer()))")
    @Mapping(target = "supervanish", expression = "java(mergeSupervanish(target.build().supervanish().toBuilder(), source.supervanish()))")
    @Mapping(target = "tab", expression = "java(mergeTab(target.build().tab().toBuilder(), source.tab()))")
    @Mapping(target = "telegram", expression = "java(mergeTelegram(target.build().telegram().toBuilder(), source.telegram()))")
    @Mapping(target = "triton", expression = "java(mergeTriton(target.build().triton().toBuilder(), source.triton()))")
    @Mapping(target = "twitch", expression = "java(mergeTwitch(target.build().twitch().toBuilder(), source.twitch()))")
    @Mapping(target = "vault", expression = "java(mergeVault(target.build().vault().toBuilder(), source.vault()))")
    @Mapping(target = "yandex", expression = "java(mergeYandex(target.build().yandex().toBuilder(), source.yandex()))")
    Integration merge(@MappingTarget Integration.IntegrationBuilder target, Integration source);

    Integration.Advancedban mergeAdvancedban(@MappingTarget Integration.Advancedban.AdvancedbanBuilder target, Integration.Advancedban source);

    Integration.Blazeandcave mergeBlazeandcave(@MappingTarget Integration.Blazeandcave.BlazeandcaveBuilder target, Integration.Blazeandcave source);

    Integration.CMI mergeCmi(@MappingTarget Integration.CMI.CMIBuilder target, Integration.CMI source);

    Integration.Libertybans mergeLibertybans(@MappingTarget Integration.Libertybans.LibertybansBuilder target, Integration.Libertybans source);

    Integration.Deepl mergeDeepl(@MappingTarget Integration.Deepl.DeeplBuilder target, Integration.Deepl source);

    @Mapping(target = "presence", expression = "java(mergeDiscordPresence(target.build().presence().toBuilder(), source.presence()))")
    @Mapping(target = "channelInfo", expression = "java(mergeChannelInfo(target.build().channelInfo().toBuilder(), source.channelInfo()))")
    Integration.Discord mergeDiscord(@MappingTarget Integration.Discord.DiscordBuilder target, Integration.Discord source);

    @Mapping(target = "activity", expression = "java(mergeDiscordPresenceActivity(target.build().activity().toBuilder(), source.activity()))")
    Integration.Discord.Presence mergeDiscordPresence(@MappingTarget Integration.Discord.Presence.PresenceBuilder target, Integration.Discord.Presence source);

    Integration.Discord.Presence.Activity mergeDiscordPresenceActivity(@MappingTarget Integration.Discord.Presence.Activity.ActivityBuilder target, Integration.Discord.Presence.Activity source);

    Integration.ChannelInfo mergeChannelInfo(@MappingTarget Integration.ChannelInfo.ChannelInfoBuilder target, Integration.ChannelInfo source);

    Integration.Floodgate mergeFloodgate(@MappingTarget Integration.Floodgate.FloodgateBuilder target, Integration.Floodgate source);

    Integration.Geyser mergeGeyser(@MappingTarget Integration.Geyser.GeyserBuilder target, Integration.Geyser source);

    Integration.Icu mergeIcu(@MappingTarget Integration.Icu.IcuBuilder target, Integration.Icu source);

    Integration.Interactivechat mergeInteractivechat(@MappingTarget Integration.Interactivechat.InteractivechatBuilder target, Integration.Interactivechat source);

    Integration.Itemsadder mergeItemsadder(@MappingTarget Integration.Itemsadder.ItemsadderBuilder target, Integration.Itemsadder source);

    Integration.Litebans mergeLitebans(@MappingTarget Integration.Litebans.LitebansBuilder target, Integration.Litebans source);

    Integration.Luckperms mergeLuckperms(@MappingTarget Integration.Luckperms.LuckpermsBuilder target, Integration.Luckperms source);

    Integration.Maintenance mergeMaintenance(@MappingTarget Integration.Maintenance.MaintenanceBuilder target, Integration.Maintenance source);

    Integration.MiniMOTD mergeMiniMOTD(@MappingTarget Integration.MiniMOTD.MiniMOTDBuilder target, Integration.MiniMOTD source);

    Integration.MiniPlaceholders mergeMiniPlaceholders(@MappingTarget Integration.MiniPlaceholders.MiniPlaceholdersBuilder target, Integration.MiniPlaceholders source);

    Integration.MOTD mergeMOTD(@MappingTarget Integration.MOTD.MOTDBuilder target, Integration.MOTD source);

    Integration.Placeholderapi mergePlaceholderapi(@MappingTarget Integration.Placeholderapi.PlaceholderapiBuilder target, Integration.Placeholderapi source);

    Integration.Plasmovoice mergePlasmovoice(@MappingTarget Integration.Plasmovoice.PlasmovoiceBuilder target, Integration.Plasmovoice source);

    Integration.Simplevoice mergeSimplevoice(@MappingTarget Integration.Simplevoice.SimplevoiceBuilder target, Integration.Simplevoice source);

    Integration.Skinsrestorer mergeSkinsrestorer(@MappingTarget Integration.Skinsrestorer.SkinsrestorerBuilder target, Integration.Skinsrestorer source);

    Integration.Supervanish mergeSupervanish(@MappingTarget Integration.Supervanish.SupervanishBuilder target, Integration.Supervanish source);

    Integration.Tab mergeTab(@MappingTarget Integration.Tab.TabBuilder target, Integration.Tab source);

    @Mapping(target = "channelInfo", expression = "java(mergeChannelInfo(target.build().channelInfo().toBuilder(), source.channelInfo()))")
    Integration.Telegram mergeTelegram(@MappingTarget Integration.Telegram.TelegramBuilder target, Integration.Telegram source);

    Integration.Triton mergeTriton(@MappingTarget Integration.Triton.TritonBuilder target, Integration.Triton source);

    Integration.Twitch mergeTwitch(@MappingTarget Integration.Twitch.TwitchBuilder target, Integration.Twitch source);

    Integration.Vault mergeVault(@MappingTarget Integration.Vault.VaultBuilder target, Integration.Vault source);

    Integration.Yandex mergeYandex(@MappingTarget Integration.Yandex.YandexBuilder target, Integration.Yandex source);

}