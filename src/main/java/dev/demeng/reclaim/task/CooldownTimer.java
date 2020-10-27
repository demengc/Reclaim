package dev.demeng.reclaim.task;

import dev.demeng.reclaim.Reclaim;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.UUID;

public class CooldownTimer implements Runnable {

  private final Reclaim i;

  public CooldownTimer(Reclaim i) {
    this.i = i;
    Bukkit.getScheduler().runTaskTimer(i, this, 20L, 20L);
  }

  @Override
  public void run() {

    for (Map.Entry<UUID, Map<String, Long>> entry : i.getManager().getCooldowns().entrySet()) {

      final Map<String, Long> playerCooldowns = entry.getValue();

      for (Map.Entry<String, Long> cooldown : playerCooldowns.entrySet()) {
        if (cooldown.getValue() <= System.currentTimeMillis()) {
          i.getManager().removeCooldown(entry.getKey(), cooldown.getKey());
        }
      }
    }
  }
}
