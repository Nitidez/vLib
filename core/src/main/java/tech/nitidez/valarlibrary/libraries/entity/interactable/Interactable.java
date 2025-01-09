package tech.nitidez.valarlibrary.libraries.entity.interactable;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;


import tech.nitidez.valarlibrary.libraries.entity.FakeEntity;

public class Interactable extends FakeEntity {
    public Interactable(Location location, EntityType entityType) {
        super(entityType, location);
        this.addMetadata(0, (byte) 0x20);
    }

    public static Set<Interactable> getInteractables() {
        return FakeEntity.getEntities().stream().filter(Interactable.class::isInstance).map(Interactable.class::cast).collect(Collectors.toSet());
    }

    public static Interactable getInteractable(Integer id) {
        return getInteractables().stream().filter(i -> i.getEntityId() == id).findFirst().orElse(null);
    }
    
}
