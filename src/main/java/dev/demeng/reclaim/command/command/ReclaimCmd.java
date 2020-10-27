package dev.demeng.reclaim.command.command;

import dev.demeng.demlib.command.CustomCommand;
import dev.demeng.reclaim.Reclaim;
import dev.demeng.reclaim.menu.ReclaimMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReclaimCmd extends CustomCommand {

  private final Reclaim i;

  public ReclaimCmd(Reclaim i) {
    super("reclaim", true, null, 0, "[reward]");
    this.i = i;
    setDescription("Base command for Reclaim plugin.");
  }

  @Override
  protected void run(CommandSender sender, String[] args) {
    new ReclaimMenu(i, (Player) sender);
  }
}
