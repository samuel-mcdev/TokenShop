package dev.tokenshop.gui;

import dev.tokenshop.TokenShop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The shop menu. Built fresh from config.yml every time it opens. */
public final class ShopGui implements InventoryHolder {

    private final TokenShop plugin;
    private final Player viewer;
    private final Map<Integer, ShopItem> itemsBySlot = new HashMap<>();
    private final Inventory inventory;

    public ShopGui(TokenShop plugin, Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;

        int rows = Math.max(1, Math.min(6, plugin.getConfig().getInt("shop.rows", 3)));
        String title = plugin.getConfig().getString("shop.title", "&8Token Shop");
        this.inventory = Bukkit.createInventory(this, rows * 9, TokenShop.color(title));

        loadItems();
        fill();
    }

    private void loadItems() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("shop.items");
        if (section == null) {
            return;
        }
        for (String id : section.getKeys(false)) {
            ConfigurationSection s = section.getConfigurationSection(id);
            if (s == null) continue;

            int slot = s.getInt("slot", -1);
            if (slot < 0 || slot >= inventory.getSize()) {
                plugin.getLogger().warning("Shop item '" + id + "' has an invalid slot, skipping.");
                continue;
            }
            Material material = Material.matchMaterial(s.getString("material", "STONE"));
            if (material == null || material.isAir()) {
                plugin.getLogger().warning("Shop item '" + id + "' has an invalid material, skipping.");
                continue;
            }

            long price = Math.max(0, s.getLong("price", 0));
            String name = s.getString("name", id);
            ItemStack icon = new ItemStack(material, Math.max(1, Math.min(64, s.getInt("amount", 1))));
            ItemMeta meta = icon.getItemMeta();
            meta.displayName(noItalic(TokenShop.color(name)));
            List<Component> lore = new ArrayList<>();
            for (String line : s.getStringList("lore")) {
                lore.add(noItalic(TokenShop.color(line.replace("%price%", String.valueOf(price)))));
            }
            meta.lore(lore);
            icon.setItemMeta(meta);

            ShopItem item = new ShopItem(id, name, icon, price, s.getStringList("commands"));
            itemsBySlot.put(slot, item);
            inventory.setItem(slot, icon);
        }
    }

    private void fill() {
        Material filler = Material.matchMaterial(plugin.getConfig().getString("shop.filler", "AIR"));
        if (filler == null || filler.isAir()) {
            return;
        }
        ItemStack pane = new ItemStack(filler);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.empty());
        pane.setItemMeta(meta);
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, pane);
            }
        }
    }

    private static Component noItalic(Component c) {
        return c.decoration(TextDecoration.ITALIC, false);
    }

    public void open() {
        viewer.openInventory(inventory);
    }

    public ShopItem itemAt(int slot) {
        return itemsBySlot.get(slot);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
