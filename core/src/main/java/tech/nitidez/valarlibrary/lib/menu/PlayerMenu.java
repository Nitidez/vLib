package tech.nitidez.valarlibrary.lib.menu;

import tech.nitidez.valarlibrary.plugin.ValarPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class PlayerMenu extends Menu implements Listener {
  
  protected Player player;
  
  public PlayerMenu(Player player, String title) {
    this(player, title, 3);
  }
  
  public PlayerMenu(Player player, String title, int rows) {
    super(title, rows);
    this.player = player;
  }
  
  public void register(ValarPlugin plugin) {
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }
  
  public void open() {
    this.player.openInventory(getInventory());
  }
  
  public Player getPlayer() {
    return player;
  }
}
