package com.scottcandy34.betterbundle;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class ItemFactory {

    private final BetterBundlePlugin plugin;
    private final NamespacedKey bundleKey;
    private final NamespacedKey slotKey;

    public ItemFactory(BetterBundlePlugin plugin, NamespacedKey bundleKey, NamespacedKey slotKey) {
        this.plugin = plugin;
        this.bundleKey = bundleKey;
        this.slotKey = slotKey;
    }
    
    public ItemStack createSlotItem(int amount) {
        ItemStack item = new ItemStack(Material.BUNDLE, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setItemModel(NamespacedKey.minecraft("shulker_spawn_egg"));

            meta.displayName(Component.text("Slot", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Special slot item", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(slotKey, PersistentDataType.BYTE, (byte) 1);

            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isOurSlot(ItemStack item) {
        if (item == null || item.getType() != Material.BUNDLE || !item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(slotKey, PersistentDataType.BYTE);
    }

    public ItemStack createBundleItem(int amount) {
        ItemStack item = new ItemStack(Material.SHULKER_BOX, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta instanceof BlockStateMeta bsm) {
            ShulkerBox shulker = (ShulkerBox) bsm.getBlockState();
            shulker.getInventory().clear();
            
            shulker.customName(Component.text("Bundle", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
            meta.setItemModel(NamespacedKey.minecraft("bundle"));
            meta.setMaxStackSize(1);

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(bundleKey, PersistentDataType.BYTE, (byte) 1);

            bsm.setBlockState(shulker);
            item.setItemMeta(meta);

            // Start as completely empty (stack 16, no damage)
            plugin.updateBundle(item);
        }
        return item;
    }

    public boolean isOurBundle(ItemStack item) {
        if (item == null || item.getType() != Material.SHULKER_BOX || !item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(bundleKey, PersistentDataType.BYTE);
    }
}
