package tech.nitidez.valarlibrary.plugin;

import tech.nitidez.valarlibrary.plugin.config.FileUtils;
import tech.nitidez.valarlibrary.plugin.config.ValarConfig;
import tech.nitidez.valarlibrary.plugin.config.ValarWriter;
import tech.nitidez.valarlibrary.plugin.logger.ValarLogger;
import tech.nitidez.valarlibrary.reflection.Accessors;
import tech.nitidez.valarlibrary.reflection.acessors.FieldAccessor;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public abstract class ValarPlugin extends JavaPlugin {
  
  private static final FieldAccessor<PluginLogger> LOGGER_ACCESSOR = Accessors.getField(JavaPlugin.class, "logger", PluginLogger.class);
  private final FileUtils fileUtils;
  
  public ValarPlugin() {
    this.fileUtils = new FileUtils(this);
    LOGGER_ACCESSOR.set(this, new ValarLogger(this));
    
    this.start();
  }
  
  public abstract void start();
  
  public abstract void load();
  
  public abstract void enable();
  
  public abstract void disable();
  
  @Override
  public void onLoad() {
    this.load();
  }
  
  @Override
  public void onEnable() {
    this.enable();
  }
  
  @Override
  public void onDisable() {
    this.disable();
  }
  
  public ValarConfig getConfig(String name) {
    return this.getConfig("", name);
  }
  
  public ValarConfig getConfig(String path, String name) {
    return ValarConfig.getConfig(this, "plugins/" + this.getName() + "/" + path, name);
  }
  
  public ValarWriter getWriter(File file) {
    return this.getWriter(file, "");
  }
  
  public ValarWriter getWriter(File file, String header) {
    return new ValarWriter((ValarLogger) this.getLogger(), file, header);
  }
  
  public FileUtils getFileUtils() {
    return this.fileUtils;
  }
}
