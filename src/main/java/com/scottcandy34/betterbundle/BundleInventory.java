package com.scottcandy34.betterbundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Represents the main inventory of a Bundle (the 27-slot container).
 * This class will eventually know how to delegate to SlotInventory instances
 * when normal space runs out, and will handle repacking, weight, etc.
 *
 * Built one method at a time, just like SlotInventory.
 */
public class BundleInventory {

    private final Inventory handle;
    private final ItemFactory itemFactory = new ItemFactory();
    
    private static final int MAX_WEIGHT = 64;
    private static final int MAX_INDIVIDUAL_STACK_COUNT = 27 * 12; // 324
    

    public BundleInventory(Inventory handle) {
        if (handle == null) {
            throw new IllegalArgumentException("Inventory handle cannot be null");
        }
        this.handle = handle;
    }

    /**
     * Attempts to add an item into this Bundle.
     *
     * Flow:
     * 1. Early exit if item is invalid or bundle cannot accept it.
     * 2. Try adding to normal 27 slots first.
     * 3. If leftovers remain, try adding into existing BundleSlotInventory slots.
     * 4. If still leftovers, expand by creating a new slot via createSlotAndAdd().
     * 5. Return any remaining item that could not be stored.
     *
     * @return null if fully added, otherwise the remaining ItemStack.
     */
    public ItemStack addItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }

        if (!canAccept(item)) {
            return item.clone();
        }

        // 1. Try normal inventory slots first
        HashMap<Integer, ItemStack> map = handle.addItem(item.clone());
        ItemStack remaining = map.isEmpty() ? null : map.values().iterator().next();

        if (remaining == null || remaining.getAmount() <= 0) {
            return null;
        }

        // 2. Try existing BundleSlotInventory slots
        remaining = addToExistingSlots(remaining);
        if (remaining == null || remaining.getAmount() <= 0) {
            return null;
        }

        // 3. Still had leftover → create a new slot and try again
        remaining = createSlotAndAdd(remaining);
        if (remaining == null || remaining.getAmount() <= 0) {
            return null;
        }

        return remaining;
    }

    /**
     * Attempts to add multiple items into this Bundle.
     *
     * @return List of items (or partial stacks) that could not be added.
     *         Returns an empty list if everything was successfully added.
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
     * Removes and returns the last item from this Bundle (LIFO).
     * This includes items stored inside any BundleSlotInventory.
     *
     * After removal, the inventory is automatically repacked to maintain
     * proper structure (Slots at the end, empty Slots cleaned, etc.).
     *
     * @return The removed ItemStack, or null if the bundle was empty.
     */
    public ItemStack removeItem() {
        List<ItemStack> allItems = getContents();

        if (allItems.isEmpty()) {
            return null;
        }

        // Remove the last item (LIFO behavior)
        ItemStack removed = allItems.remove(allItems.size() - 1);

        // Rebuild the inventory cleanly with the remaining items
        handle.clear();
        repack(allItems);

        return removed;
    }

    /**
     * Removes up to the requested number of items from this Bundle (LIFO).
     *
     * After removal, the inventory is automatically repacked to maintain
     * proper structure (Slots at the end, empty Slots cleaned, etc.).
     *
     * @param amount Maximum number of items to remove
     * @return List of items that were actually removed (may be smaller than requested)
     */
    public List<ItemStack> removeItems(int amount) {
        List<ItemStack> removed = new ArrayList<>();
        if (amount <= 0) {
            return removed;
        }

        List<ItemStack> allItems = getContents();
        if (allItems.isEmpty()) {
            return removed;
        }

        int toRemove = Math.min(amount, allItems.size());

        for (int i = 0; i < toRemove; i++) {
            removed.add(allItems.remove(allItems.size() - 1));
        }

        // Rebuild the inventory cleanly with the remaining items
        handle.clear();
        repack(allItems);

        return removed;
    }

    /**
     * Calculates the total weight of everything inside this bundle,
     * including items stored inside any Slots.
     */
    public int getWeight() {
        int weight = 0;

        for (ItemStack item : getContents()) {
            int amount = item.getAmount();
            int maxStack = item.getMaxStackSize();
            weight += (amount * 64 + maxStack - 1) / maxStack;
        }

        return weight;
    }
    
    /**
     * Returns true if this bundle contains no items at all
     * (including items inside any Slots).
     */
    public boolean isEmpty() {
        return getContents().isEmpty();
    }
    
    /**
     * Returns a flat list of ALL items in this bundle, including items
     * stored inside any SlotInventory instances.
     */
    public List<ItemStack> getContents() {
        List<ItemStack> contents = new ArrayList<>();

        for (ItemStack item : handle.getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;

            try {
                // If this is one of our Slots, flatten its contents
                BundleSlotInventory slotInv = new BundleSlotInventory(item);
                contents.addAll(slotInv.getContents());
            } catch (IllegalArgumentException e) {
                // Not a Slot → normal item
                contents.add(item.clone());
            }
        }

        return contents;
    }
    
    /**
     * Returns true if this bundle has reached its maximum capacity.
     * Checks both weight limit (64) and maximum individual stack count (27 * 12).
     */
    public boolean isFull() {
        if (getWeight() >= MAX_WEIGHT) {
            return true;
        }

        int totalIndividualStacks = getContents().size();
        return totalIndividualStacks >= MAX_INDIVIDUAL_STACK_COUNT;
    }

    /**
     * Checks whether this bundle can accept the given item.
     *
     * Rules:
     * - If it's a completely new type → accepted only if we have room in both weight (64) and individual stack count (27*12)
     * - If an existing stack of the same type still has room (amount < maxStackSize) → accepted
     */
    public boolean canAccept(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        Material type = item.getType();

        if (getWeight() >= MAX_WEIGHT) {
            return false;
        }

        for (ItemStack existing : getContents()) {
            if (existing.getType() == type) {
                if (existing.getAmount() < existing.getMaxStackSize()) {
                    return true; // We can stack more into this existing stack
                }
            }
        }

        if (getContents().size() >= MAX_INDIVIDUAL_STACK_COUNT) {
            return false;
        }

        return true;
    }

    /**
     * Rebuilds the bundle inventory from a list of items.
     *
     * Flow:
     * 1. Tries to add each item to the normal 27-slot inventory first.
     * 2. If leftovers remain, attempts to add them into existing SlotInventories.
     * 3. If still leftovers, expands by creating a new Slot via expandWithNewSlot().
     * 4. Any items that still could not fit are collected and returned.
     * 5. Finally runs pruneEmptySlots() to clean up any empty Slots.
     *
     * @return List of items that could not be fitted (empty list if everything fit).
     */
    private List<ItemStack> repack(List<ItemStack> items) {
        List<ItemStack> leftovers = new ArrayList<>();

        if (items == null || items.isEmpty()) {
            pruneEmptySlots();
            return leftovers;
        }

        for (ItemStack item : items) {
            if (item == null || item.getType() == Material.AIR) continue;

            // Step 1: Try normal inventory first
            HashMap<Integer, ItemStack> map = handle.addItem(item.clone());
            ItemStack remaining = map.isEmpty() ? null : map.values().iterator().next();

            // Step 2: Try existing Slots
            if (remaining != null && remaining.getAmount() > 0) {
                remaining = addToExistingSlots(remaining);
            }

            // Step 3: Still had leftover → create a new Slot and try again
            if (remaining != null && remaining.getAmount() > 0) {
                remaining = createSlotAndAdd(remaining);
            }

            // Step 4: If it still couldn't fit after all attempts, collect it
            if (remaining != null && remaining.getAmount() > 0) {
                leftovers.add(remaining);
            }
        }

        // Final cleanup
        pruneEmptySlots();
        return leftovers;
    }
    
    private int countBundleSlots() {
        if (handle == null) return 0;
        int count = 0;
        for (ItemStack item : handle.getContents()) {
            if (itemFactory.isOurBundleSlot(item)) count++;
        }
        return count;
    }

    /**
     * Attempts to add the given item into any existing SlotInventory inside this bundle.
     * 
     * - If an item is added to a Slot and produces a leftover, it will try to add 
     *   that leftover into the next available Slot.
     * - Returns null if everything was successfully added.
     * - Returns the remaining ItemStack if it could not fit into any Slot.
     */
    private ItemStack addToExistingSlots(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }

        ItemStack current = item.clone();

        for (ItemStack content : handle.getContents()) {
            if (content == null || content.getType() == Material.AIR) continue;

            try {
                BundleSlotInventory slotInv = new BundleSlotInventory(content);

                if (slotInv.canAccept(current)) {
                    ItemStack leftover = slotInv.addItem(current);

                    // Apply changes back to the original ItemStack in the inventory
                    content.setItemMeta(slotInv.getBundleSlot().getItemMeta());

                    if (leftover == null || leftover.getAmount() <= 0) {
                        return null; // Fully added
                    }

                    // There was leftover — continue trying to add it to other Slots
                    current = leftover;
                }
            } catch (IllegalArgumentException ignored) {
                // Not a SlotInventory, skip
            }
        }

        // Could not fit everything into existing Slots
        return current;
    }

    /**
     * Prepares the inventory for a repack operation.
     *
     * - Counts the current number of Slots **before** clearing.
     * - Calculates the final target slot count as: current + addition.
     * - Extracts all items, clears the inventory, and creates the target number of empty Slots at the end.
     * - Returns the extracted items so they can be re-inserted by the caller.
     *
     * @param addition How many additional Slots to create (can be negative to remove Slots)
     */
    private List<ItemStack> prepareForRepack(int addition) {
        // 1. Count current slots BEFORE we clear anything
        int currentSlotCount = countBundleSlots();

        int targetSlotCount = Math.max(0, currentSlotCount + addition);

        // 2. Extract all current items (including items inside Slots)
        List<ItemStack> allItems = getContents();

        // 3. Clear the inventory
        handle.clear();

        // 4. Create the target number of empty Slots at the end
        for (int i = 0; i < targetSlotCount; i++) {
            int slotIndex = handle.getSize() - 1 - i;
            if (slotIndex >= 0) {
                handle.setItem(slotIndex, itemFactory.createBundleSlotItem(1));
            }
        }

        // 5. Return the saved items for the caller to re-insert
        return allItems;
    }

    /**
     * Forces the creation of one new Slot by calling prepareForRepack(1),
     * re-inserts existing items, then attempts to add the given item
     * into the now expanded set of Slots.
     *
     * @return leftover ItemStack if it still couldn't fit, or null if fully added.
     */
    private ItemStack createSlotAndAdd(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }

        // 1. Extract all current items and create 1 new empty Slot at the end
        List<ItemStack> existingItems = prepareForRepack(1);

        // 2. Rebuild the inventory with the existing items (new repack overload)
        repack(existingItems);

        // 3. Now try to add the new item into existing Slots (including the newly created one)
        ItemStack leftover = addToExistingSlots(item);

        // 4. Return whatever couldn't fit (if any)
        return leftover;
    }

    /**
     * Scans the inventory for empty Slot items and removes them.
     * 
     * Process:
     * 1. Counts how many SlotInventory items are currently empty.
     * 2. If any empty Slots exist, calls prepareForRepack() with that count
     *    so the repack logic can reduce the total number of Slots.
     * 3. Then calls repack() with the items returned from prepareForRepack().
     */
    private void pruneEmptySlots() {
        int emptySlotCount = 0;

        // Step 1: Count how many Slot items are empty
        for (ItemStack item : handle.getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;

            try {
                BundleSlotInventory slotInv = new BundleSlotInventory(item);
                if (slotInv.isEmpty()) {
                    emptySlotCount++;
                }
            } catch (IllegalArgumentException ignored) {
                // Not a Slot, skip
            }
        }

        // Step 2 & 3: If we found empty Slots, remove them via prepareForRepack + repack
        if (emptySlotCount > 0) {
            List<ItemStack> remainingItems = prepareForRepack(-emptySlotCount);
            repack(remainingItems);
        }
    }

    /**
     * Returns the underlying Inventory that can be opened with player.openInventory(...).
     * This is the "live" inventory the player interacts with.
     */
    public Inventory getHandle() {
        return handle;
    }

    /**
     * Clears all items from this bundle's inventory.
     * 
     * This directly clears the underlying 27-slot handle.
     * Note: This removes Slot items themselves. Contents inside Slots
     * will be lost unless they were already extracted.
     */
    public void clear() {
        handle.clear();
    }
}