package dev.demeng.reclaim.data;

import dev.demeng.reclaim.Reclaim;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class RewardsManager {

  private final Map<UUID, Map<String, Long>> cooldowns;

  public RewardsManager(Reclaim i) {

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
    }

    final Map<String, Long> playerCooldowns = new HashMap<>();
    playerCooldowns.put(id, expiry);

    cooldowns.put(player, playerCooldowns);
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
}
