package com.scottcandy34.betterbundle;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
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

    public boolean isInteractingWithOpenedBundle(InventoryClickEvent event) {
        InventoryHolder topHolder = event.getView().getTopInventory().getHolder();
        if (!(topHolder instanceof BundleInventoryHolder openedHolder)) return false;

        UUID openedId = openedHolder.getBundleItem() != null ? openedHolder.getBundleItem().getBundleId() : null;

        if (openedId == null) return false;

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        // Check current item in slot
        if (current != null && itemFactory.isOurBundle(current)) {
            try {
                BundleItem bundle = new BundleItem(current);
                if (openedId.equals(bundle.getBundleId())) return true;
            } catch (IllegalArgumentException ignored) {}
        }

        // Check cursor
        if (cursor != null && itemFactory.isOurBundle(cursor)) {
            try {
                BundleItem bundle = new BundleItem(cursor);
                if (openedId.equals(bundle.getBundleId())) return true;
            } catch (IllegalArgumentException ignored) {}
        }

        return false;
    }

    public boolean isCursorDropClickInBundleGui(InventoryClickEvent event) {
        ClickType click = event.getClick();
        if (click != ClickType.DROP && click != ClickType.CONTROL_DROP) return false;

        // Must be dropping into the Bundle GUI itself
        if (event.getClickedInventory() == null) return false;
        return event.getClickedInventory().equals(event.getView().getTopInventory());
    }

    public boolean isNumberKeyClick(InventoryClickEvent event) {
        return event.getClick() == ClickType.NUMBER_KEY;
    }

    public boolean isBlockedSlotInteractionInBundleGui(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getView().getTopInventory())) return false;

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        if (!itemFactory.isOurBundleSlot(current) && !itemFactory.isOurBundleSlot(cursor)) return false;

        boolean isLeftClickWithItemOnCursor = event.getClick().isLeftClick() && cursor != null && cursor.getType() != Material.AIR;

        // Block everything except right-click and left-click with item on cursor
        return !event.getClick().isRightClick() && !isLeftClickWithItemOnCursor;
    }

    public boolean isRemovalClickInBundleGui(InventoryClickEvent event) {
        ClickType click = event.getClick();
        boolean isRemovalClick = click == ClickType.LEFT || click == ClickType.RIGHT || click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT;

        if (!isRemovalClick) return false;
        if (event.getClickedInventory() == null) return false;
        return event.getClickedInventory().equals(event.getView().getTopInventory());
    }

    public boolean isRightClickInsertionIntoBundleGui(InventoryClickEvent event) {
        if (!event.getClick().isRightClick()) return false;
        if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) return false;
        if (event.getClickedInventory() == null) return false;
        return event.getClickedInventory().equals(event.getView().getTopInventory());
    }

    public boolean isShiftClickInsertionIntoBundleGui(InventoryClickEvent event) {
        if (event.getClick() != ClickType.SHIFT_LEFT && event.getClick() != ClickType.SHIFT_RIGHT) return false;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return false;
        if (event.getClickedInventory() == null) return false;
        return !event.getClickedInventory().equals(event.getView().getTopInventory());
    }

    public boolean isLeftClickInsertionIntoBundleGui(InventoryClickEvent event) {
        if (!event.getClick().isLeftClick()) return false;
        if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) return false;
        if (event.getClickedInventory() == null) return false;
        return event.getClickedInventory().equals(event.getView().getTopInventory());
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

    public boolean isShiftRightClickToEmptyVanillaBundle(InventoryClickEvent event) {
        return event.getClick() == ClickType.SHIFT_RIGHT && itemFactory.isOriginalBundle(event.getCursor());
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
