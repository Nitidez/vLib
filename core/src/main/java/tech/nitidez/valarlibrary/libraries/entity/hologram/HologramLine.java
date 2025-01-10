package tech.nitidez.valarlibrary.libraries.entity.hologram;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import tech.nitidez.valarlibrary.libraries.entity.FakeEntity;
import tech.nitidez.valarlibrary.libraries.entity.interactable.Interactable;

public class HologramLine extends FakeEntity {
    private String hologramText;
    private boolean spaceLine;
    private Interactable touchable;

    public HologramLine(Location location) {
        super(EntityType.ARMOR_STAND, location);
        hologramText = "";
        spaceLine = true;
        this.addMetadata(0, (byte) 0x20);
        this.addMetadata(10, (byte) (0x01 | 0x08 | 0x10));
    }

    public HologramLine(Location location, String text) {
        super(EntityType.ARMOR_STAND, location);
        hologramText = text;
        spaceLine = false;
        this.addMetadata(0, (byte) 0x20);
        this.addMetadata(2, text);
        this.addMetadata(3, (byte) 0x01);
        this.addMetadata(10, (byte) (0x01 | 0x08 | 0x10));
    }

    public String getText() {
        return hologramText;
    }

    public boolean isSpaceLine() {
        return spaceLine;
    }

    public boolean isTouchable() {
        return touchable != null;
    }

    public Interactable getTouchable() {
        return touchable;
    }

    public void setTouchable(boolean touchable) {
        if (touchable) {
            if (this.touchable == null) {
                this.touchable = new Interactable(getLocation(), EntityType.SLIME);
                for (Player p : this.getPlayers()) {
                    this.touchable.spawn(p);
                }
            }
        } else {
            if (this.touchable != null) {
                this.touchable.despawn();
                this.touchable.uncache();
                this.touchable = null;
            }
        }
    }

    @Override
    protected void spawnM(Player player) {
        if (touchable != null) {
            touchable.spawn(player);
        }
        super.spawnM(player);
    }

    @Override
    protected void despawnM(Player player) {
        if (touchable != null) {
            touchable.despawn(player);
        }
        super.despawnM(player);
    }
}
