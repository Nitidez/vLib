package tech.nitidez.valarlibrary.bungee;

import tech.nitidez.valarlibrary.bungee.cmd.Commands;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class Bungee extends Plugin {
  
  private static Bungee instance;
  public static HashMap<ProxiedPlayer, ProxiedPlayer> reply = new HashMap<ProxiedPlayer, ProxiedPlayer>();
  
  public Bungee() {
    instance = this;
  }
  
  public static Bungee getInstance() {
    return instance;
  }
 
  /**
   * Copia um arquivo a partir de um InputStream.
   *
   * @param input O input para ser copiado.
   * @param out   O arquivo destinario.
   */
  public static void copyFile(InputStream input, File out) {
    FileOutputStream ou = null;
    try {
      ou = new FileOutputStream(out);
      byte[] buff = new byte[1024];
      int len;
      while ((len = input.read(buff)) > 0) {
        ou.write(buff, 0, len);
      }
    } catch (IOException ex) {
      getInstance().getLogger().log(Level.WARNING, "Failed at copy file " + out.getName() + "!", ex);
    } finally {
      try {
        if (ou != null) {
          ou.close();
        }
        if (input != null) {
          input.close();
        }
      } catch (IOException ignore) {
      }
    }
  }
  
  @Override
  public void onEnable() {
    
    Commands.setupCommands();
    
    getProxy().registerChannel(getDescription().getName());
    
    this.getLogger().info("O plugin foi ativado.");
  }
  
  @Override
  public void onDisable() {
    this.getLogger().info("O plugin foi desativado.");
  }

}
