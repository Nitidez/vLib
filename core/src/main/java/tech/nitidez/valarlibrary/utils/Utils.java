package tech.nitidez.valarlibrary.utils;

import org.bukkit.Location;

import tech.nitidez.valarlibrary.lib.MinecraftVersion;

/**
 * Classe com utilitários diversos sem nicho predefinido.
 */
public class Utils {
  
  public static float clampYaw(float yaw) {
    while (yaw < -180.0F) {
      yaw += 360.0F;
    }
    while (yaw >= 180.0F) {
      yaw -= 360.0F;
    }
    
    return yaw;
  }
  
  /**
   * Verifica se uma localização tem a chunk carregada no mundo.
   *
   * @param location localização para verificar
   * @return TRUE caso a localização tenha a chunk carregada, FALSE caso não tenha.
   */
  public static boolean isLoaded(Location location) {
    if (location == null || location.getWorld() == null) {
      return false;
    }
    
    int chunkX = location.getBlockX() >> 4;
    int chunkZ = location.getBlockZ() >> 4;
    return location.getWorld().isChunkLoaded(chunkX, chunkZ);
  }

  public static int get1_8LocInt(double param) {
    double paramTranslated = param * 32.0D;
    int i = (int) paramTranslated;
    return (paramTranslated < i) ? (i - 1) : i;
  }

  public static byte toByteAngle(float angle) {
    return (byte) (angle * 256.0F / 360.0F);
  }

  public static byte toByteAngle(double angle) {
    return (byte) (angle * 256.0D / 360.0D);
  }

  public static int getSkinLayersByteIndex() {
    int version = MinecraftVersion.getCurrentVersion().getCompareId();
    if (version >= 1171) {
      return 17;
    } else if (version >= 1151) {
      return 16;
    } else if (version >= 1141) {
      return 15;
    } else if (version >= 1101) {
      return 13;
    } else if (version >= 191) {
      return 12;
    } else {
      return 10;
    }
  }
}
