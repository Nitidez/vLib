package tech.nitidez.valarlibrary.lib.entity.npc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

import tech.nitidez.valarlibrary.lib.MinecraftVersion;
import tech.nitidez.valarlibrary.utils.Utils;

public final class NPCPackets {

    private static final int version = MinecraftVersion.getCurrentVersion().getCompareId();

    public static final Packet<NPC> NPC_ADD_INFO = (NPC container) -> {
        PacketContainer add = createPacket(PacketType.Play.Server.PLAYER_INFO);
        if (version >= 1192) {
            add.getPlayerInfoActions().write(0, EnumSet.of(EnumWrappers.PlayerInfoAction.ADD_PLAYER));
            add.getPlayerInfoDataLists().write(1, Collections.singletonList(container.getPlayerInfo()));
        } else {
            add.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
            add.getPlayerInfoDataLists().write(0, Collections.singletonList(container.getPlayerInfo()));
        }
        return add;
    };

    @SuppressWarnings("deprecation")
    public static final Packet<NPC> NPC_REMOVE_INFO = (NPC container) -> {
		PacketContainer removeInfo;
		if (version >= 1192) {
			removeInfo = createPacket(PacketType.Play.Server.PLAYER_INFO_REMOVE);
			removeInfo.getUUIDLists().write(0, Collections.singletonList(container.getPlayerID()));
		} else {
			removeInfo = createPacket(PacketType.Play.Server.PLAYER_INFO);
			removeInfo.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
			removeInfo.getPlayerInfoDataLists().write(0, Collections.singletonList(container.getPlayerInfo()));
		}
		return removeInfo;
    };

