package tech.nitidez.valarlibrary.listeners.player;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import com.mojang.authlib.GameProfile;

import tech.nitidez.valarlibrary.vLib;
import tech.nitidez.valarlibrary.data.Database;
import tech.nitidez.valarlibrary.lib.entity.FakeEntity;
import tech.nitidez.valarlibrary.player.profile.Profile;

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

    /*@EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent evt) {
        if (evt.getLoginResult() == Result.ALLOWED) {
            if (Profile.hasProfile(evt.getUniqueId().toString())) {
                Profile p = Profile.loadProfile(evt.getUniqueId());
                if (p.getFake() != null) {
                    String fakeName = p.getFake();
                    try {
                        Field nameField = evt.getClass().getDeclaredField("playerName");
                        nameField.setAccessible(true);
                        nameField.set(evt, fakeName);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }*/

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent evt) {
        Player plr = evt.getPlayer();
        if (evt.getResult() == Result.ALLOWED) {
            Profile p = Profile.loadProfile(plr);
            if (p != null) {
                Database.uncacheRow(p.getData());
                p = Profile.createOrLoadProfile(plr);
                if (p.getFake() != null) {
                    String fakeName = p.getFake();
                    try {
                        Object playerHandle = plr.getClass().getMethod("getHandle").invoke(plr);
                        Field profileField = Arrays.asList(playerHandle.getClass().getSuperclass().getDeclaredFields()).stream().filter(f -> f.getType().equals(GameProfile.class)).findFirst().get();
                        profileField.setAccessible(true);
                        GameProfile oldProfile = (GameProfile) profileField.get(playerHandle);
                        GameProfile newProfile = new GameProfile(plr.getUniqueId(), fakeName);
                        newProfile.getProperties().putAll(oldProfile.getProperties());
                        profileField.set(playerHandle, newProfile);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent evt) {
        Profile p = Profile.loadProfile(evt.getPlayer());
        if (p != null) {
            Database.getDatabase().save(p.getData().getTable());
            Database.uncacheRow(p.getData());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) {
        Profile.createOrLoadProfile(evt.getPlayer());
    }
    
}
