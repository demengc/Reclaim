package dev.demeng.reclaim.menu;

import dev.demeng.demlib.item.ItemCreator;
import dev.demeng.demlib.menu.Menu;
import dev.demeng.demlib.time.TimeFormatter;
import dev.demeng.reclaim.Reclaim;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReclaimMenu extends Menu {

  private final Reclaim i;

  public ReclaimMenu(Reclaim i, Player player) {
    super(
        i.getSettings().getInt("gui.size"),
        Objects.requireNonNull(i.getSettings().getString("gui.title")));

    this.i = i;

    for (String reward :
        Objects.requireNonNull(i.getSettings().getConfigurationSection("rewards")).getKeys(false)) {

      final ConfigurationSection section =
          i.getSettings().getConfigurationSection("rewards." + reward);
      Objects.requireNonNull(section);

      final String name = section.getString("name");
      final String permission = section.getString("permission");

      final int slot = section.getInt("slot") - 1;

      if (!player.hasPermission(Objects.requireNonNull(permission))) {
        setItem(slot, getDummy(name, -1, true), null);
        continue;
      }

      final long remaining = i.getManager().getRemainingCooldown(player.getUniqueId(), reward);

      if (remaining != -1) {
        setItem(slot, getDummy(name, remaining, false), event -> open(player));
        continue;
      }

      setItem(
          slot,
          ItemCreator.quickBuild(
              ItemCreator.getMaterial(Objects.requireNonNull(section.getString("material"))),
              Objects.requireNonNull(name),
              section.getStringList("lore")),
          event -> {
            final long newRemaining =
                i.getManager().getRemainingCooldown(player.getUniqueId(), reward);

            if (newRemaining != -1) {
              setItem(slot, getDummy(name, newRemaining, false), null);
              return;
            }

            i.getManager().claim(player, reward);
            player.closeInventory();
          });
    }

    open(player);
  }

  private ItemStack getDummy(String id, long remaining, boolean perm) {

    final ConfigurationSection section =
        i.getSettings().getConfigurationSection("gui.dummy-button");
    Objects.requireNonNull(section);

    final ItemStack stack =
        ItemCreator.getMaterial(Objects.requireNonNull(section.getString("material")));
    final String name = Objects.requireNonNull(section.getString("name")).replace("%reward%", id);
    final List<String> lore = new ArrayList<>();

    if (perm) {
      lore.addAll(section.getStringList("permission-lore"));

    } else {

      for (String line : section.getStringList("cooldown-lore")) {
        lore.add(line.replace("%remaining%", new TimeFormatter(remaining / 1000).format()));
      }
    }

    return ItemCreator.quickBuild(stack, name, lore);
  }
}
