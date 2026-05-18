package com.scottcandy34.betterbundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

public class BundleRepackService {

    private final BetterBundlePlugin plugin;

    public BundleRepackService(BetterBundlePlugin plugin) {
        this.plugin = plugin;
    }

    public int getUniqueItemCount(ItemStack slot) {
        if (!plugin.getItemFactory().isOurSlot(slot) || !(slot.getItemMeta() instanceof BundleMeta meta)) return 0;
        Set<Material> unique = new HashSet<>();
        for (ItemStack content : meta.getItems()) {
            if (content != null && content.getType() != Material.AIR) unique.add(content.getType());
        }
        return unique.size();
    }

    public boolean tryAddToExistingSlot(Inventory bundleInv, ItemStack toAdd) {
        if (toAdd == null || toAdd.getType() == Material.AIR) return false;

        for (ItemStack content : bundleInv.getContents()) {
            if (plugin.getItemFactory().isOurSlot(content)) {
                if (!(content.getItemMeta() instanceof BundleMeta meta)) continue;

                int unique = getUniqueItemCount(content);
                boolean alreadyPresent = false;

                for (ItemStack existing : meta.getItems()) {
                    if (existing != null && existing.getType() == toAdd.getType()) {
                        alreadyPresent = true;
                        break;
                    }
                }

                if (unique < 12 || alreadyPresent) {
                    Inventory tempInv = Bukkit.createInventory(null, 27);
                    List<ItemStack> currentItems = new ArrayList<>();
                    for (ItemStack item : meta.getItems()) {
                        if (item != null && item.getType() != Material.AIR) currentItems.add(item.clone());
                    }
                    tempInv.setContents(currentItems.toArray(new ItemStack[0]));
                    tempInv.addItem(toAdd.clone());

                    List<ItemStack> finalItems = new ArrayList<>();
                    for (ItemStack item : tempInv.getContents()) {
                        if (item != null && item.getType() != Material.AIR) finalItems.add(item);
                    }

                    meta.setItems(finalItems);
                    content.setItemMeta(meta);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addNewSlotToBundle(Inventory bundleInv, ItemStack toAdd) {
        repackBundle(bundleInv, 1);
        if (toAdd != null && toAdd.getType() != Material.AIR) {
            tryAddToExistingSlot(bundleInv, toAdd.clone());
        }
        return true;
    }

    public List<ItemStack> getItemsFromSlot(ItemStack slot) {
        if (!plugin.getItemFactory().isOurSlot(slot) || !(slot.getItemMeta() instanceof BundleMeta meta)) {
            return Collections.emptyList();
        }
        return new ArrayList<>(meta.getItems());
    }

    public List<ItemStack> getAllItemsFromBundle(Inventory bundleInv) {
        List<ItemStack> allItems = new ArrayList<>();
        for (ItemStack item : bundleInv.getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;
            if (plugin.getItemFactory().isOurSlot(item)) {
                allItems.addAll(getItemsFromSlot(item));
            } else {
                allItems.add(item.clone());
            }
        }
        allItems.removeIf(i -> i == null || i.getType() == Material.AIR);
        return allItems;
    }

    public int countSlotItems(Inventory bundleInv) {
        if (bundleInv == null) return 0;
        int count = 0;
        for (ItemStack item : bundleInv.getContents()) {
            if (plugin.getItemFactory().isOurSlot(item)) count++;
        }
        return count;
    }
    
    public void addSlotItemsAtEnd(Inventory bundleInv, int amount) {
        if (bundleInv == null || amount <= 0) return;
        for (int i = 0; i < amount; i++) {
            int slotIndex = bundleInv.getSize() - 1 - i;
            if (slotIndex >= 0) {
                bundleInv.setItem(slotIndex, plugin.getItemFactory().createSlotItem(1));
            }
        }
    }

    public Inventory repackBundle(List<ItemStack> allItems) {
        Inventory bundleInv = Bukkit.createInventory(null, 27);
        for (ItemStack item : allItems) {
            HashMap<Integer, ItemStack> leftovers = bundleInv.addItem(item);
            if (!leftovers.isEmpty()) {
                ItemStack remaining = leftovers.values().iterator().next();
                if (!tryAddToExistingSlot(bundleInv, remaining)) {
                    addNewSlotToBundle(bundleInv, remaining);
                }
            }
        }
        ItemStack lastItem = bundleInv.getItem(26);
        if (plugin.getItemFactory().isOurSlot(lastItem) && getUniqueItemCount(lastItem) == 0) {
            repackBundle(bundleInv, -1);
        }
        return bundleInv;
    }

    public void repackBundle(Inventory bundleInv, int addition) {
        List<ItemStack> allItems = getAllItemsFromBundle(bundleInv);
        int currentSlotItems = countSlotItems(bundleInv);
        bundleInv.clear();
        addSlotItemsAtEnd(bundleInv, currentSlotItems + addition);

        for (ItemStack item : allItems) {
            HashMap<Integer, ItemStack> leftovers = bundleInv.addItem(item);
            if (!leftovers.isEmpty()) {
                ItemStack remaining = leftovers.values().iterator().next();
                if (!tryAddToExistingSlot(bundleInv, remaining)) {
                    addNewSlotToBundle(bundleInv, remaining);
                }
            }
        }
        ItemStack lastItem = bundleInv.getItem(26);
        if (plugin.getItemFactory().isOurSlot(lastItem) && getUniqueItemCount(lastItem) == 0) {
            repackBundle(bundleInv, -1);
        }
    }
}
