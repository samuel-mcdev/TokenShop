package dev.tokenshop.command;

import dev.tokenshop.TokenShop;
import dev.tokenshop.gui.ShopGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** /tokenshop opens the shop, /tokenshop reload reloads config.yml. */
public final class TokenShopCommand implements TabExecutor {

    private final TokenShop plugin;

    public TokenShopCommand(TokenShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("tokenshop.admin")) {
                plugin.send(sender, "no-permission");
                return true;
            }
            plugin.reload();
            plugin.send(sender, "reloaded");
            return true;
        }

        if (!(sender instanceof Player player)) {
            plugin.send(sender, "players-only");
            return true;
        }
        if (!player.hasPermission("tokenshop.use")) {
            plugin.send(sender, "no-permission");
            return true;
        }
        new ShopGui(plugin, player).open();
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("tokenshop.admin") && "reload".startsWith(args[0].toLowerCase())) {
            return List.of("reload");
        }
        return List.of();
    }
}
