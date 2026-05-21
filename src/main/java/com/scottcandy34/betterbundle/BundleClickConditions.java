package com.scottcandy34.betterbundle;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class BundleClickConditions {

    private final ItemFactory itemFactory = new ItemFactory();

    public boolean isAttemptingSlotInsertion(InventoryClickEvent event) {
        if (!event.getClick().isLeftClick()) return false;

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        // Case 1: Clicking on a Slot with an item on the cursor
        if (current != null && current.getType() != Material.AIR) {
            if (itemFactory.isOurBundleSlot(current) && cursor != null && cursor.getType() != Material.AIR) {
                return true;
            }
        }

        // Case 2: Clicking with a Slot on the cursor into an item
        if (cursor != null && cursor.getType() != Material.AIR) {
            if (itemFactory.isOurBundleSlot(cursor) && current != null && current.getType() != Material.AIR) {
                return true;
            }
        }

        return false;
    }

    public boolean isShiftLeftClickToOpen(InventoryClickEvent event) {
        return event.getClick() == ClickType.SHIFT_LEFT && itemFactory.isOurBundle(event.getCurrentItem());
    }

    public boolean isShiftRightClickToEmptySlot(InventoryClickEvent event) {
        return event.getClick() == ClickType.SHIFT_RIGHT && itemFactory.isOurBundleSlot(event.getCursor());
    }

    public boolean isShiftRightClickToEmptyBundle(InventoryClickEvent event) {
        return event.getClick() == ClickType.SHIFT_RIGHT && itemFactory.isOurBundle(event.getCursor());
    }

    public boolean isLeftClickToInsert(InventoryClickEvent event) {
        if (!event.getClick().isLeftClick()) return false;

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        // Case 1: Clicking on a Bundle with an item on cursor → check if bundle is not full
        if (itemFactory.isOurBundle(current) && cursor != null && cursor.getType() != Material.AIR) {
            return true;
        }

        // Case 2: Clicking with a Bundle on cursor into an item
        if (itemFactory.isOurBundle(cursor) && current != null && current.getType() != Material.AIR) {
            return true;
        }

        return false;
    }

    public boolean isRightClickToRemove(InventoryClickEvent event) {
        if (!event.getClick().isRightClick()) return false;

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        // Case 1: Right-clicking with Bundle on cursor into an empty slot
        if (cursor != null && cursor.getType() != Material.AIR) {
            try {
                BundleItem bundle = new BundleItem(cursor);
                boolean emptySlot = (current == null || current.getType() == Material.AIR);
                return emptySlot && !bundle.isEmpty();
            } catch (IllegalArgumentException ignored) {}
        }

        // Case 2: Right-clicking on a Bundle in inventory with empty cursor
        if (current != null && current.getType() != Material.AIR) {
            try {
                BundleItem bundle = new BundleItem(current);
                boolean emptyCursor = (cursor == null || cursor.getType() == Material.AIR);
                return emptyCursor && !bundle.isEmpty();
            } catch (IllegalArgumentException ignored) {}
        }

        return false;
    }
}
