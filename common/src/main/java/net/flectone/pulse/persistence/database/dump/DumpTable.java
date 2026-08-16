package net.flectone.pulse.persistence.database.dump;

public enum DumpTable {

    PLAYER("fp_player"),
    FCOLOR("fp_fcolor"),
    TIME("fp_time"),
    SETTING("fp_setting"),
    MAIL("fp_mail"),
    IGNORE("fp_ignore"),
    MODERATION("fp_moderation"),
    PLAYER_FCOLOR("fp_player_fcolor"),
    VERSION("fp_version");

    private final String tableName;

    DumpTable(String tableName) {
        this.tableName = tableName;
    }

    public String tableName() {
        return tableName;
    }

}
