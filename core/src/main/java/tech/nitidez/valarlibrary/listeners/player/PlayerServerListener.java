package tech.nitidez.valarlibrary.listeners.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import tech.nitidez.valarlibrary.lib.entity.FakeEntity;

public class PlayerServerListener implements Listener {
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent evt) {
        Player plr = evt.getEntity();
        FakeEntity.getEntities().stream().filter(fe -> fe.getPlayers().contains(plr)).forEach(fe -> {
            fe.despawn(plr);
            fe.spawn(plr);
        });
    }
}
