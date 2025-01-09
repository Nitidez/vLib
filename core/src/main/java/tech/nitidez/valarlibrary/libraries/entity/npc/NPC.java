package tech.nitidez.valarlibrary.libraries.entity.npc;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;

import tech.nitidez.valarlibrary.libraries.entity.FakeEntity;
import tech.nitidez.valarlibrary.utils.Utils;

public class NPC extends FakeEntity {
    private UUID playerID;
    private NPCTrait npcTrait;
    private String npcName;

    public NPC(Location location) {
        super(EntityType.PLAYER, location);
        this.playerID = UUID.randomUUID();
        this.npcTrait = new NPCTrait();
        this.npcName = "§8[NPC]";
    }

    public UUID getPlayerID() {
        return this.playerID;
    }

    public NPCTrait getTrait() {
        return this.npcTrait;
    }

    @Override
    public Map<Integer, Object> getMetadata() {
        this.addMetadata(Utils.getSkinLayersByteIndex(), this.getTrait().getSkinLayersByte());
        return super.getMetadata();
    }

    public PlayerInfoData getPlayerInfo() {
        WrappedGameProfile profile = new WrappedGameProfile(this.playerID, npcName);
        profile.getProperties().put("textures", new WrappedSignedProperty("textures", npcTrait.getSkin().getTexture(), this.npcTrait.getSkin().getSignature()));
        return new PlayerInfoData(profile, 0, NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(npcName));
    }

    public void setTrait(NPCTrait trait) {
        this.npcTrait = trait;
    }

    @Override
    protected void spawnM(Player player) {
        NPCManager.loadNPC(this, player);
    }

    @Override
    protected void despawnM(Player player) {
        super.despawnM(player);
        NPCManager.unloadNPC(this, player);
    }

    public static Set<NPC> getNPCs() {
        return FakeEntity.getEntities().stream().filter(NPC.class::isInstance).map(NPC.class::cast).collect(Collectors.toSet());
    }

    public static NPC getNPC(int id) {
        return getNPCs().stream().filter(n -> n.getEntityId() == id).findFirst().orElse(null);
    }
}
