package dev.tokenshop.gui;

import org.bukkit.inventory.ItemStack;

import java.util.List;

/** One item in the shop, loaded from config.yml. */
public record ShopItem(String id, String displayName, ItemStack icon, long price, List<String> commands) {
}
