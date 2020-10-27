package dev.demeng.reclaim.util;

import com.cryptomorin.xseries.XEnchantment;
import dev.demeng.demlib.item.ItemCreator;
import dev.demeng.demlib.message.MessageUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemDeserializer {

  public static ItemStack deserialize(ConfigurationSection section) {

    final ItemStack stack = ItemCreator.getMaterial(section.getName());

    final ItemMeta meta = stack.getItemMeta();

    Objects.requireNonNull(meta)
        .setDisplayName(MessageUtils.colorize(Objects.requireNonNull(section.getString("name"))));

    final List<String> lore = new ArrayList<>();
    for (String line : section.getStringList("lore")) {
      lore.add(MessageUtils.colorize(line));
    }

    meta.setLore(lore);

    final Map<Enchantment, Integer> enchants = new HashMap<>();

    for (String key :
        Objects.requireNonNull(section.getConfigurationSection("enchants")).getKeys(false)) {

      final XEnchantment enchant = XEnchantment.matchXEnchantment(key).orElse(null);

      if (enchant == null) {
        MessageUtils.error(null, "Invalid enchantment: " + key, false);
        continue;
      }

      enchants.put(enchant.parseEnchantment(), section.getInt("enchants." + key));
    }

    for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
      meta.addEnchant(entry.getKey(), entry.getValue(), true);
    }

    stack.setItemMeta(meta);
    return stack;
  }
}
