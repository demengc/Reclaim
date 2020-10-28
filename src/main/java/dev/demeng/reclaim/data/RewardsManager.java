package dev.demeng.reclaim.data;

import dev.demeng.demlib.message.MessageUtils;
import dev.demeng.reclaim.Reclaim;
import dev.demeng.reclaim.util.ItemDeserializer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class RewardsManager {

  private final Reclaim i;
  @Getter private final Map<UUID, Map<String, Long>> cooldowns;

  public RewardsManager(Reclaim i) {

    this.i = i;
    this.cooldowns = new HashMap<>();

    final ConfigurationSection section = i.getData().getConfigurationSection("cooldowns");
    Objects.requireNonNull(section);

    for (String key : section.getKeys(false)) {

      final Map<String, Long> playerCooldowns = new HashMap<>();

      for (String id :
          Objects.requireNonNull(section.getConfigurationSection(key)).getKeys(false)) {
        playerCooldowns.put(id, i.getData().getLong("cooldowns." + key + "." + id));
      }

      cooldowns.put(UUID.fromString(key), playerCooldowns);
    }
  }

  public void setCooldown(UUID player, String id, long expiry) {

    for (Map.Entry<UUID, Map<String, Long>> entry : cooldowns.entrySet()) {

      if (entry.getKey().equals(player)) {

        final Map<String, Long> playerCooldowns = new HashMap<>(entry.getValue());

        playerCooldowns.put(id, expiry);
        cooldowns.put(entry.getKey(), playerCooldowns);

        return;
      }

      i.getData().set(player.toString() + "." + id, expiry);

      try {
        i.getDataFile().saveConfig();

      } catch (IOException ex) {
        MessageUtils.error(ex, "Failed to save data.", false);
      }
    }

    final Map<String, Long> playerCooldowns = new HashMap<>();
    playerCooldowns.put(id, expiry);

    cooldowns.put(player, playerCooldowns);
  }

  public void removeCooldown(UUID player, String id) {

    for (Map.Entry<UUID, Map<String, Long>> entry : cooldowns.entrySet()) {

      if (entry.getKey().equals(player)) {

        final Map<String, Long> playerCooldowns = new HashMap<>(entry.getValue());

        playerCooldowns.remove(id);
        cooldowns.put(entry.getKey(), playerCooldowns);

        return;
      }

      i.getData().set(player.toString() + "." + id, null);

      try {
        i.getDataFile().saveConfig();

      } catch (IOException ex) {
        MessageUtils.error(ex, "Failed to save data.", false);
      }
    }
  }

  public long getRemainingCooldown(UUID player, String id) {

    for (Map.Entry<UUID, Map<String, Long>> entry : cooldowns.entrySet()) {

      if (entry.getKey().equals(player)) {

        if (entry.getValue().containsKey(id)) {
          return entry.getValue().get(id) - System.currentTimeMillis();
        }
      }
    }

    return -1;
  }

  public void claim(Player player, String reward) {

    final ConfigurationSection section =
        i.getSettings().getConfigurationSection("rewards." + reward);
    Objects.requireNonNull(section);

    final String name = section.getString("name");
    Objects.requireNonNull(name);

    i.getManager()
        .setCooldown(
            player.getUniqueId(),
            reward,
            System.currentTimeMillis() + (section.getLong("cooldown") * 1000));

    final double money = section.getDouble("money");

    if (money != 0) {
      i.getEconomy().depositPlayer(player, money);
    }

    for (String key :
        Objects.requireNonNull(section.getConfigurationSection("items")).getKeys(false)) {
      player
          .getInventory()
          .addItem(
              ItemDeserializer.deserialize(
                  Objects.requireNonNull(section.getConfigurationSection("items." + key))));
    }

    for (String cmd : section.getStringList("commands")) {
      Bukkit.dispatchCommand(
          Bukkit.getConsoleSender(),
          cmd.replace("%player%", player.getName()).replace("%reward%", name));
    }

    MessageUtils.tell(
        player,
        Objects.requireNonNull(i.getMessages().getString("reward-claimed"))
            .replace("%reward%", name));
  }
}
