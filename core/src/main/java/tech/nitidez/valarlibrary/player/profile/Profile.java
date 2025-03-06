package tech.nitidez.valarlibrary.player.profile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import tech.nitidez.valarlibrary.data.Database;
import tech.nitidez.valarlibrary.data.data.DataRow;
import tech.nitidez.valarlibrary.data.data.DataTable;
import tech.nitidez.valarlibrary.data.tables.ProfileTable;
import tech.nitidez.valarlibrary.lib.profile.Mojang;
import tech.nitidez.valarlibrary.ranks.Rank;

public class Profile {
    private static final Gson gson = new Gson();

    private DataRow data;
    private UUID uuid;

    private Profile(DataRow dataRow, UUID uuid) {
        this.data = dataRow;
        this.uuid = uuid;
    }

    public DataRow getData() {
        return this.data;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    public Rank getRank() {
        return this.getRanks().get(0);
    }

    public Rank getTag() {
        String tag = (String) data.get("tagged").orElse("");
        if (Rank.getRank(tag) != null && this.getRanks().contains(Rank.getRank(tag))) {
            return Rank.getRank(tag);
        } else {
            return this.getRank();
        }
    }

    public void setTag(Rank rank) {
        this.data.set("tagged", rank.getRankId());
    }

    public void setFake(String fakeName) {
        this.data.set("faked", fakeName);
    }

    public String getFakedName() {
        String faked = this.getFake();
        if (faked != null) {
            return faked;
        } else {
            return this.getName();
        }
    }

    public String getName() {
        if (this.getFake() != null) {
            try {
                String name = Mojang.getName(this.uuid);
                return name;
            } catch (Exception e) {
            }
        }
        return getDName();
    }

    public String getDName() {
        return (String) this.data.get("name").get();
    }

    public String getFake() {
        String faked = (String) this.data.get("faked").orElse("");
        if (faked.length() > 0) {
            return faked;
        } else return null;
    }

    public Map<String, Boolean> getPreferences() {
        String json = (String) this.data.get("preferences").orElse("{}");
        return gson.fromJson(json, new TypeToken<HashMap<String, Boolean>>(){}.getType());
    }

    public Map<String, String> getSelectedMap() {
        String json = (String) this.data.get("selected").orElse("{}");
        return gson.fromJson(json, new TypeToken<HashMap<String, String>>(){}.getType());
    }

    public Optional<String> getSelected(String selected) {
        Map<String, String> map = this.getSelectedMap();
        if (map.containsKey(selected)) {
            return Optional.of(map.get(selected));
        } else {
            return Optional.empty();
        }
    }

    public void setSelected(String selected, String s_value) {
        Map<String, String> map = this.getSelectedMap();
        map.put(selected, s_value);
        this.data.set("selected", gson.toJson(map));
    }

    public Optional<Boolean> getPreference(String preference) {
        Map<String, Boolean> preferences = this.getPreferences();
        if (preferences.containsKey(preference)) {
            return Optional.of(preferences.get(preference));
        } else {
            return Optional.empty();
        }
    }

    public void setPreference(String preference, boolean active) {
        Map<String, Boolean> preferences = this.getPreferences();
        preferences.put(preference, active);
        this.data.set("preferences", gson.toJson(preferences));
    }

    public Instant getCreated() {
        long created = (long) this.data.get("created").orElse(System.currentTimeMillis());
        return Instant.ofEpochMilli(created);
    }

    public Instant getLastLogin() {
        long lastlogin = (long) this.data.get("lastlogin").orElse(System.currentTimeMillis());
        return Instant.ofEpochMilli(lastlogin);
    }

    public List<Rank> getRanks() {
        return getRanks0(this.getPlayer());
    }

    private List<Rank> getRanks0(Player player) {
        List<Rank> ranks = new ArrayList<>();
        if (player != null) {
            ranks = Rank.getRanks().stream().filter(r -> player.hasPermission(r.getPermission())).collect(Collectors.toList());
        } else {
            String rank = (String) this.data.get("rank").orElse("");
            if (Rank.getRank(rank) != null) ranks.add(Rank.getRank(rank));
        }

        if (ranks.size() == 0) ranks.add(Rank.getRanks().get(Rank.getRanks().size() - 1));
        return ranks;
    }

    private static String nameToUUID(String uuid) {
        try {
            String newUuid = Mojang.getUUID(uuid);
            return newUuid;
        } catch (Exception e) {
            return Mojang.getOfflineUUID(uuid).toString();
        }
    }

    public static boolean hasProfile(String uuidOrName) {
        if (hasProfile0(nameToUUID(uuidOrName))) return true;
        return hasProfile0(uuidOrName);
    }

    private static boolean hasProfile0(String uuid) {
        if (Database.getCachedRows().stream().anyMatch(r -> 
        (r.getTable() instanceof ProfileTable) &&
        (r.get("uuid").get().equals(uuid)) )) return true;
        DataTable table = DataTable.getTable("vlDB_Profile").get();
        boolean rowPresent = Database.getDatabase().load(table, uuid).isPresent();
        return rowPresent;
    }

    private static Profile createOrLoadProfile0(String uuid, Player plr) {
        Player plr0 = Bukkit.getPlayer(UUID.fromString(uuid));
        if (plr0 != null) plr = plr0;
        String plrName = plr != null ? plr.getName() : "";
        Profile profile = new Profile(Database.getCachedRows().stream().filter(
            r ->
            (r.getTable() instanceof ProfileTable) &&
            (r.get("uuid").get().equals(uuid))
        ).findFirst().orElse(
            ((Supplier<DataRow>) () -> {
                DataTable table = DataTable.getTable("vlDB_Profile").get();
                DataRow row = Database.getDatabase().load(table, uuid).orElse(
                    new DataRow(table, uuid)
                );
                Database.cacheRow(row);
                return row;
            }).get()
        ), UUID.fromString(uuid));
        if (profile.getFake() == null) {
            if (!profile.getDName().equals(plrName)) profile.getData().set("name", plrName);
        }
        if (plr != null) {
            String rankName = profile.getRanks0(plr).get(0).getRankId();
            if (!profile.getData().get("rank").equals(rankName)) profile.getData().set("rank", rankName);
        }
        return profile;
    }

    public static Profile createOrLoadProfile(String uuidOrName) {
        return hasProfile(nameToUUID(uuidOrName)) ? createOrLoadProfile0(nameToUUID(uuidOrName), null) : createOrLoadProfile0(uuidOrName, null);
    }

    public static Profile createOrLoadProfile(UUID uuid) {
        return createOrLoadProfile0(uuid.toString(), null);
    }

    public static Profile createOrLoadProfile(Player player) {
        return createOrLoadProfile0(player.getUniqueId().toString(), player);
    }

    public static Profile loadProfile(String uuidOrName) {
        if (hasProfile(uuidOrName)) return createOrLoadProfile(uuidOrName);
        return null;
    }

    public static Profile loadProfile(UUID uuid) {
        return loadProfile(uuid.toString());
    }

    public static Profile loadProfile(Player player) {
        return loadProfile(player.getUniqueId());
    }
}