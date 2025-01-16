package tech.nitidez.valarlibrary;

import tech.nitidez.valarlibrary.reflection.Accessors;
import tech.nitidez.valarlibrary.reflection.acessors.MethodAccessor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class Manager {
  
  public static boolean BUNGEE;
  
  private static Object PROXY_SERVER;
  
  private static MethodAccessor GET_NAME;
  private static MethodAccessor GET_PLAYER;
  private static MethodAccessor GET_SPIGOT;
  private static MethodAccessor HAS_PERMISSION;
  private static MethodAccessor SEND_MESSAGE;
  private static MethodAccessor SEND_MESSAGE_COMPONENTS;
  
  static {
    try {
      Class<?> proxyServer = Class.forName("net.md_5.bungee.api.ProxyServer");
      Class<?> proxiedPlayer = Class.forName("net.md_5.bungee.api.connection.ProxiedPlayer");
      PROXY_SERVER = Accessors.getMethod(proxyServer, "getInstance").invoke(null);
      GET_NAME = Accessors.getMethod(proxiedPlayer, "getName");
      GET_PLAYER = Accessors.getMethod(proxyServer, "getPlayer", String.class);
      HAS_PERMISSION = Accessors.getMethod(proxiedPlayer, "hasPermission", String.class);
      SEND_MESSAGE_COMPONENTS = Accessors.getMethod(proxiedPlayer, "sendMessage", BaseComponent[].class);
      BUNGEE = true;
    } catch (ClassNotFoundException ignore) {
      try {
        Class<?> player = Class.forName("org.bukkit.entity.Player");
        Class<?> spigot = Class.forName("org.bukkit.entity.Player$Spigot");
        GET_NAME = Accessors.getMethod(player, "getName");
        GET_PLAYER = Accessors.getMethod(Class.forName("tech.nitidez.valarlibrary.player.Profile"), "findCached", String.class);
        HAS_PERMISSION = Accessors.getMethod(player, "hasPermission", String.class);
        SEND_MESSAGE = Accessors.getMethod(player, "sendMessage", String.class);
        GET_SPIGOT = Accessors.getMethod(player, "spigot");
        SEND_MESSAGE_COMPONENTS = Accessors.getMethod(spigot, "sendMessage", BaseComponent[].class);
      } catch (ClassNotFoundException ex) {
        ex.printStackTrace();
      }
    }
  }
  

  
  public static void sendMessage(Object player, String message) {
    if (BUNGEE) {
      sendMessage(player, TextComponent.fromLegacyText(message));
      return;
    }
    
    SEND_MESSAGE.invoke(player, message);
  }
  
  public static void sendMessage(Object player, BaseComponent... components) {
    SEND_MESSAGE_COMPONENTS.invoke(BUNGEE ? player : GET_SPIGOT.invoke(player), new Object[]{components});
  }
  
  public static String getName(Object player) {
    return (String) GET_NAME.invoke(player);
  }
  
  public static Object getPlayer(String name) {
    return GET_PLAYER.invoke(BUNGEE ? PROXY_SERVER : null, name);
  }
  
  public static boolean hasPermission(Object player, String permission) {
    return (boolean) HAS_PERMISSION.invoke(player, permission);
  }
}
