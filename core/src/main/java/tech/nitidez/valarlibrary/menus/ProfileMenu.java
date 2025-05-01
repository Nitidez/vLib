package tech.nitidez.valarlibrary.menus;

import org.bukkit.entity.Player;

import tech.nitidez.valarlibrary.lib.localization.LanguageAPI;
import tech.nitidez.valarlibrary.lib.menu.Menu;
import tech.nitidez.valarlibrary.lib.menu.PlayerMenu;
import tech.nitidez.valarlibrary.player.profile.Profile;
import tech.nitidez.valarlibrary.utils.BukkitUtils;

public class ProfileMenu extends PlayerMenu {
    private Player prplayer;
    private ProfileSection section;
    public static enum ProfileSection {
        PROFILE, STATS;
    }
    public ProfileMenu(Player player, Player prplayer, ProfileSection section) {
        super(player, Profile.loadProfile(player).getLanguage().get("menus.profile." + section.toString().toLowerCase() + ".title" + (player.equals(prplayer) ? "you" : "other")).getAsString().replace("{n}", Profile.loadProfile(prplayer).getName()), 6);
        this.prplayer = prplayer;
        topitems();
    }

    public ProfileSection getSection() {
        return section;
    }

    private void topitems() {
        this.setItem(this.same() ? 0 : 1, BukkitUtils.deserializeItemStack(
            ""
        ));
    }

    private boolean same() {return this.player.equals(this.prplayer);}
}