    public static final Packet<NPC> NPC_SPAWN = (NPC container) -> {
        Location npcLoc = container.getLocation();

        if (version >= 1202) {
            PacketContainer spawn = createPacket(PacketType.Play.Server.SPAWN_ENTITY);
            spawn.getIntegers().write(0, container.getEntityId());
            spawn.getUUIDs().write(0, container.getPlayerInfo().getProfileId());
            spawn.getEntityTypeModifier().write(0, EntityType.PLAYER);
            spawn.getDoubles()
                .write(0, npcLoc.getX())
                .write(1, npcLoc.getY())
                .write(2, npcLoc.getZ());
            
            spawn.getBytes()
                    .write(0, Utils.toByteAngle(npcLoc.getPitch()))
                    .write(1, Utils.toByteAngle(npcLoc.getYaw()))
                    .write(2, Utils.toByteAngle(npcLoc.getYaw()));

            return spawn;
        } else {
            PacketContainer spawn = createPacket(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
            spawn.getIntegers().write(0, container.getEntityId());
            spawn.getUUIDs().write(0, container.getPlayerInfo().getProfileId());
            if (version >= 191) {
                spawn.getDoubles()
                        .write(0, npcLoc.getX())
                        .write(1, npcLoc.getY())
                        .write(2, npcLoc.getZ());
            } else {
                spawn.getIntegers()
                        .write(1, Utils.get1_8LocInt(npcLoc.getX()))
                        .write(2, Utils.get1_8LocInt(npcLoc.getY()))
                        .write(3, Utils.get1_8LocInt(npcLoc.getZ()));
            }
            spawn.getBytes()
                    .write(0, Utils.toByteAngle(npcLoc.getYaw()))
                    .write(1, Utils.toByteAngle(npcLoc.getPitch()));
            return spawn;
        }
    };

    public static final Packet<NPC> NPC_UPDATE_METADATA = (NPC container) -> {
        PacketContainer meta = createPacket(PacketType.Play.Server.ENTITY_METADATA);
        meta.getIntegers().write(0, container.getEntityId());

        if (version >= 1192) {
            final List<WrappedDataValue> wrappedDataValueList = new ArrayList<>();
            wrappedDataValueList.add(new WrappedDataValue(
                    Utils.getSkinLayersByteIndex(),
                    WrappedDataWatcher.Registry.get(Byte.class),
                    container.getTrait().getSkinLayersByte()));

            meta.getDataValueCollectionModifier().write(0, wrappedDataValueList);
        } else {
            WrappedDataWatcher watcher = new WrappedDataWatcher();
            if (version >= 191) {
                watcher.setObject(
                        Utils.getSkinLayersByteIndex(),
                        WrappedDataWatcher.Registry.get(Byte.class),
                        container.getTrait().getSkinLayersByte());
            } else {
                watcher.setObject(
                        Utils.getSkinLayersByteIndex(),
                        container.getTrait().getSkinLayersByte());
            }

            meta.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
        }
        return meta;
    };

    public static final Packet<NPC> NPC_DESTROY = (NPC container) -> {
        PacketContainer removeEntities = createPacket(PacketType.Play.Server.ENTITY_DESTROY);
		List<Integer> ids = new ArrayList<Integer>();
		ids.add(container.getEntityId());

		if (version >= 1171) {
			removeEntities.getIntLists().write(0, ids);
		} else {
			removeEntities.getIntegerArrays().write(0, ids.stream().mapToInt(Integer::intValue).toArray());
		}
        return removeEntities;
    };

    public static final PacketList<NPC> NPC_RESET_HEAD_ROTATION = (NPC container) -> {
        byte yaw = Utils.toByteAngle(container.getLocation().getYaw());
        byte pitch = Utils.toByteAngle(container.getLocation().getPitch());
        return getHeadRotationPackets(container, yaw, pitch);

    };

    public static final Packet<NPC> NPC_PLAY_ANIMATION = (NPC container) -> {
        PacketContainer orient = createPacket(PacketType.Play.Server.ANIMATION);
        orient.getIntegers()
                .write(0, container.getEntityId())
                .write(1, 0);

        return orient;
    };

    public static final PacketList<List<String>> SCOREBOARD_CREATE_AND_ADD = (List<String> npcNames) -> {
        final LinkedHashSet<PacketContainer> scoreboardPackets = new LinkedHashSet<>();
        
		if (version >= 1171) {
			PacketContainer createTeam = createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
			createTeam.getStrings().write(0, NPCManager.NPC_SCOREBOARD_TEAM_NAME);
			InternalStructure struct = createTeam.getOptionalStructures().read(0).get();
			struct.getStrings()
				.write(0, "never")  // Visibility
				.write(1, "never"); // Collision
			createTeam.getOptionalStructures().write(0, Optional.of(struct));

			if (version >= 1181) {
				// Above 1.18, only one packet is needed, which includes
				// the team settings and NPC names in one.
				createTeam.getModifier().write(2, npcNames);
				scoreboardPackets.add(createTeam);
			} else {
				// For 1.17 servers, two packets are necessary, with
				// team settings and NPC names in separate packets.
				scoreboardPackets.add(createTeam);
				PacketContainer addNPCsToTeam = createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
				addNPCsToTeam.getStrings().write(0, NPCManager.NPC_SCOREBOARD_TEAM_NAME);
				addNPCsToTeam.getIntegers().write(0, 3); // Set packet mode to MODIFY_TEAM
				addNPCsToTeam.getModifier().write(2, npcNames);
				scoreboardPackets.add(addNPCsToTeam);
			}
		} else {
			// For servers prior to 1.17, a completely different packet
			// structure is used.
            PacketContainer createTeam = createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
            int teamSettingIndex = version >= 1131 ? 1 : 4;
            createTeam.getStrings()
                .write(0, NPCManager.NPC_SCOREBOARD_TEAM_NAME)
                .write(teamSettingIndex, "never");
            if (version >= 191) {
                createTeam.getStrings().write(teamSettingIndex + 1, "never");
            }
            scoreboardPackets.add(createTeam);

            PacketContainer addNPCsToTeam = createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
            addNPCsToTeam.getStrings().write(0, NPCManager.NPC_SCOREBOARD_TEAM_NAME);
            int teamPacketModeIndex = version >= 1131 ? 0 : 1;
            addNPCsToTeam.getIntegers().write(teamPacketModeIndex, 3);
            addNPCsToTeam.getModifier().write(version >= 191 ? 7 : 6,
                npcNames);
            scoreboardPackets.add(addNPCsToTeam);
		}

        return scoreboardPackets;
    };

    // DYNAMIC PACKETS

    public static LinkedHashSet<PacketContainer> getHeadRotationPackets(NPC container, byte yaw, byte pitch) {
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

    // UTILITY FUNCTIONS

    private static PacketContainer createPacket(PacketType type) {
        return ProtocolLibrary.getProtocolManager().createPacket(type);
    }

    // INTERFACES

    public interface Packet<T> {
        public PacketContainer get(T container);
    }

    public interface PacketList<T> {
        public LinkedHashSet<PacketContainer> get(T container);
    }
}
