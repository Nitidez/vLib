package tech.nitidez.valarlibrary.libraries.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;

import tech.nitidez.valarlibrary.libraries.MinecraftVersion;
import tech.nitidez.valarlibrary.utils.Utils;

public class FakeEntityPackets {
    private static int version = MinecraftVersion.getCurrentVersion().getCompareId();

    @SuppressWarnings("deprecation")
    public static final Packet<FakeEntity> FE_SPAWN = (FakeEntity container) -> {
        PacketContainer packet;
        Location loc = container.getLocation();
        EntityType eType = container.getEntityType();
        if (version >= 1190) {
            packet = createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        } else {
            packet = createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
            packet.getIntegers()
            .write(1, (int) eType.getTypeId())
            .write(2, Utils.get1_8LocInt(loc.getX()))
            .write(3, Utils.get1_8LocInt(loc.getY()))
            .write(4, Utils.get1_8LocInt(loc.getZ()));
        }
        packet.getIntegers().write(0, container.getEntityId());
        return packet;
    };

    public static final Packet<FakeEntity> FE_DESTROY = (FakeEntity container) -> {
        PacketContainer packet = createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        List<Integer> ids = new ArrayList<Integer>();
        ids.add(container.getEntityId());

        if (version >= 1171) {
            packet.getIntLists().write(0, ids);
        } else {
            packet.getIntegerArrays().write(0, ids.stream().mapToInt(Integer::intValue).toArray());
        }
        return packet;
    };

    public static final Packet<FakeEntity> FE_METADATA = (FakeEntity container) -> {
        PacketContainer packet = createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, container.getEntityId());
        if (version >= 1192) {
            final List<WrappedDataValue> wrappedDataValueList = container.getMetadata().entrySet().stream()
            .map(entry -> new WrappedDataValue(entry.getKey(), container.getSerializer(entry.getKey()), entry.getValue()))
            .collect(Collectors.toList());
            packet.getDataValueCollectionModifier().write(0, wrappedDataValueList);
        } else {
            WrappedDataWatcher watcher = new WrappedDataWatcher();
            container.getMetadata().forEach((index, value) -> {
                if (version >= 191) {
                    watcher.setObject(index, container.getSerializer(index), value);
                } else {
                    watcher.setObject(index, value);
                }
            });
            packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
        }
        return packet;
    };

    public static final PacketList<FakeEntity> FE_SET_EQUIPMENT = (FakeEntity container) -> {
        Map<ItemSlot, ItemStack> equipped = container.getEquipped().get();
        List<Pair<ItemSlot, ItemStack>> pairList = Arrays.stream(ItemSlot.values()).filter(equipped::containsKey).map(slot -> new Pair<>(slot, equipped.get(slot))).collect(Collectors.toList());
        if (pairList.isEmpty()) return new LinkedHashSet<>();

        final LinkedHashSet<PacketContainer> packets = new LinkedHashSet<>();
        if (version >= 1160) {
            PacketContainer packet = createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
            packet.getIntegers().write(0, container.getEntityId());
            packet.getSlotStackPairLists().write(0, pairList);
            packets.add(packet);
        } else {
            for (Pair<ItemSlot, ItemStack> pair : pairList) {
                PacketContainer packet = createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
                packet.getIntegers().write(0, container.getEntityId());
                if (version >= 190) {
                    packet.getItemSlots().write(0, pair.getFirst());
                } else {
                    int equipmentSlot;
                    switch (pair.getFirst()) {
                        case MAINHAND: equipmentSlot = 0; break;
                        case FEET: equipmentSlot = 1; break;
                        case LEGS: equipmentSlot = 2; break;
                        case CHEST: equipmentSlot = 3; break;
                        case HEAD: equipmentSlot = 4; break;
                        default: continue;
                    }
                    packet.getIntegers().write(1, equipmentSlot);
                }
                packet.getItemModifier().write(0, pair.getSecond());
                packets.add(packet);
            }
        }

        return packets;
    };

    public static final PacketList<FakeEntity> FE_RESET_HEAD_ROTATION = (FakeEntity container) -> {
        byte yaw = Utils.toByteAngle(container.getLocation().getYaw());
        byte pitch = Utils.toByteAngle(container.getLocation().getPitch());
        return getHeadRotationPackets(container, yaw, pitch);
    };

    public static LinkedHashSet<PacketContainer> getHeadRotationPackets(FakeEntity container, byte yaw, byte pitch) {
        final LinkedHashSet<PacketContainer> packets = new LinkedHashSet<>();
        PacketContainer rotate = createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        rotate.getIntegers().write(0, container.getEntityId());
        rotate.getBytes().write(0, yaw);

        PacketContainer look = createPacket(PacketType.Play.Server.ENTITY_LOOK);
        look.getIntegers().write(0, container.getEntityId());
        int yawIndex = version >= 191 ? 0 : 3;
        look.getBytes()
            .write(yawIndex, yaw)
            .write(yawIndex + 1, pitch);
        look.getBooleans()
            .write(0, true)
            .write(1, true);
        packets.add(rotate);
        packets.add(look);

        return packets;
    }

    public static final Packet<FakeEntity> FE_PLAY_ANIMATION = (FakeEntity container) -> {
        PacketContainer orient = createPacket(PacketType.Play.Server.ANIMATION);
        orient.getIntegers()
            .write(0, container.getEntityId())
            .write(1, 0);

        return orient;
    };

    private static PacketContainer createPacket(PacketType packetType) {
        return ProtocolLibrary.getProtocolManager().createPacket(packetType);
    }

    public interface Packet<T> {
        public PacketContainer get(T container);
    }

    public interface PacketList<T> {
        public LinkedHashSet<PacketContainer> get(T container);
    }
}
