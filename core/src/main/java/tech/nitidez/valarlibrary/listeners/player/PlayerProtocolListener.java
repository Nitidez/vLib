package tech.nitidez.valarlibrary.listeners.player;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;

import tech.nitidez.valarlibrary.vLib;
import tech.nitidez.valarlibrary.libraries.MinecraftVersion;
import tech.nitidez.valarlibrary.libraries.entity.giantitem.GiantItem;
import tech.nitidez.valarlibrary.libraries.entity.interactable.Interactable;
import tech.nitidez.valarlibrary.libraries.entity.npc.NPC;
import tech.nitidez.valarlibrary.libraries.hologram.Hologram;

public class PlayerProtocolListener {
    private static void onNPCInteract(NPC npc, Player player, EntityUseAction action, Boolean secondary) {
        if (action.equals(EntityUseAction.INTERACT)) {
            player.sendMessage("interagiu com npc "+npc.getEntityId());
        }
    }

    private static void onGiantItemInteract(GiantItem gi, Player player, EntityUseAction action, Boolean secondary) {
        if (action.equals(EntityUseAction.INTERACT)) {
            player.sendMessage("interagiu com o gi "+gi.getEntityId());
        }
    }

    private static void onHologramInteract(Hologram holo, int index, Player player, EntityUseAction action, Boolean secondary) {
        if (action.equals(EntityUseAction.INTERACT)) {
            holo.touch(player);
        }
    }

    public static void registerPacketsListener() {
        ProtocolManager pm = vLib.getProtocolManager();
        int version = MinecraftVersion.getCurrentVersion().getCompareId();
        pm.addPacketListener(new PacketAdapter(vLib.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent evt) {
                PacketContainer packet = evt.getPacket().deepClone();
                Player p = evt.getPlayer();
                int id = packet.getIntegers().read(0);
                EntityUseAction action;
                if (version >= 1171) {
                    action = packet.getEnumEntityUseActions().read(0).getAction();
                } else {
                    action = packet.getEntityUseActions().read(0);
                }

                Boolean secondary = null;
                if (version >= 1161) {
                    secondary = packet.getBooleans().read(0);
                }

                if (NPC.getNPC(id) != null) {
                    onNPCInteract(NPC.getNPC(id), p, action, secondary);
                }

                if (Interactable.getInteractable(id) != null) {
                    Interactable interactable = Interactable.getInteractable(id);
                    GiantItem gi = GiantItem.getGIs().stream().filter(gi2 -> gi2.getInteractables().contains(interactable)).findFirst().orElse(null);
                    if (gi != null) {
                        onGiantItemInteract(gi, p, action, secondary);
                    }
                    Hologram holo = Hologram.getHolograms().stream().filter(h -> h.getLines().stream().anyMatch(hl -> (hl.isTouchable() ? hl.getTouchable().equals(interactable) : false))).findFirst().orElse(null);
                    if (holo != null) {
                        int index = (new ArrayList<>(holo.getLines())).indexOf(holo.getLines().stream().filter(hl -> hl.isTouchable() ? hl.getTouchable().equals(interactable) : false).findFirst().orElse(null));
                        onHologramInteract(holo, index, p, action, secondary);
                    }
                }
            }
        });
    }
}
