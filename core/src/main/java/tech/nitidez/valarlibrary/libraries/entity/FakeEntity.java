package tech.nitidez.valarlibrary.libraries.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;

public class FakeEntity {
    public static Set<FakeEntity> fakeEntitySet = new HashSet<>();
    public static class FakeEntityInventory {
        private Map<ItemSlot, ItemStack> items;
        public FakeEntityInventory() {this.items = new HashMap<>();}
        public ItemStack get(ItemSlot itemSlot) {return items.get(itemSlot);}
        public Map<ItemSlot, ItemStack> get() {return items;}
        public void set(ItemSlot itemSlot, ItemStack item) {items.put(itemSlot, item);}
    }


    private int entityID;
    private UUID entityUUID;
    private EntityType entityType;
    private Location location;
    private Set<Player> playersShown;
    private FakeEntityInventory inventory;
    private Map<Integer, Object> meta;
    private Map<Integer, Serializer> metaSerializer;
    
    public FakeEntity(EntityType entityType, Location location) {
        this.entityID = EntityManager.atomicIncrementAndGetEntityId();
        this.entityUUID = UUID.randomUUID();
        this.entityType = entityType;
        this.location = location;
        this.playersShown = new HashSet<>();
        this.inventory = new FakeEntityInventory();
        this.meta = new HashMap<>();
        this.metaSerializer = new HashMap<>();
        fakeEntitySet.add(this);
    }

    public int getEntityId() {
        return this.entityID;
    }

    public UUID getEntityUUID() {
        return this.entityUUID;
    }

    public EntityType getEntityType() {
        return this.entityType;
    }

    public Location getLocation() {
        return this.location;
    }

    public FakeEntityInventory getEquipped() {
        return this.inventory;
    }

    public void setEquipped(FakeEntityInventory inventory) {
        this.inventory = inventory;
        refreshEquipped();
    }

    public void refreshEquipped() {
        for (Player p : this.getPlayers()) {
            EntityManager.entityEquipment(p, this);
        }
    }

    public Map<Integer, Object> getMetadata() {
        return this.meta;
    }

    public Map<Integer, Serializer> getSerializers() {
        return this.metaSerializer;
    }

    public Serializer getSerializer(Integer index) {
        if (this.metaSerializer.containsKey(index)) {
            return this.metaSerializer.get(index);
        } else {
            return WrappedDataWatcher.Registry.get(this.meta.get(index).getClass());
        }
    }

    public void move(Location moveLoc) {
        Location cL = this.location;
        this.location = new Location(moveLoc.getWorld(), moveLoc.getX(), moveLoc.getY(), moveLoc.getZ(), cL.getYaw(), cL.getPitch());
        refresh();
    }

    public void look(float yaw, float pitch) {
        Location cL = this.location;
        this.location = new Location(cL.getWorld(), cL.getX(), cL.getY(), cL.getZ(), yaw, pitch);
        for (Player p : this.getPlayers()) {
            EntityManager.entityHead(p, this);
        }
    }

    public void addMetadata(Integer index, Object value) {
        this.meta.put(index, value);
        updateMetadata();
    }

    public void removeMetadata(Integer index) {
        if (this.meta.containsKey(index)) {
            this.meta.remove(index);
            updateMetadata();
        }
    }

    public void setSerializer(Integer index, Serializer serializer) {
        this.metaSerializer.put(index, serializer);
    }

    private void updateMetadata() {
        for (Player p : this.getPlayers()) {
            EntityManager.loadEntityMetadata(p, this);
        }
    }

    public void clearMetadata() {
        this.meta.clear();
        updateMetadata();
    }

    public void spawn() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            spawn(p);
        }
    }

    public void spawn(Player... players) {
        for (Player player : players) {
            if (!this.getPlayers().contains(player)) {
                spawnM(player);
                addPlayer(player);
            }
        }
    }

    protected void spawnM(Player player) {
        EntityManager.loadEntity(player, this);
    }

    public void despawn() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            despawn(p);
        }
    }

    public void despawn(Player... players) {
        for (Player player : players) {
            if (this.getPlayers().contains(player)) {
                despawnM(player);
                removePlayer(player);
            }
        }
    }

    protected void despawnM(Player player) {
        EntityManager.unloadEntity(player, this);
    }

    public void refresh() {
        Set<Player> players = getPlayers();
        despawn();
        for (Player p : players) {
            spawn(p);
        }
    }

    private void refreshPlayers() {
        this.playersShown = Bukkit.getOnlinePlayers().stream().filter(p -> this.playersShown.contains(p)).collect(Collectors.toSet());
    }

    public Set<Player> getPlayers() {
        refreshPlayers();
        return this.playersShown;
    }

    private void addPlayer(Player plr) {
        if (!this.getPlayers().contains(plr)) this.playersShown.add(plr);
    }

    private void removePlayer(Player plr) {
        if (this.getPlayers().contains(plr)) this.playersShown.remove(plr);
    }

    public void uncache() {
        fakeEntitySet.remove(this);
    }

    public static Set<FakeEntity> getEntities() {
        return fakeEntitySet;
    }

    public static FakeEntity getEntity(int id) {
        return fakeEntitySet.stream().filter(e -> e.getEntityId() == id).findFirst().orElse(null);
    }
}
