package dev.tokenshop.data;

import dev.tokenshop.TokenShop;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/** Stores every player's token balance in tokens.yml. */
public final class TokenManager {

    private final TokenShop plugin;
    private final File file;
    private final Map<UUID, Long> balances = new ConcurrentHashMap<>();
    private boolean dirty = false;

    public TokenManager(TokenShop plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "tokens.yml");
    }

    public void load() {
        balances.clear();
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("balances");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            try {
                balances.put(UUID.fromString(key), section.getLong(key));
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Skipping invalid UUID in tokens.yml: " + key);
            }
        }
    }

    public synchronized void save() {
        if (!dirty && file.exists()) {
            return;
        }
        YamlConfiguration yaml = new YamlConfiguration();
        balances.forEach((uuid, amount) -> yaml.set("balances." + uuid, amount));
        try {
            plugin.getDataFolder().mkdirs();
            yaml.save(file);
            dirty = false;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save tokens.yml", e);
        }
    }

    public long getBalance(UUID uuid) {
        return balances.computeIfAbsent(uuid, id -> {
            dirty = true;
            return Math.max(0L, plugin.getConfig().getLong("starting-balance", 0));
        });
    }

    public boolean has(UUID uuid, long amount) {
        return getBalance(uuid) >= amount;
    }

    public void add(UUID uuid, long amount) {
        set(uuid, getBalance(uuid) + amount);
    }

    /** Removes tokens if the player has enough. Returns false if they don't. */
    public boolean take(UUID uuid, long amount) {
        long current = getBalance(uuid);
        if (current < amount) {
            return false;
        }
        set(uuid, current - amount);
        return true;
    }

    public void set(UUID uuid, long amount) {
        balances.put(uuid, Math.max(0L, amount));
        dirty = true;
    }

    public int size() {
        return balances.size();
    }
}
