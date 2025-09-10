package net.flectone.pulse.config;

public interface ModuleConfig {

    CommandConfig getCommand();
    IntegrationConfig getIntegration();
    MessageConfig getMessage();

    interface CommandConfig {

        SubCommandConfig getAfk();
        SubCommandConfig getAnon();
        SubCommandConfig getBall();
        SubCommandConfig getBan();
        SubCommandConfig getBanlist();
        SubCommandConfig getBroadcast();
        SubCommandConfig getChatcolor();
        SubCommandConfig getChatsetting();
        SubCommandConfig getClearchat();
        SubCommandConfig getClearmail();
        SubCommandConfig getCoin();
        SubCommandConfig getDeletemessage();
        SubCommandConfig getDice();
        SubCommandConfig getDo();
        SubCommandConfig getFlectonepulse();
        SubCommandConfig getGeolocate();
        SubCommandConfig getHelper();
        SubCommandConfig getIgnore();
        SubCommandConfig getIgnorelist();
        SubCommandConfig getKick();
        SubCommandConfig getMail();
        SubCommandConfig getMaintenance();
        SubCommandConfig getMe();
        SubCommandConfig getMute();
        SubCommandConfig getMutelist();
        SubCommandConfig getOnline();
        SubCommandConfig getPing();
        SubCommandConfig getPoll();
        SubCommandConfig getReply();
        SubCommandConfig getRockpaperscissors();
        SubCommandConfig getSpy();
        SubCommandConfig getStream();
        SubCommandConfig getSymbol();
        SubCommandConfig getTell();
        SubCommandConfig getTictactoe();
        SubCommandConfig getToponline();
        SubCommandConfig getTranslateto();
        SubCommandConfig getTry();
        SubCommandConfig getUnban();
        SubCommandConfig getUnmute();
        SubCommandConfig getUnwarn();
        SubCommandConfig getWarn();
        SubCommandConfig getWarnlist();

        interface SubCommandConfig {}
    }

    interface IntegrationConfig {

        SubIntegrationConfig getAdvancedban();
        SubIntegrationConfig getDeepl();
        SubIntegrationConfig getDiscord();
        SubIntegrationConfig getInteractivechat();
        SubIntegrationConfig getItemsadder();
        SubIntegrationConfig getLitebans();
        SubIntegrationConfig getLuckperms();
        SubIntegrationConfig getMaintenance();
        SubIntegrationConfig getMinimotd();
        SubIntegrationConfig getMiniplaceholders();
        SubIntegrationConfig getMotd();
        SubIntegrationConfig getPlaceholderapi();
        SubIntegrationConfig getPlasmovoice();
        SubIntegrationConfig getSimplevoice();
        SubIntegrationConfig getSkinsrestorer();
        SubIntegrationConfig getSupervanish();
        SubIntegrationConfig getTelegram();
        SubIntegrationConfig getTriton();
        SubIntegrationConfig getTwitch();
        SubIntegrationConfig getVault();
        SubIntegrationConfig getYandex();

        interface SubIntegrationConfig {}
    }

    interface MessageConfig {

        SubMessageConfig getAdvancement();
        SubMessageConfig getAfk();
        SubMessageConfig getAnvil();
        SubMessageConfig getAuto();
        SubMessageConfig getBed();
        SubMessageConfig getBook();
        SubMessageConfig getBrand();
        SubMessageConfig getBubble();
        SubMessageConfig getChat();
        SubMessageConfig getClear();
        SubMessageConfig getClone();
        SubMessageConfig getDamage();
        SubMessageConfig getDeath();
        SubMessageConfig getDeop();
        SubMessageConfig getDifficulty();
        SubMessageConfig getEffect();
        SubMessageConfig getEnchant();
        SubMessageConfig getExecute();
        SubMessageConfig getExperience();
        SubMessageConfig getFill();
        FormatMessageConfig getFormat();
        SubMessageConfig getGamemode();
        SubMessageConfig getGamerule();
        SubMessageConfig getGreeting();
        SubMessageConfig getJoin();
        SubMessageConfig getKill();
        ObjectiveMessageConfig getObjective();
        SubMessageConfig getOp();
        SubMessageConfig getQuit();
        SubMessageConfig getReload();
        SubMessageConfig getRightclick();
        SubMessageConfig getSave();
        SubMessageConfig getSeed();
        SubMessageConfig getSetblock();
        SubMessageConfig getSidebar();
        SubMessageConfig getSpawn();
        SubMessageConfig getSign();
        StatusMessageConfig getStatus();
        SubMessageConfig getSummon();
        TabMessageConfig getTab();
        SubMessageConfig getTeleport();
        SubMessageConfig getTime();
        SubMessageConfig getUpdate();
        SubMessageConfig getWeather();

        interface SubMessageConfig {}

        interface FormatMessageConfig extends SubMessageConfig {

            SubFormatMessageConfig getFcolor();
            SubFormatMessageConfig getFixation();
            SubFormatMessageConfig getMention();
            ModerationFormatMessageConfig getModeration();
            SubFormatMessageConfig getName_();
            SubFormatMessageConfig getQuestionAnswer();
            SubFormatMessageConfig getReplacement();
            SubFormatMessageConfig getScoreboard();
            SubFormatMessageConfig getTranslate();
            SubFormatMessageConfig getWorld();

            interface SubFormatMessageConfig {}

            interface ModerationFormatMessageConfig extends SubFormatMessageConfig {

                SubModerationFormatMessageConfig getCaps();
                SubModerationFormatMessageConfig getDelete();
                SubModerationFormatMessageConfig getNewbie();
                SubModerationFormatMessageConfig getFlood();
                SubModerationFormatMessageConfig getSwear();

                interface SubModerationFormatMessageConfig {}

            }

        }

        interface ObjectiveMessageConfig extends SubMessageConfig {

            SubObjectiveMessageConfig getBelowname();
            SubObjectiveMessageConfig getTabname();

            interface SubObjectiveMessageConfig {}

        }

        interface StatusMessageConfig {

            SubStatusMessageConfig getMotd();
            SubStatusMessageConfig getIcon();
            SubStatusMessageConfig getPlayers();
            SubStatusMessageConfig getVersion();

            interface SubStatusMessageConfig {}

        }

        interface TabMessageConfig extends SubMessageConfig {

            SubTabMessageConfig getFooter();
            SubTabMessageConfig getHeader();
            SubTabMessageConfig getPlayerlistname();

            interface SubTabMessageConfig {}

        }
    }
}
