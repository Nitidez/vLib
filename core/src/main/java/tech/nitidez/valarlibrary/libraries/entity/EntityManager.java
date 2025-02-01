package tech.nitidez.valarlibrary.libraries.entity;

import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.entity.Player;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;

import tech.nitidez.valarlibrary.vLib;
import tech.nitidez.valarlibrary.libraries.MinecraftVersion;

public class EntityManager {
    private static ProtocolManager pm = vLib.getProtocolManager();
    private static int version = MinecraftVersion.getCurrentVersion().getCompareId();
    private static AtomicInteger atomicEntityId = new AtomicInteger(6000);

    public static int atomicIncrementAndGetEntityId() {
        return atomicEntityId.incrementAndGet();
    }

    public static int atomicEntityId() {
        return atomicEntityId.get();
    }

    public static void atomicResetEntityId() {
        atomicEntityId.set(6000);
    }

    public static void atomicSetEntityId(int amount) {
        atomicEntityId.set(amount < 6000 ? 6000 : amount);
    }

    public static void loadEntity(Player player, FakeEntity container) {
        PacketContainer packet = FakeEntityPackets.FE_SPAWN.get(container);
        pm.sendServerPacket(player, packet);
        loadEntityMetadata(player, container);
        entityEquipment(player, container);

        if (!(version >= 1202)) {
            entityHead(player, container);
        }
    }

    public static void loadEntityMetadata(Player player, FakeEntity container) {
        PacketContainer packet = FakeEntityPackets.FE_METADATA.get(container);
        pm.sendServerPacket(player, packet);
    }

    public static void teleportEntity(Player player, FakeEntity container) {
        PacketContainer packet = FakeEntityPackets.FE_TELEPORT.get(container);
        pm.sendServerPacket(player, packet);
    }

    public static void unloadEntity(Player player, FakeEntity container) {
        PacketContainer packet = FakeEntityPackets.FE_DESTROY.get(container);
        pm.sendServerPacket(player, packet);
    }

    public static void entityHead(Player player, FakeEntity container) {
        for (PacketContainer packet : FakeEntityPackets.FE_RESET_HEAD_ROTATION.get(container)) {
            pm.sendServerPacket(player, packet);
        }
    }

    public static void entityEquipment(Player player, FakeEntity container) {
        for (PacketContainer packet : FakeEntityPackets.FE_SET_EQUIPMENT.get(container)) {
            pm.sendServerPacket(player, packet);
        }
    }
}
