package com.scottcandy34.betterbundle;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Custom InventoryHolder implementation that ties an opened Bundle GUI
 * back to its originating BundleItem.
 *
 * This allows BundleListener to reliably identify which specific Bundle
 * was opened when the inventory is closed, so changes can be saved back
 * to the correct ItemStack instead of guessing.
 */
public class BundleInventoryHolder implements InventoryHolder {

    private final BundleItem bundleItem;

    /**
     * Creates a new holder linked to the given BundleItem.
     *
     * @param bundleItem The BundleItem that owns this inventory view
     */
    public BundleInventoryHolder(BundleItem bundleItem) {
        this.bundleItem = bundleItem;
    }

    /**
     * Returns the BundleItem associated with this inventory holder.
     *
     * @return The originating BundleItem
     */
    public BundleItem getBundleItem() {
        return bundleItem;
    }

    /**
     * Returns null because this holder does not manage a persistent
     * inventory on its own — it is only used as a marker to identify
     * which Bundle the player has open.
     */
    @Override
    public @Nullable Inventory getInventory() {
        return null;
    }

    /**
     * Copies the current contents of the GUI inventory the player was viewing
     * back into this Bundle's internal inventory handle, then calls update()
     * so that lore, durability bar, and the PersistentDataContainer are refreshed.
     */
    public void syncFromGuiInventory(Inventory guiInventory) {
        if (bundleItem == null || guiInventory == null) {
            return;
        }

        Inventory handle = bundleItem.getInventory().getHandle();
        if (handle != null) {
            handle.setContents(guiInventory.getContents());
        }

        bundleItem.repackAfterGuiClose();
        bundleItem.update();
    }

    /**
     * Safely updates the player's inventory (hotbar + main inventory) and cursor
     * with the latest version of this Bundle using UUID matching.
     * Only the exact Bundle that matches this holder's UUID is updated.
     * Prevents overwriting other Bundles the player may be holding.
     */
    public void refreshPlayerInventory(Player player) {
        if (player == null || bundleItem == null) return;

        UUID targetId = bundleItem.getBundleId();
        if (targetId == null) return;

        ItemStack updated = bundleItem.getBundle();

        // Update main inventory + hotbar
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack current = player.getInventory().getItem(slot);
            if (current == null) continue;

            try {
                BundleItem existing = new BundleItem(current);
                if (targetId.equals(existing.getBundleId())) {
                    player.getInventory().setItem(slot, updated.clone());
                }
            } catch (IllegalArgumentException ignored) {
                // Not one of our bundles
            }
        }

        // Also update cursor if it contains this Bundle
        ItemStack cursor = player.getItemOnCursor();
        if (cursor != null && cursor.getType() == Material.PLAYER_HEAD) {
            try {
                BundleItem cursorBundle = new BundleItem(cursor);
                if (targetId.equals(cursorBundle.getBundleId())) {
                    player.setItemOnCursor(updated.clone());
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }
}