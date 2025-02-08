package tech.nitidez.valarlibrary.listeners.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import tech.nitidez.valarlibrary.vLib;
import tech.nitidez.valarlibrary.lib.entity.FakeEntity;

public class PlayerServerListener implements Listener {

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent evt) {
        Player plr = evt.getPlayer();
        Bukkit.getScheduler().runTaskLater(vLib.getInstance(), () -> {
            FakeEntity.getEntities().stream().filter(fe -> fe.getPlayers().contains(plr)).forEach(fe -> {
                fe.despawn(plr);
                fe.spawn(plr);
            });
        }, 20L);
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent evt) {
        Player plr = evt.getPlayer();
        FakeEntity.getEntities().stream().filter(fe -> fe.getPlayers().contains(plr)).forEach(fe -> {
            fe.despawn(plr);
            fe.spawn(plr);
        });
    }
    
}
