package com.grok.betterbundles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BundleManager {

    private final JavaPlugin plugin;
    private final NamespacedKey tierKey;
    private final NamespacedKey contentsKey;

    public BundleManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.tierKey = new NamespacedKey(plugin, "bundle_tier");
        this.contentsKey = new NamespacedKey(plugin, "bundle_contents");
    }

    public ItemStack createBundle(BundleTier tier) {
        ItemStack item = new ItemStack(tier.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(tier.getColoredName());
        meta.setCustomModelData(tier.getCustomModelData());

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Capacity: " + ChatColor.YELLOW + tier.getCapacity() + " units");
        lore.add(ChatColor.GRAY + "Holds up to " + (tier.getCapacity() / 64) + " stacks");
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "Right-click to open inventory");
        lore.add(ChatColor.DARK_GRAY + "Shift-right-click in inventory to open");
        if (tier == BundleTier.NETHERITE) {
            lore.add(ChatColor.RED + "Fire & Lava resistant");
        }
        meta.setLore(lore);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(tierKey, PersistentDataType.STRING, tier.name());
        pdc.set(contentsKey, PersistentDataType.BYTE_ARRAY, new byte[0]);

        item.setItemMeta(meta);
        return item;
    }

    public boolean isCustomBundle(ItemStack item) {
        if (item == null || item.getType() != Material.BUNDLE) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(tierKey, PersistentDataType.STRING);
    }

    public BundleTier getTier(ItemStack item) {
        if (!isCustomBundle(item)) return null;
        ItemMeta meta = item.getItemMeta();
        String tierName = meta.getPersistentDataContainer().get(tierKey, PersistentDataType.STRING);
        return BundleTier.fromString(tierName);
    }

    public List<ItemStack> getContents(ItemStack bundle) {
        if (!isCustomBundle(bundle)) return new ArrayList<>();
        ItemMeta meta = bundle.getItemMeta();
        byte[] data = meta.getPersistentDataContainer().get(contentsKey, PersistentDataType.BYTE_ARRAY);
        if (data == null || data.length == 0) return new ArrayList<>();

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             BukkitObjectInputStream bois = new BukkitObjectInputStream(bais)) {
            @SuppressWarnings("unchecked")
            List<ItemStack> items = (List<ItemStack>) bois.readObject();
            List<ItemStack> clean = new ArrayList<>();
            for (ItemStack is : items) {
                if (is != null && is.getType() != Material.AIR) {
                    clean.add(is.clone());
                }
            }
            return clean;
        } catch (IOException | ClassNotFoundException e) {
            plugin.getLogger().warning("Failed to deserialize bundle contents: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void setContents(ItemStack bundle, List<ItemStack> contents) {
        if (!isCustomBundle(bundle)) return;
        ItemMeta meta = bundle.getItemMeta();
        if (meta == null) return;

        List<ItemStack> toSave = new ArrayList<>();
        for (ItemStack is : contents) {
            if (is != null && is.getType() != Material.AIR) {
                toSave.add(is.clone());
            }
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos)) {
            boos.writeObject(toSave);
            byte[] data = baos.toByteArray();
            meta.getPersistentDataContainer().set(contentsKey, PersistentDataType.BYTE_ARRAY, data);
            bundle.setItemMeta(meta);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to serialize bundle contents: " + e.getMessage());
        }
    }

    public double calculateUsedCapacity(List<ItemStack> items) {
        double used = 0;
        for (ItemStack item : items) {
            if (item == null || item.getType() == Material.AIR) continue;
            int maxStack = item.getMaxStackSize();
            if (maxStack <= 0) maxStack = 1;
            used += item.getAmount() * (64.0 / maxStack);
        }
        return used;
    }

    public boolean canAddItem(List<ItemStack> currentContents, ItemStack toAdd, BundleTier tier) {
        if (toAdd == null || toAdd.getType() == Material.AIR) return true;
        List<ItemStack> testList = new ArrayList<>(currentContents);
        testList.add(toAdd.clone());
        return calculateUsedCapacity(testList) <= tier.getCapacity();
    }
}