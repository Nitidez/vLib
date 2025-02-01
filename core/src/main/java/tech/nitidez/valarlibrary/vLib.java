package tech.nitidez.valarlibrary;

import org.bukkit.Bukkit;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import tech.nitidez.valarlibrary.commands.Commands;
import tech.nitidez.valarlibrary.database.Database;
import tech.nitidez.valarlibrary.database.data.DataTable;
import tech.nitidez.valarlibrary.database.tables.ProfileTable;
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

        DataTable.TABLES.add(new ProfileTable());
        Database.setupDatabase(
            getConfig().getString("database.type").toUpperCase(),
            getConfig().getBoolean("database.mysql.mariadb"),
            getConfig().getString("database.mysql.hostname"),
            getConfig().getString("database.mysql.port"),
            getConfig().getString("database.mysql.name"),
            getConfig().getString("database.mysql.user"),
            getConfig().getString("database.mysql.password")
        );

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
