package dev.tokenshop;

import dev.tokenshop.command.TokenShopCommand;
import dev.tokenshop.command.TokensCommand;
import dev.tokenshop.data.TokenManager;
import dev.tokenshop.gui.ShopListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class TokenShop extends JavaPlugin implements Listener {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private TokenManager tokenManager;
    private int autosaveTaskId = -1;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        tokenManager = new TokenManager(this);
        tokenManager.load();

        TokensCommand tokensCommand = new TokensCommand(this);
        PluginCommand tokens = Objects.requireNonNull(getCommand("tokens"));
        tokens.setExecutor(tokensCommand);
        tokens.setTabCompleter(tokensCommand);

        TokenShopCommand shopCommand = new TokenShopCommand(this);
        PluginCommand shop = Objects.requireNonNull(getCommand("tokenshop"));
        shop.setExecutor(shopCommand);
        shop.setTabCompleter(shopCommand);

        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(this, this);

        startAutosave();
        getLogger().info("TokenShop enabled. Loaded " + tokenManager.size() + " player balances.");
    }

    @Override
    public void onDisable() {
        if (tokenManager != null) {
            tokenManager.save();
        }
    }

    /** Reloads config.yml and restarts the autosave timer. */
    public void reload() {
        reloadConfig();
        startAutosave();
    }

    private void startAutosave() {
        if (autosaveTaskId != -1) {
            getServer().getScheduler().cancelTask(autosaveTaskId);
            autosaveTaskId = -1;
        }
        long minutes = getConfig().getLong("autosave-minutes", 5);
        if (minutes > 0) {
            long ticks = minutes * 60L * 20L;
            autosaveTaskId = getServer().getScheduler()
                    .runTaskTimer(this, tokenManager::save, ticks, ticks)
                    .getTaskId();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Creates the account with the starting balance on first join
        tokenManager.getBalance(event.getPlayer().getUniqueId());
    }

    public TokenManager tokens() {
        return tokenManager;
    }

    /** Turns "&a text" into a colored Component. */
    public static Component color(String text) {
        return LEGACY.deserialize(text == null ? "" : text);
    }

    /** Sends a prefixed message from config, replacing %key% placeholders (pairs of key, value). */
    public void send(CommandSender to, String key, String... replacements) {
        String prefix = getConfig().getString("messages.prefix", "");
        String msg = getConfig().getString("messages." + key, key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            msg = msg.replace("%" + replacements[i] + "%", replacements[i + 1]);
        }
        to.sendMessage(color(prefix + msg));
    }
}
