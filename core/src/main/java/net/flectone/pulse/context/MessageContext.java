package net.flectone.pulse.context;

import lombok.Getter;
import lombok.Setter;
import net.flectone.pulse.model.FEntity;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.*;

@Setter
@Getter
public class MessageContext {

    private final Map<String, Boolean> flags = new HashMap<>();
    private final Set<TagResolver> tagResolvers = new HashSet<>();

    private final FEntity sender;
    private final FEntity receiver;
    private final UUID processId;
    private String message;
    private String messageToTranslate;

    public MessageContext(FEntity sender, FEntity receiver, String message) {
        this.processId = UUID.randomUUID();
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

    public void addTagResolvers(TagResolver... resolvers) {
        if (resolvers == null || resolvers.length == 0) return;

        tagResolvers.addAll(Set.of(resolvers));
    }

    public boolean isUserMessage() {
        return flags.getOrDefault("userMessage", false);
    }

    public void setUserMessage(boolean userMessage) {
        flags.put("userMessage", userMessage);
    }

    public boolean isMention() {
        return flags.getOrDefault("mention", false);
    }

    public void setMention(boolean mention) {
        flags.put("mention", mention);
    }

    public boolean isPlayer() {
        return flags.getOrDefault("player", true);
    }

    public void setPlayer(boolean player) {
        flags.put("player", player);
    }

    public boolean isEmoji() {
        return flags.getOrDefault("emoji", true);
    }

    public void setEmoji(boolean emoji) {
        flags.put("emoji", emoji);
    }

    public boolean isFixation() {
        return flags.getOrDefault("fixation", true);
    }

    public void setFixation(boolean fixation) {
        flags.put("fixation", fixation);
    }

    public boolean isQuestion() {
        return flags.getOrDefault("question", true);
    }

    public void setQuestion(boolean question) {
        flags.put("question", question);
    }

    public boolean isSpoiler() {
        return flags.getOrDefault("spoiler", true);
    }

    public void setSpoiler(boolean spoiler) {
        flags.put("spoiler", spoiler);
    }

    public boolean isTranslate() {
        return flags.getOrDefault("translate", false);
    }

    public void setTranslate(boolean translate) {
        flags.put("translate", translate);
    }

    public boolean isTranslateItem() {
        return flags.getOrDefault("translateItem", true);
    }

    public void setTranslateItem(boolean translateItem) {
        flags.put("translateItem", translateItem);
    }

    public boolean isSwear() {
        return flags.getOrDefault("swear", true);
    }

    public void setSwear(boolean swear) {
        flags.put("swear", swear);
    }

    public boolean isCaps() {
        return flags.getOrDefault("caps", true);
    }

    public void setCaps(boolean caps) {
        flags.put("caps", caps);
    }

    public boolean isFlood() {
        return flags.getOrDefault("flood", true);
    }

    public void setFlood(boolean flood) {
        flags.put("flood", flood);
    }

    public boolean isFormatting() {
        return flags.getOrDefault("formatting", true);
    }

    public void setFormatting(boolean formatting) {
        flags.put("formatting", formatting);
    }

    public boolean isUrl() {
        return flags.getOrDefault("url", true);
    }

    public void setUrl(boolean url) {
        flags.put("url", url);
    }

    public boolean isImage() {
        return flags.getOrDefault("image", true);
    }

    public void setImage(boolean image) {
        flags.put("image", image);
    }

    public boolean isColors() {
        return flags.getOrDefault("colors", true);
    }

    public void setColors(boolean colors) {
        flags.put("colors", colors);
    }

    public boolean isInteractiveChat() {
        return flags.getOrDefault("interactiveChat", true);
    }

    public void setInteractiveChat(boolean interactiveChat) {
        flags.put("interactiveChat", interactiveChat);
    }
}
