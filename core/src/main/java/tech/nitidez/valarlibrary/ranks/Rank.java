package tech.nitidez.valarlibrary.ranks;

import java.util.ArrayList;
import java.util.List;

import tech.nitidez.valarlibrary.vLib;
import tech.nitidez.valarlibrary.plugin.config.ValarConfig;
import tech.nitidez.valarlibrary.utils.StringUtils;

public class Rank {
    private static List<Rank> RANKS = new ArrayList<>();

    private String rankId;
    private String name;
    private String prefix;
    private boolean alwaysVisible;
    private boolean broadcast;
    private String permission;

    public Rank(String rankId, String name, String prefix, boolean alwaysVisible, boolean broadcast, String permission) {
        this.rankId = rankId;
        this.name = name;
        this.prefix = StringUtils.formatColors(prefix);
        this.alwaysVisible = alwaysVisible;
        this.broadcast = broadcast;
        this.permission = permission;
    }

    public int getId() {
        return RANKS.indexOf(this);
    }

    public String getRankId() {
        return this.rankId;
    }

    public String getName() {
        return this.name;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public boolean isAlwaysVisible() {
        return this.alwaysVisible;
    }

    public boolean hasBroadcast() {
        return this.broadcast;
    }

    public String getPermission() {
        return this.permission;
    }

    public static Rank getRank(String rankName) {
        return RANKS.stream().filter(r -> r.getRankId().equals(rankName)).findFirst().orElse(null);
    }

    public static List<Rank> getRanks() {
        return RANKS;
    }

    public static void setupRanks() {
        ValarConfig cfg = vLib.getInstance().getConfig("ranks");
        for (String key : cfg.getSection("ranks").getKeys(false)) {
            String name = cfg.getString("ranks."+key+".name");
            String prefix = cfg.getString("ranks."+key+".prefix");
            String permission = cfg.getString("ranks."+key+".permission");
            boolean broadcast = cfg.getBoolean("ranks."+key+".broadcast");
            boolean alwaysvisible = cfg.getBoolean("ranks."+key+".alwaysvisible");
            RANKS.add(new Rank(key, name, prefix, alwaysvisible, broadcast, permission));
        }
        if (RANKS.size() == 0) {
            RANKS.add(new Rank("member", "Member", "&7", false, false, ""));
        }
    }
}
