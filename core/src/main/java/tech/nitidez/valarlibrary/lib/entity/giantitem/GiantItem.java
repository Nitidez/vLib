package tech.nitidez.valarlibrary.lib.entity.giantitem;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;

import tech.nitidez.valarlibrary.lib.entity.FakeEntity;
import tech.nitidez.valarlibrary.lib.entity.interactable.Interactable;

public class GiantItem extends FakeEntity {
    private ItemStack item;
    private boolean materialRotate;
    private Set<Interactable> interactables;

    private static Location giantLocation(Location loc) {
        loc = offsetLocation(loc.clone(), 2, -8, -3).clone();
        loc.setPitch(0f);
        return loc;
    }

    private static Location offsetLocation(Location loc, double distanceX, double distanceY, double distanceZ) {
        loc = loc.clone();
        double[] offset = calculateOffset(loc.getYaw(), distanceX, distanceZ);
        loc.add(offset[0], distanceY, offset[1]);
        return loc;
    }

    public GiantItem(ItemStack item, Location location, boolean materialRotate) {
        super(EntityType.GIANT, giantLocation(materialRotate ? new Location(location.getWorld(), location.getX(), location.getY(), location.getZ(), (location.getYaw()+90) % 360, 0f) : location));
        this.item = item;
        this.materialRotate = materialRotate;
        interactables = new HashSet<>();
        interactables.add(new Interactable(offsetLocation(this.getItemLocation(), 0.7, 0, 0.3), EntityType.IRON_GOLEM));
        interactables.add(new Interactable(offsetLocation(this.getItemLocation(), -0.7, 0, 0.3), EntityType.IRON_GOLEM));
        interactables.add(new Interactable(offsetLocation(this.getItemLocation(), 0.7, 1.8, 0.3), EntityType.IRON_GOLEM));
        interactables.add(new Interactable(offsetLocation(this.getItemLocation(), -0.7, 1.8, 0.3), EntityType.IRON_GOLEM));
    }

    public ItemStack getItem() {
        return this.item;
    }

    @Override
    public void move(Location moveLoc) {
        super.move(giantLocation(moveLoc));
    }

    @Override
    public void look(float yaw, float pitch) {
        super.look(yaw, pitch);
        this.move(getItemLocation());
    }

    public Location getItemLocation() {
        Location loc = this.getLocation().clone();
        loc.setPitch(0f);
        double[] offset = calculateOffset(loc.getYaw(), 2, -3);
        loc.add(-offset[0], 8, -offset[1]);
        if (materialRotate) loc.setYaw(((loc.getYaw() - 90) + 360) % 360);
        return loc;
    }

    @Override
    public Map<Integer, Object> getMetadata() {
        this.addMetadata(0, (byte) 0x20);
        return super.getMetadata();
    }

    @Override
    public FakeEntityInventory getEquipped() {
        FakeEntityInventory newInv = new FakeEntityInventory();
        newInv.set(ItemSlot.MAINHAND, this.item);
        return newInv;
    }

    public Set<Interactable> getInteractables() {
        return this.interactables;
    }

    @Override
    protected void spawnM(Player player) {
        super.spawnM(player);
        this.interactables.forEach(i -> i.spawn(player));
    }

    @Override
    protected void despawnM(Player player) {
        super.despawnM(player);
        this.interactables.forEach(i -> i.despawn(player));
    }

    private static double[] calculateOffset(float yawF, double distanceX, double distanceZ) {
        yawF = (yawF % 360 + 360) % 360;
        double radians = Math.toRadians(yawF);
        double offsetX = distanceX * Math.cos(radians) - distanceZ * Math.sin(radians);
        double offsetZ = distanceX * Math.sin(radians) + distanceZ * Math.cos(radians);
        return new double[]{offsetX, offsetZ};
    }

    public static Set<GiantItem> getGIs() {
        return FakeEntity.getEntities().stream().filter(GiantItem.class::isInstance).map(GiantItem.class::cast).collect(Collectors.toSet());
    }

    public static GiantItem getGI(Integer id) {
        return getGIs().stream().filter(gi -> gi.getEntityId() == id).findFirst().orElse(null);
    }
}
