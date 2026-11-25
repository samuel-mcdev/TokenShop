package dev.tokenshop.command;

import dev.tokenshop.TokenShop;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * /tokens                       - your balance
 * /tokens <player>              - someone else's balance
 * /tokens pay <player> <amount> - send tokens
 * /tokens give|take|set <player> <amount> - admin
 */
public final class TokensCommand implements TabExecutor {

    private final TokenShop plugin;

    public TokensCommand(TokenShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("tokenshop.use")) {
            plugin.send(sender, "no-permission");
            return true;
        }

        // /tokens
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                plugin.send(sender, "usage");
                return true;
            }
            plugin.send(sender, "balance", "amount", fmt(plugin.tokens().getBalance(player.getUniqueId())));
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        // /tokens <player>
        if (args.length == 1 && !List.of("pay", "give", "take", "set").contains(sub)) {
            OfflinePlayer target = findPlayer(args[0]);
            if (target == null) {
                plugin.send(sender, "player-not-found");
                return true;
            }
            plugin.send(sender, "balance-other",
                    "player", name(target), "amount", fmt(plugin.tokens().getBalance(target.getUniqueId())));
            return true;
        }

        if (args.length != 3) {
            plugin.send(sender, "usage");
            return true;
        }

        OfflinePlayer target = findPlayer(args[1]);
        if (target == null) {
            plugin.send(sender, "player-not-found");
            return true;
        }
        long amount = parseAmount(args[2]);
        if (amount <= 0 && !(sub.equals("set") && amount == 0)) {
            plugin.send(sender, "invalid-amount");
            return true;
        }

        switch (sub) {
            case "pay" -> pay(sender, target, amount);
            case "give", "take", "set" -> admin(sender, sub, target, amount);
            default -> plugin.send(sender, "usage");
        }
        return true;
    }

    private void pay(CommandSender sender, OfflinePlayer target, long amount) {
        if (!(sender instanceof Player player)) {
            plugin.send(sender, "players-only");
            return;
        }
        if (player.getUniqueId().equals(target.getUniqueId())) {
            plugin.send(sender, "cannot-pay-self");
            return;
        }
        if (!plugin.tokens().take(player.getUniqueId(), amount)) {
            plugin.send(sender, "not-enough");
            return;
        }
        plugin.tokens().add(target.getUniqueId(), amount);
        plugin.send(sender, "paid", "amount", fmt(amount), "player", name(target));
        Player online = target.getPlayer();
        if (online != null) {
            plugin.send(online, "received", "amount", fmt(amount), "player", player.getName());
        }
    }

    private void admin(CommandSender sender, String sub, OfflinePlayer target, long amount) {
        if (!sender.hasPermission("tokenshop.admin")) {
            plugin.send(sender, "no-permission");
            return;
        }
        switch (sub) {
            case "give" -> {
                plugin.tokens().add(target.getUniqueId(), amount);
                plugin.send(sender, "given", "amount", fmt(amount), "player", name(target));
            }
            case "take" -> {
                long current = plugin.tokens().getBalance(target.getUniqueId());
                long removed = Math.min(current, amount);
                plugin.tokens().set(target.getUniqueId(), current - removed);
                plugin.send(sender, "taken", "amount", fmt(removed), "player", name(target));
            }
            case "set" -> {
                plugin.tokens().set(target.getUniqueId(), amount);
                plugin.send(sender, "set", "amount", fmt(amount), "player", name(target));
            }
            default -> { }
        }
    }

    /** Finds online players first, then players who have joined before (no web lookup). */
    private OfflinePlayer findPlayer(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return online;
        }
        OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached(name);
        return (cached != null && cached.hasPlayedBefore()) ? cached : null;
    }

    private static String name(OfflinePlayer player) {
        return player.getName() != null ? player.getName() : player.getUniqueId().toString();
    }

    private static long parseAmount(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String fmt(long amount) {
        return String.format(Locale.US, "%,d", amount);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(List.of("pay"));
            if (sender.hasPermission("tokenshop.admin")) {
                subs.addAll(List.of("give", "take", "set"));
            }
            for (String s : subs) {
                if (s.startsWith(args[0].toLowerCase(Locale.ROOT))) out.add(s);
            }
        } else if (args.length == 2) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT))) {
                    out.add(p.getName());
                }
            }
        } else if (args.length == 3) {
            out.addAll(List.of("10", "50", "100"));
        }
        return out;
    }
}
