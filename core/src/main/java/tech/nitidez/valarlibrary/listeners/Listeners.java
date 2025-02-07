package tech.nitidez.valarlibrary.listeners;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import tech.nitidez.valarlibrary.vLib;
import tech.nitidez.valarlibrary.listeners.player.PlayerProtocolListener;
import tech.nitidez.valarlibrary.listeners.player.PlayerServerListener;

public class Listeners {
    public static void setupListeners() {
        PlayerProtocolListener.registerPacketsListener();
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerServerListener(), vLib.getInstance());
    }
}
