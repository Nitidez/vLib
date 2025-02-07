package tech.nitidez.valarlibrary.lib.entity.npc;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.events.PacketContainer;

import tech.nitidez.valarlibrary.vLib;
import tech.nitidez.valarlibrary.lib.MinecraftVersion;
import tech.nitidez.valarlibrary.lib.entity.FakeEntityPackets;

public class NPCManager {
    public static final String NPC_SCOREBOARD_TEAM_NAME = "zzzzzzzzzzNMNPCs";
    public static final int version = MinecraftVersion.getCurrentVersion().getCompareId();
    private static final LinkedHashSet<PacketContainer> scoreboardPackets = new LinkedHashSet<>();
    

    public static List<PacketContainer> loadPackets(NPC npc) {
        List<PacketContainer> loadPackets = new ArrayList<PacketContainer>();
        loadPackets.add(NPCPackets.NPC_ADD_INFO.get(npc));
        loadPackets.add(NPCPackets.NPC_SPAWN.get(npc));
        loadPackets.add(FakeEntityPackets.FE_METADATA.get(npc));

        if (!(version >= 1202)) {
            loadPackets.addAll(FakeEntityPackets.FE_RESET_HEAD_ROTATION.get(npc));
        }
        return loadPackets;
    }

    private static void updateScoreboardPackets() {
        scoreboardPackets.clear();
        List<String> npcNames = NPC.getNPCs().stream().map(npc -> npc.getPlayerInfo().getProfile().getName())
        .collect(Collectors.toList());
        scoreboardPackets.addAll(NPCPackets.SCOREBOARD_CREATE_AND_ADD.get(npcNames));
    }

    public static void sendScoreboardPackets(Player receiver) {
        for (PacketContainer packet : scoreboardPackets) {
            vLib.getProtocolManager().sendServerPacket(receiver, packet);
        }
    }

    public static void updateAndSendScoreboardPackets() {
        updateScoreboardPackets();
        for (Player p : Bukkit.getOnlinePlayers()) {
            sendScoreboardPackets(p);
        }
    }

    public static void loadNPC(NPC npc, Player player) {
        for (PacketContainer pc : loadPackets(npc)) {
            vLib.getProtocolManager().sendServerPacket(player, pc);
        }

        if (!npc.getTrait().isTablist()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(vLib.getInstance(), new Runnable() {
                @Override
                public void run() {
                    PacketContainer rI = NPCPackets.NPC_REMOVE_INFO.get(npc);
                    vLib.getProtocolManager().sendServerPacket(player, rI);
                }
            }, 60);
        }
        updateAndSendScoreboardPackets();
    }

    public static void unloadNPC(NPC npc, Player player) {
        vLib.getProtocolManager().sendServerPacket(player, NPCPackets.NPC_REMOVE_INFO.get(npc));
    }

    
}
