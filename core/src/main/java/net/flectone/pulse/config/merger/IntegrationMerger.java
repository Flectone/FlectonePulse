package net.flectone.pulse.config.merger;

import net.flectone.pulse.config.Integration;
import org.mapstruct.*;

@Mapper(config = MapstructMergerConfig.class)
public interface IntegrationMerger {

    @Mapping(target = "advancedban", expression = "java(mergeAdvancedban(target.build().advancedban().toBuilder(), source.advancedban()))")
    @Mapping(target = "libertybans", expression = "java(mergeLibertybans(target.build().libertybans().toBuilder(), source.libertybans()))")
    @Mapping(target = "deepl", expression = "java(mergeDeepl(target.build().deepl().toBuilder(), source.deepl()))")
    @Mapping(target = "discord", expression = "java(mergeDiscord(target.build().discord().toBuilder(), source.discord()))")
    @Mapping(target = "geyser", expression = "java(mergeGeyser(target.build().geyser().toBuilder(), source.geyser()))")
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

    Integration.Advancedban mergeAdvancedban(@MappingTarget Integration.Advancedban.AdvancedbanBuilder target, Integration.Advancedban advancedban);

    Integration.Libertybans mergeLibertybans(@MappingTarget Integration.Libertybans.LibertybansBuilder target, Integration.Libertybans libertybans);

    Integration.Deepl mergeDeepl(@MappingTarget Integration.Deepl.DeeplBuilder target, Integration.Deepl deepl);

    @Mapping(target = "presence", expression = "java(mergePresence(target.build().presence().toBuilder(), source.presence()))")
    @Mapping(target = "channelInfo", expression = "java(mergeChannelInfo(target.build().channelInfo().toBuilder(), source.channelInfo()))")
    Integration.Discord mergeDiscord(@MappingTarget Integration.Discord.DiscordBuilder target, Integration.Discord source);

    @Mapping(target = "activity", expression = "java(mergeActivity(target.build().activity().toBuilder(), source.activity()))")
    Integration.Discord.Presence mergePresence(@MappingTarget Integration.Discord.Presence.PresenceBuilder target, Integration.Discord.Presence source);

    Integration.Discord.Presence.Activity mergeActivity(@MappingTarget Integration.Discord.Presence.Activity.ActivityBuilder target, Integration.Discord.Presence.Activity activity);

    Integration.ChannelInfo mergeChannelInfo(@MappingTarget Integration.ChannelInfo.ChannelInfoBuilder target, Integration.ChannelInfo channelInfo);

    Integration.Geyser mergeGeyser(@MappingTarget Integration.Geyser.GeyserBuilder target, Integration.Geyser geyser);

    Integration.Interactivechat mergeInteractivechat(@MappingTarget Integration.Interactivechat.InteractivechatBuilder target, Integration.Interactivechat interactivechat);

    Integration.Itemsadder mergeItemsadder(@MappingTarget Integration.Itemsadder.ItemsadderBuilder target, Integration.Itemsadder itemsadder);

    Integration.Litebans mergeLitebans(@MappingTarget Integration.Litebans.LitebansBuilder target, Integration.Litebans litebans);

    Integration.Luckperms mergeLuckperms(@MappingTarget Integration.Luckperms.LuckpermsBuilder target, Integration.Luckperms luckperms);

    Integration.Maintenance mergeMaintenance(@MappingTarget Integration.Maintenance.MaintenanceBuilder target, Integration.Maintenance maintenance);

    Integration.MiniMOTD mergeMiniMOTD(@MappingTarget Integration.MiniMOTD.MiniMOTDBuilder target, Integration.MiniMOTD miniMOTD);

    Integration.MiniPlaceholders mergeMiniPlaceholders(@MappingTarget Integration.MiniPlaceholders.MiniPlaceholdersBuilder target, Integration.MiniPlaceholders miniPlaceholders);

    Integration.MOTD mergeMOTD(@MappingTarget Integration.MOTD.MOTDBuilder target, Integration.MOTD motd);

    Integration.Placeholderapi mergePlaceholderapi(@MappingTarget Integration.Placeholderapi.PlaceholderapiBuilder target, Integration.Placeholderapi placeholderapi);

    Integration.Plasmovoice mergePlasmovoice(@MappingTarget Integration.Plasmovoice.PlasmovoiceBuilder target, Integration.Plasmovoice plasmovoice);

    Integration.Simplevoice mergeSimplevoice(@MappingTarget Integration.Simplevoice.SimplevoiceBuilder target, Integration.Simplevoice simplevoice);

    Integration.Skinsrestorer mergeSkinsrestorer(@MappingTarget Integration.Skinsrestorer.SkinsrestorerBuilder target, Integration.Skinsrestorer skinsrestorer);

    Integration.Supervanish mergeSupervanish(@MappingTarget Integration.Supervanish.SupervanishBuilder target, Integration.Supervanish supervanish);

    Integration.Tab mergeTab(@MappingTarget Integration.Tab.TabBuilder target, Integration.Tab tab);

    @Mapping(target = "channelInfo", expression = "java(mergeChannelInfo(target.build().channelInfo().toBuilder(), source.channelInfo()))")
    Integration.Telegram mergeTelegram(@MappingTarget Integration.Telegram.TelegramBuilder target, Integration.Telegram source);

    Integration.Triton mergeTriton(@MappingTarget Integration.Triton.TritonBuilder target, Integration.Triton triton);

    Integration.Twitch mergeTwitch(@MappingTarget Integration.Twitch.TwitchBuilder target, Integration.Twitch twitch);

    Integration.Vault mergeVault(@MappingTarget Integration.Vault.VaultBuilder target, Integration.Vault vault);

    Integration.Yandex mergeYandex(@MappingTarget Integration.Yandex.YandexBuilder target, Integration.Yandex yandex);

    Integration.Command mergeIntegrationCommand(@MappingTarget Integration.Command.CommandBuilder target, Integration.Command command);

}