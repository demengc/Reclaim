package dev.demeng.reclaim;

import dev.demeng.demlib.Common;
import dev.demeng.demlib.Registerer;
import dev.demeng.demlib.command.CommandMessages;
import dev.demeng.demlib.core.DemLib;
import dev.demeng.demlib.file.YamlFile;
import dev.demeng.demlib.message.MessageUtils;
import dev.demeng.reclaim.command.command.ReclaimCmd;
import dev.demeng.reclaim.data.RewardsManager;
import dev.demeng.reclaim.task.CooldownTimer;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Reclaim extends JavaPlugin {

  @Getter private static Reclaim instance;

  @Getter private YamlFile settingsFile;
  @Getter private YamlFile messagesFile;
  @Getter private YamlFile dataFile;

  @Getter private RewardsManager manager;

  @Getter private Economy economy;

  @Override
  public void onEnable() {

    instance = this;
    DemLib.setPlugin(this);
    DemLib.setPrefix("&8[&6Reclaim&8] &r");

    getLogger().info("Loading files...");
    if (!loadFiles()) {
      return;
    }

    MessageUtils.setPrefix(getMessages().getString("prefix"));

    getLogger().info("Loading data...");
    manager = new RewardsManager(this);

    getLogger().info("Hooking into Vault...");
    if (!setupEconomy()) {
      MessageUtils.error(null, "Failed to hook into Vault.", true);
      return;
    }

    getLogger().info("Registering commands...");
    DemLib.setCommandMessages(new CommandMessages(getMessages()));

    try {
      Registerer.registerCommand(new ReclaimCmd(this));

    } catch (NoSuchFieldException | IllegalAccessException ex) {
      MessageUtils.error(ex, "Failed to register commands.", true);
      return;
    }

    getLogger().info("Starting tasks...");
    new CooldownTimer(this);

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
      dataFile = new YamlFile("data.yml");
    } catch (Exception ex) {
      MessageUtils.error(ex, "Failed to load config files.", true);
      return false;
    }

    return true;
  }

  private boolean setupEconomy() {

    if (getServer().getPluginManager().getPlugin("Vault") == null) {
      return false;
    }

    final RegisteredServiceProvider<Economy> rsp =
        getServer().getServicesManager().getRegistration(Economy.class);

    if (rsp == null) {
      return false;
    }

    economy = rsp.getProvider();
    return true;
  }

  public FileConfiguration getSettings() {
    return settingsFile.getConfig();
  }

  public FileConfiguration getMessages() {
    return messagesFile.getConfig();
  }

  public FileConfiguration getData() {
    return dataFile.getConfig();
  }
}
