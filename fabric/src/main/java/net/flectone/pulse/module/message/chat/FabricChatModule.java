package net.flectone.pulse.module.message.chat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;

@Singleton
public class FabricChatModule extends ChatModule {

    @Inject
    public FabricChatModule(FileManager fileManager) {
        super(fileManager);
    }

    @Override
    public void send(FPlayer fPlayer, Object event) {

    }

    @Override
    public void send(FEntity fPlayer, String chatName, String string) {

    }
}
