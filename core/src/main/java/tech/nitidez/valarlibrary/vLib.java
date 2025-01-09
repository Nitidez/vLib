package tech.nitidez.valarlibrary;

import org.bukkit.Bukkit;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import tech.nitidez.valarlibrary.commands.Commands;
import tech.nitidez.valarlibrary.listeners.Listeners;
import tech.nitidez.valarlibrary.plugin.ValarPlugin;

public class vLib extends ValarPlugin {

    public static boolean validInit;
    private static vLib instance;

    public static vLib getInstance() {
        return instance;
    }

    public static ProtocolManager getProtocolManager() {
        return ProtocolLibrary.getProtocolManager();
    }

    @Override
    public void start() {
        instance = this;
    }

    @Override
    public void load() {
        //
    }

    @Override
    public void enable() {
        saveDefaultConfig();

        if (Bukkit.getSpawnRadius() != 0) {
            Bukkit.setSpawnRadius(0);
        }
        Listeners.setupListeners();
        Commands.setupCommands();

        validInit = true;
        this.getLogger().info("O plugin foi ativado.");
    }

    @Override
    public void disable() {
        throw new UnsupportedOperationException("Unimplemented method 'disable'");
    }
}
