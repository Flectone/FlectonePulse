package net.flectone.pulse.file;

public interface IModule {

    ICommand getCommand();
    IIntegration getIntegration();
    IMessage getMessage();

    interface ICommand {

        ISubCommand getAfk();
        ISubCommand getBall();
        ISubCommand getBan();
        ISubCommand getBanlist();
        ISubCommand getBroadcast();
        ISubCommand getChatcolor();
        ISubCommand getChatsetting();
        ISubCommand getClearchat();
        ISubCommand getClearmail();
        ISubCommand getCoin();
        ISubCommand getDice();
        ISubCommand getDo();
        ISubCommand getFlectonepulse();
        ISubCommand getGeolocate();
        ISubCommand getHelper();
        ISubCommand getIgnore();
        ISubCommand getIgnorelist();
        ISubCommand getKick();
        ISubCommand getMail();
        ISubCommand getMaintenance();
        ISubCommand getMark();
        ISubCommand getMe();
        ISubCommand getMute();
        ISubCommand getMutelist();
        ISubCommand getOnline();
        ISubCommand getPing();
        ISubCommand getPoll();
        ISubCommand getReply();
        ISubCommand getRockpaperscissors();
        ISubCommand getSpit();
        ISubCommand getSpy();
        ISubCommand getStream();
        ISubCommand getSymbol();
        ISubCommand getTell();
        ISubCommand getTictactoe();
        ISubCommand getTranslateto();
        ISubCommand getTry();
        ISubCommand getUnban();
        ISubCommand getUnmute();
        ISubCommand getUnwarn();
        ISubCommand getWarn();
        ISubCommand getWarnlist();

        interface ISubCommand {}
    }

    interface IIntegration {

        ISubIntegration getDiscord();
        ISubIntegration getLuckperms();
        ISubIntegration getPlaceholderapi();
        ISubIntegration getPlasmovoice();
        ISubIntegration getSimplevoice();
        ISubIntegration getSkinsrestorer();
        ISubIntegration getSupervanish();
        ISubIntegration getTelegram();
        ISubIntegration getTwitch();
        ISubIntegration getVault();

        interface ISubIntegration {}
    }

    interface IMessage {

        ISubMessage getAdvancement();
        ISubMessage getAnvil();
        ISubMessage getAuto();
        ISubMessage getBook();
        ISubMessage getBrand();
        ISubMessage getBubble();
        ISubMessage getChat();
        ISubMessage getClear();
        IContactMessage getContact();
        ISubMessage getDeath();
        ISubMessage getDeop();
        ISubMessage getEnchant();
        IFormatMessage getFormat();
        ISubMessage getGamemode();
        ISubMessage getGreeting();
        ISubMessage getJoin();
        IObjectiveMessage getObjective();
        ISubMessage getOp();
        ISubMessage getQuit();
        ISubMessage getScoreboard();
        ISubMessage getSeed();
        ISubMessage getSetblock();
        ISubMessage getSign();
        ISubMessage getSpawnpoint();
        IStatusMessage getStatus();
        ITabMessage getTab();

        interface ISubMessage {}

        interface IContactMessage extends ISubMessage {

            ISubContactMessage getAfk();
            ISubContactMessage getKnock();
            ISubContactMessage getMark();
            ISubContactMessage getRightclick();
            ISubContactMessage getSign();
            ISubContactMessage getSpit();
            ISubContactMessage getUnsign();

            interface ISubContactMessage {}

        }

        interface IFormatMessage extends ISubMessage {

            ISubFormatMessage getColor();
            ISubFormatMessage getEmoji();
            ISubFormatMessage getImage();
            ISubFormatMessage getMention();
            IModerationFormatMessage getModeration();
            ISubFormatMessage getName_();
            ISubFormatMessage getQuestionAnswer();
            ISubFormatMessage getSpoiler();
            ISubFormatMessage getWorld();

            interface ISubFormatMessage {}

            interface IModerationFormatMessage extends ISubFormatMessage {

                ISubModerationFormatMessage getCaps();
                ISubModerationFormatMessage getSwear();

                interface ISubModerationFormatMessage {}

            }

        }

        interface IObjectiveMessage extends ISubMessage {

            ISubObjectiveMessage getBelowname();
            ISubObjectiveMessage getTabname();

            interface ISubObjectiveMessage {}

        }

        interface IStatusMessage {

            ISubStatusMessage getMotd();
            ISubStatusMessage getIcon();
            ISubStatusMessage getPlayers();
            ISubStatusMessage getVersion();

            interface ISubStatusMessage {}

        }

        interface ITabMessage extends ISubMessage {

            ISubTabMessage getFooter();
            ISubTabMessage getHeader();
            ISubTabMessage getPlayerlistname();

            interface ISubTabMessage {}

        }
    }
}
