package dev.demeng.reclaim.command;

import dev.demeng.demlib.command.CustomCommand;
import dev.demeng.demlib.time.TimeFormatter;
import dev.demeng.reclaim.Reclaim;
import dev.demeng.reclaim.menu.ReclaimMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Objects;

public class ReclaimCmd extends CustomCommand {

  private final Reclaim i;

  public ReclaimCmd(Reclaim i) {
    super("reclaim", true, null, 0, "[reward]");
    this.i = i;
    setDescription("Base command for Reclaim plugin.");
  }

  @Override
  protected void run(CommandSender sender, String[] args) {

    final Player p = (Player) sender;

    if (args.length == 1) {

      if (!Objects.requireNonNull(i.getSettings().getConfigurationSection("rewards"))
          .contains(args[0])) {
        returnTell(
            Objects.requireNonNull(i.getMessages().getString("invalid-reward"))
                .replace("%reward%", args[0]));
      }

      final ConfigurationSection section =
          i.getSettings().getConfigurationSection("rewards." + args[0]);
      Objects.requireNonNull(section);

      final String permission = section.getString("permission");

      if (!p.hasPermission(Objects.requireNonNull(permission))) {
        returnTell(i.getMessages().getString("manual-permission"));
      }

      final long remaining = i.getManager().getRemainingCooldown(p.getUniqueId(), args[0]);

      if (remaining != -1) {
        returnTell(
            Objects.requireNonNull(i.getMessages().getString("manual-cooldown"))
                .replace("%remaining%", new TimeFormatter(remaining / 1000).format()));
      }

      i.getManager().claim(p, args[0]);
      return;
    }

    new ReclaimMenu(i, p);
  }
}
