package tech.nitidez.valarlibrary.libraries.entity.hologram;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import tech.nitidez.valarlibrary.libraries.entity.FakeEntity;

public class HologramLine extends FakeEntity {
    private String hologramText;
    private ItemStack hologramItem;
    private boolean itemLine;
    private boolean spaceLine;

    public HologramLine(Location location) {
        super(EntityType.ARMOR_STAND, location);
        hologramText = "";
        hologramItem = new ItemStack(Material.AIR);
        itemLine = false;
        spaceLine = true;
        this.addMetadata(0, (byte) 0x20);
        this.addMetadata(10, (byte) (0x01 | 0x08 | 0x10));
    }

    public HologramLine(Location location, String text) {
        super(EntityType.ARMOR_STAND, location);
        hologramText = text;
        hologramItem = new ItemStack(Material.AIR);
        itemLine = false;
        spaceLine = false;
        this.addMetadata(0, (byte) 0x20);
        this.addMetadata(2, text);
        this.addMetadata(3, (byte) 0x01);
        this.addMetadata(10, (byte) (0x01 | 0x08 | 0x10));
    }

    public HologramLine(Location location, ItemStack item) {
        super(EntityType.DROPPED_ITEM, location);
        hologramText = "";
        hologramItem = item;
        itemLine = true;
        spaceLine = false;
        this.addMetadata(7, item);
    }

    public String getText() {
        return hologramText;
    }

    public ItemStack getItem() {
        return hologramItem;
    }

    public boolean isItemLine() {
        return itemLine;
    }

    public boolean isSpaceLine() {
        return spaceLine;
    }
}
