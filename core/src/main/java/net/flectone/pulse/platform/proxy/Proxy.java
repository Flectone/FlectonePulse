package net.flectone.pulse.platform.proxy;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.experimental.UtilityClass;
import net.flectone.pulse.util.MessageTag;

import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class Proxy {

    public byte[] create(byte[] data) {
        ByteArrayDataInput input = ByteStreams.newDataInput(data);

        String tag = input.readUTF();
        if (!tag.startsWith("FlectonePulse")) return null;

        MessageTag proxyMessageTag = MessageTag.fromProxyString(tag);
        if (proxyMessageTag == null) return null;

        int clustersCount = input.readInt();
        Set<String> clusters = new HashSet<>(clustersCount);

        for (int i = 0; i < clustersCount; i++) {
            clusters.add(input.readUTF());
        }

        boolean isPlayer = input.readBoolean();
        String fPlayer = input.readUTF();

        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF(tag);

        output.writeInt(clustersCount);
        for (String cluster : clusters) {
            output.writeUTF(cluster);
        }

        output.writeBoolean(isPlayer);
        output.writeUTF(fPlayer);

        switch (proxyMessageTag) {
            case COMMAND_ME, COMMAND_BROADCAST, COMMAND_DO, COMMAND_HELPER, COMMAND_CHATCOLOR,
                 COMMAND_STREAM, DEATH, ADVANCEMENT, COMMAND_DICE, COMMAND_UNBAN,
                 COMMAND_UNMUTE, COMMAND_UNWARN -> output.writeUTF(input.readUTF());

            case COMMAND_BALL, COMMAND_TRY -> {
                output.writeInt(input.readInt());
                output.writeUTF(input.readUTF());
            }

            case COMMAND_POLL_CREATE_MESSAGE, FROM_TWITCH_TO_MINECRAFT, FROM_TELEGRAM_TO_MINECRAFT -> {
                output.writeUTF(input.readUTF());
                output.writeUTF(input.readUTF());
                output.writeUTF(input.readUTF());
            }

            case CHAT, COMMAND_TRANSLATETO, FROM_DISCORD_TO_MINECRAFT, COMMAND_TELL, COMMAND_KICK, COMMAND_SPY,
                 COMMAND_ROCKPAPERSCISSORS_CREATE, COMMAND_ROCKPAPERSCISSORS_MOVE, COMMAND_ROCKPAPERSCISSORS_FINAL,
                 COMMAND_MUTE, COMMAND_BAN, COMMAND_WARN -> {
                output.writeUTF(input.readUTF());
                output.writeUTF(input.readUTF());
            }

            case COMMAND_COIN -> output.writeInt(input.readInt());

            case COMMAND_POLL_VOTE -> {
                output.writeInt(input.readInt());
                output.writeInt(input.readInt());
            }

            case COMMAND_TICTACTOE_CREATE -> {
                output.writeUTF(input.readUTF());
                output.writeInt(input.readInt());
                output.writeBoolean(input.readBoolean());
            }

            case COMMAND_TICTACTOE_MOVE -> {
                output.writeUTF(input.readUTF());
                output.writeUTF(input.readUTF());
                output.writeInt(input.readInt());
                output.writeUTF(input.readUTF());
            }

            case JOIN, AFK -> output.writeBoolean(input.readBoolean());

            default -> {}
        }

        return output.toByteArray();
    }

}
