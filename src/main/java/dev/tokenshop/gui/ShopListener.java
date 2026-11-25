package dev.tokenshop.gui;

import dev.tokenshop.TokenShop;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

/** Handles clicks inside the shop menu and blocks item stealing. */
public final class ShopListener implements Listener {

    private final TokenShop plugin;

    public ShopListener(TokenShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ShopGui gui)) {
            return;
        }
        // Never let items move in or out of the shop
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        // Only react to clicks in the top (shop) inventory
        if (event.getClickedInventory() == null || event.getClickedInventory() != gui.getInventory()) {
            return;
        }

        ShopItem item = gui.itemAt(event.getRawSlot());
        if (item == null) {
            return;
        }

        if (!plugin.tokens().take(player.getUniqueId(), item.price())) {
            plugin.send(player, "not-enough");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        for (String cmd : item.commands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
        }
        plugin.send(player, "purchased", "item", item.displayName(), "price", String.valueOf(item.price()));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof ShopGui) {
            event.setCancelled(true);
        }
    }
}
