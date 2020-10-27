package dev.demeng.reclaim;

import dev.demeng.demlib.Common;
import dev.demeng.demlib.core.DemLib;
import dev.demeng.demlib.file.YamlFile;
import dev.demeng.demlib.message.MessageUtils;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class Reclaim extends JavaPlugin {

  @Getter private static Reclaim instance;

  @Getter private YamlFile settingsFile;
  @Getter private YamlFile messagesFile;

  @Override
  public void onEnable() {

    instance = this;
    DemLib.setPlugin(this);
    DemLib.setPrefix("&8[&6Reclaim&8] &r");

    getLogger().info("Loading config files...");
    if (!loadFiles()) {
      return;
    }

    MessageUtils.console("&aReclaim v" + Common.getVersion() + " has been successfully enabled.");
  }

  @Override
  public void onDisable() {
    MessageUtils.console("&cReclaim v" + Common.getVersion() + " has been successfully disabled.");
  }

  private boolean loadFiles() {

    try {
      settingsFile = new YamlFile("settings.yml");
      messagesFile = new YamlFile("messages.yml");
    } catch (Exception ex) {
      MessageUtils.error(ex, "Failed to load config files.", true);
      return false;
    }

    return true;
  }
}
