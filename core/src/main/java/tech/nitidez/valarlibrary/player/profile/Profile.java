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
        List<Rank> ranks = new ArrayList<>();
        if (this.getPlayer() != null) {
            Player player = this.getPlayer();
            ranks = Rank.getRanks().stream().filter(r -> player.hasPermission(r.getPermission())).collect(Collectors.toList());
        } else {
            String rank = (String) this.data.get("rank").orElse("");
            if (Rank.getRank(rank) != null) ranks.add(Rank.getRank(rank));
        }

        if (ranks.size() == 0) ranks.add(Rank.getRanks().get(Rank.getRanks().size() - 1));
        return ranks;
    }

    public static Profile loadProfile(String uuid) {
        return new Profile(Database.getCachedRows().stream().filter(
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
    }

    public static Profile loadProfile(UUID uuid) {
        return loadProfile(uuid.toString());
    }

    public static Profile loadProfile(Player player) {
        return loadProfile(player.getUniqueId());
    }
}
