package com.scottcandy34.betterbundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

/**
 * Wraps a single "Slot" item (the special BUNDLE item with BundleMeta).
 * Encapsulates all logic for adding/removing items inside one Slot
 * (including the 12 unique item type limit).
 *
 * We will build this class one method at a time.
 */
public class BundleSlotInventory {

    private final ItemStack bundleSlotItem;
    private final ItemFactory itemFactory = new ItemFactory();
    private BundleMeta meta;

    public BundleSlotInventory(ItemStack bundleSlotItem) {
        if (!itemFactory.isOurBundleSlot(bundleSlotItem)) {
            throw new IllegalArgumentException("ItemStack is not one of our Slots");
        }

        this.bundleSlotItem = bundleSlotItem;
        this.meta = (BundleMeta) bundleSlotItem.getItemMeta();
    }

    /**
     * Attempts to add the given item into this Slot.
     *
     * @return null if everything was added successfully,
     *         or a copy of the item with the remaining amount that didn't fit.
     */
    public ItemStack addItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }

        if (!canAccept(item)) {
            return item.clone(); // cannot accept this new type
        }

        // Use a temporary inventory so Bukkit handles proper stacking
        Inventory tempInv = Bukkit.createInventory(null, 27);
        for (ItemStack existing : getContents()) {
            tempInv.addItem(existing);
        }

        HashMap<Integer, ItemStack> leftovers = tempInv.addItem(item.clone());

        // Rebuild the BundleMeta from what ended up in temp
        List<ItemStack> newList = new ArrayList<>();
        for (ItemStack i : tempInv.getContents()) {
            if (i != null && i.getType() != Material.AIR) {
                newList.add(i.clone());
            }
        }

        meta.setItems(newList);
        bundleSlotItem.setItemMeta(meta);
        this.meta = (BundleMeta) bundleSlotItem.getItemMeta();

        if (leftovers.isEmpty()) {
            return null;
        }
        return leftovers.values().iterator().next().clone();
    }

    /**
     * Attempts to add multiple items into this Slot.
     *
     * @return A list of items (or partial stacks) that could not be added.
     *         Empty list means everything was successfully added.
     */
    public List<ItemStack> addItems(List<ItemStack> items) {
        List<ItemStack> leftovers = new ArrayList<>();
        if (items == null || items.isEmpty()) {
            return leftovers;
        }

        for (ItemStack item : items) {
            if (item == null || item.getType() == Material.AIR) continue;

            ItemStack remaining = addItem(item);
            if (remaining != null && remaining.getAmount() > 0) {
                leftovers.add(remaining);
            }
        }
        return leftovers;
    }

    /**
     * Removes and returns the last item in this Slot (LIFO).
     * Returns null if the Slot is empty.
     */
    public ItemStack removeItem() {
        List<ItemStack> contents = getContents();
        if (contents.isEmpty()) {
            return null;
        }

        // Remove the last item (LIFO behavior, matching the rest of the plugin)
        ItemStack removed = contents.remove(contents.size() - 1);

        // Write the cleaned list back
        meta.setItems(contents);
        bundleSlotItem.setItemMeta(meta);
        this.meta = (BundleMeta) bundleSlotItem.getItemMeta();

        return removed;
    }

    /**
     * Removes up to the requested number of items from this Slot (LIFO).
     *
     * @param amount Maximum number of items to remove
     * @return List of items that were actually removed (may be smaller than requested)
     */
    public List<ItemStack> removeItems(int amount) {
        List<ItemStack> removed = new ArrayList<>();
        if (amount <= 0) return removed;

        List<ItemStack> contents = getContents();
        if (contents.isEmpty()) return removed;

        int toRemove = Math.min(amount, contents.size());

        for (int i = 0; i < toRemove; i++) {
            removed.add(contents.remove(contents.size() - 1));
        }

        meta.setItems(contents);
        bundleSlotItem.setItemMeta(meta);
        this.meta = (BundleMeta) bundleSlotItem.getItemMeta();

        return removed;
    }

    /**
     * Returns how many unique item types are currently stored in this Slot.
     * Maximum is 12.
     */
    public int getUniqueItemCount() {
        if (meta == null) return 0;
        Set<Material> uniqueTypes = new HashSet<>();
        for (ItemStack item : getContents()) {
            uniqueTypes.add(item.getType());
        }
        return uniqueTypes.size();
    }
    
    /**
     * Checks whether the given item can be added to this Slot.
     *
     * Rules:
     * - If the item type already exists in the slot → always accepted (stacking)
     * - If it's a new type → accepted only if we have room for another unique type (< 12)
     */
    public boolean canAccept(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || meta == null) {
            return false;
        }

        Material type = item.getType();

        // Already contains this type? → can always add more
        for (ItemStack existing : getContents()) {
            if (existing.getType() == type && existing.getAmount() < existing.getMaxStackSize()) {
                return true; // We can stack more into this existing stack
            }
        }

        // New type → only allowed if we haven't hit the 12 unique limit
        return !isFull();
    }

    /**
     * Returns true if this Slot has reached the maximum of 12 unique item types.
     * When full on unique types, it can no longer accept *new* item types,
     * but can still accept more quantity of types it already contains.
     */
    public boolean isFull() {
        return getUniqueItemCount() >= 12;
    }

    /**
     * Returns a copy of all items currently stored inside this Slot.
     * Nulls and AIR are filtered out. Items are cloned for safety.
     */
    public List<ItemStack> getContents() {
        if (meta == null) {
            return new ArrayList<>();
        }

        List<ItemStack> contents = new ArrayList<>();
        for (ItemStack item : meta.getItems()) {
            if (item != null && item.getType() != Material.AIR) {
                contents.add(item.clone());
            }
        }
        return contents;
    }

    /**
     * Cleans the internal BundleMeta item list.
     * Removes any null or AIR entries so the stored list stays tidy.
     * Call this after bulk modifications if you want a clean internal state.
     */
    public void repack() {
        if (meta == null) return;

        List<ItemStack> cleanList = new ArrayList<>();
        for (ItemStack item : getContents()) {
            cleanList.add(item.clone());
        }

        meta.setItems(cleanList);
        bundleSlotItem.setItemMeta(meta);

        // Refresh our cached meta reference
        this.meta = (BundleMeta) bundleSlotItem.getItemMeta();
    }

    /**
     * Returns the live Slot item with the latest BundleMeta applied.
     * 
     * IMPORTANT: This returns the internal ItemStack (not a clone) so that
     * callers can directly update it in the parent inventory (e.g. BundleInventory)
     * without needing to search/replace the item again.
     */
    public ItemStack getBundleSlot() {
        if (meta != null) {
            bundleSlotItem.setItemMeta(meta);
        }
        return bundleSlotItem;
    }

    /**
     * Returns true if this Slot contains no items.
     */
    public boolean isEmpty() {
        return getContents().isEmpty();
    }

    /**
     * Removes all items from this Slot.
     */
    public void clear() {
        if (meta == null) return;

        meta.setItems(new ArrayList<>());
        bundleSlotItem.setItemMeta(meta);
        this.meta = (BundleMeta) bundleSlotItem.getItemMeta();
    }
}